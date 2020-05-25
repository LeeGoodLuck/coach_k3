package com.bootdo.coach.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bootdo.coach.domain.*;
import com.bootdo.coach.po.PayResult;
import com.bootdo.coach.service.*;
import com.bootdo.common.utils.DateUtils;
import com.bootdo.nio.mina.server.GlobalSessionMap;
import com.github.pagehelper.util.StringUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bootdo.coach.api.wx.HttpRequest;
import com.bootdo.coach.api.wx.TypeCoverUtil;
import com.bootdo.coach.api.wx.WXPayUtil;
import com.bootdo.coach.api.wx.WeixinUtil;
import com.bootdo.coach.key.RedisKey;
import com.bootdo.coach.po.TokenObj;
import com.bootdo.coach.result.ResponseEntityUtil;
import com.bootdo.common.utils.IPUtils;
import com.bootdo.common.utils.StringUtils;

import static com.bootdo.nio.constant.NormalCmdIdType.DeviceStatus;
import static com.bootdo.nio.constant.NormalCmdIdType.OperationResult;
import static com.bootdo.nio.constant.OperationType.OperationLogin;
import static com.bootdo.nio.constant.OperationType.OperationPayResult;

/**
 * 5.3学员端相关接口
 * @author Administrator
 *
 */
@RequestMapping("coach_background/student")
@RestController
public class StudentApi {
    private static final Logger logger = LoggerFactory.getLogger(StudentApi.class);
	@Autowired
	private TokenService tokenService;

	@Autowired
	private TabStudentService studentService;

	@Autowired
	private TabTestService testService;

	@Autowired
	private TabOrderService orderService;
	
	@Autowired
	private TabDeviceService deviceService;

	@Autowired
	private TabCoachService coachService;

	@Autowired
	private TabAmountService amountService;

	@Autowired
	private TabAmountDetailedService amountDetailedService;

	/**
	 * 预付费用 prepaid_expenses
	 **/
	@Value("${prepaid_expenses}")
	private String prepaid_expenses;

	/**
	 * 微信公众号支付 请求链接
	 **/
	private final String UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	/**
	 * APP ID appid
	 **/
	@Value("${weixin.charge_account}")
	private String charge_account;
	
	/**
	 * APP ID appid
	 **/
	@Value("${weixin.app_id}")
	private String app_id;
	/**
	 * APP商户号 mch_id
	 **/
	@Value("${weixin.mch_id}")
	private String mch_id;

	/**
	 * APP 商品秘钥 key
	 **/
	@Value("${weixin.api_key}")
	private String api_key;

	/**
	 * 商品描述 body
	 **/
	@Value("${weixin.body}")
	private String body;
	/**
	 * 异步回调地址 notify_url
	 **/
	@Value("${weixin.notify_url}")
	private String notify_url;

	
	/**
	 * 5.3.1学员登录
	 * 学员端通过微信登录，登录成功后调用本接口，如果学员表（tab_student）
	 * 没有此登录账号，则创建，有则更新注册时间，成功后返回学员id（student_id），如果status状态是禁用，则不允许登陆。
	 * @param request
	 * @param openid
	 * @param headimg
	 * @return
	 */
	@PostMapping("/login.do")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, String openid,String unionid,String wechat_name,String headimg) {
    	if (StringUtils.isEmpty(openid)) {
    		return ResponseEntityUtil.getFailEntity("openid参数不能为空");
		}
    	TabStudent student = studentService.selectByOpenId(openid);
    	if (student == null) {
    		student = new TabStudent();
    		student.setAccount("");
    		student.setCreateTime(new Date());
    		student.setHeadimg(headimg);
    		student.setStudentName(wechat_name);
    		student.setRemarks("");
    		student.setSex(0);
    		student.setStatus(1);
    		student.setOpenid(openid);
    		student.setUnionid(unionid);
    		student.setWechatName(wechat_name);
    		try {
    			int i = studentService.insertSelectiveAndReturnId(student);
        		if (i <= 0) {
        			return ResponseEntityUtil.getFailEntity("登录失败，操作db出错");
				}
			} catch (Exception e) {
				return ResponseEntityUtil.getFailEntity("登录失败，服务出异常");
			}
		}else {
			if (student.getStatus().intValue() == 0) {
	    		return ResponseEntityUtil.getFailEntity("账号已经禁用，不允许登陆");
	    	}
	    	// 更新登录时间和登录ip
			student.setLastLoginIp(IPUtils.getIpAddr(request));
			student.setLastLoginTime(new Date());
	    	if (!StringUtils.isEmpty(headimg)) {
	    		student.setHeadimg(headimg);
			}
	    	if (!StringUtils.isEmpty(wechat_name)) {
	    		student.setStudentName(wechat_name);
	    	}
	    	try {
	    		studentService.updateByPrimaryKeySelective(student);
			} catch (Exception e) {
				return ResponseEntityUtil.getFailEntity("登录失败，服务出异常");
			}
		}
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("student_id", student.getStudentId());
    	
    	// 设置token
    	TokenObj tokenObj = tokenService.createToken(student.getStudentId(), RedisKey.STUDENT_KEY);
        String authorization = tokenObj.getId().toString() + "_" + tokenObj.getKey() + "_" + tokenObj.getToken();
    	data.put("token", authorization);
        return ResponseEntityUtil.getSucEntity(data);
    }
    
	/**
	 * 5.3.2 学员扫描确认考试接口
	 * 必须教练扫描后学员才能扫描确认，即设备表（tab_device）的考试状态（test_status）只能为1时，学员才能扫描成功，否则学员扫描确认失败，学员扫描成功后，
	 * 修改设备表（tab_device）的考试状态（test_status）为2，同时记录学员id到test_student_id；
	 * 如果学员有未付的订单，要先付款再才能扫描练车
	 * @param student_id
	 * @param device_id
	 * @param token
	 * @return
	 */
	@PostMapping("/confirmTest.do")
    @ResponseBody
    public Map<String, Object> confirmTest(Long student_id, Long device_id, String token) {

		// 查找设备
		TabDevice device = deviceService.selectByPrimaryKey(device_id);
		if (device == null) {
			return ResponseEntityUtil.getFailEntity("设备不存在");
		}

    	// 验证token
    	String msg = tokenService.checkToken(token);
    	if (msg != null) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "验证token不通过");
    		return ResponseEntityUtil.getFailEntity(msg);
		}
    	
    	if (student_id == null || device_id == null) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "必要参数不能为空");
    		return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}

    	// 查找学员
    	TabStudent student = studentService.selectByPrimaryKey(student_id);
    	if (student == null) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "学员账号不存在");
    		return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
    	if (student.getStatus().intValue() == 0) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "该学员已经被禁用");
    		return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
    	}

		BigDecimal prepaidExpenses = new BigDecimal(prepaid_expenses);
		if(device.getSellTypeId() == 4){ //预付费设备
			BigDecimal amountBalance = student.getAmountBalance();
			if(amountBalance == null){
				amountBalance = new BigDecimal(0);
			}
			if(amountBalance.compareTo(prepaidExpenses) < 0){
				GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
						-4, "余额不足，请到公众号里充值");
				return ResponseEntityUtil.getFailEntity("余额不足，请到公众号里充值");
			}
		}

    	// 判断学员是否有未付款的订单
    	if (orderService.findUnPayOrderByStudentId(student_id) != null) {
    		return ResponseEntityUtil.getFailEntity(-3, "欠费，需要先扫描付费");
		}
        

    	//status状态1:正常，0:禁止
        if (device.getStatus().intValue() == 0) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "车辆不能使用");
        	return ResponseEntityUtil.getFailEntity(-2, "车辆不能使用");
		}
        //设备表（tab_device）的考试状态（test_status）只能为1时，学员才能扫描成功，否则学员扫描确认失败
        if (device.getTestStatus().intValue() != 1 && device.getTestStatus().intValue() != 2) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					-1, "教练还未扫描，请等待");
        	return ResponseEntityUtil.getFailEntity("教练还未扫描，请等待");
		}
        //学员扫描成功后，修改设备表（tab_device）的考试状态（test_status）为2，同时记录学员id到test_student_id；
        device.setTestStudentId(student_id);
        device.setTestStatus(2);
        int i = deviceService.updateByPrimaryKeySelective(device);
        if (i > 0) {
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
					0, "学员成功登陆");
			GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), DeviceStatus, OperationLogin,
					0, new Gson().toJson(device));
        	return ResponseEntityUtil.getSucEntity();
		}

		GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationLogin,
				-1, "学员登陆失败");
    	return ResponseEntityUtil.getFailEntity("修改设备失败，扫描失败");
    }

	/**
	 * 5.3.3 学员考试查询接口
	 * @param student_id
	 * @param test_id
	 * @param token
	 * @return
	 */
	@PostMapping("/queryTest.do")
	@ResponseBody
	public Map<String, Object> queryTest(Long student_id, Long test_id, String token) {
		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}
		
		if (student_id == null) {
			return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}
		if (test_id == null) {
			test_id = 0l;
		}
		
		// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
		}
		TabTest test = null;
		if (test_id == null || test_id == 0l) {
			//为0查最后一个
			test = testService.findLastOneTest(student_id);
		}else {
			test = testService.selectByPrimaryKey(test_id);
		}

		if (test == null) {
			return ResponseEntityUtil.getFailEntity("查询不到记录");
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("test_id", test.getTestId());
		data.put("test_time", test.getTestTime());
		data.put("test_duration", test.getTestDuration());
		data.put("plate_number", test.getPlateNumber());
		data.put("test_score", test.getTestScore());
		data.put("test_data", test.getTestData());
		return ResponseEntityUtil.getSucEntity(data);
	}
	
	/**
	 * 5.3.4 考试列表查询接口
	 * @param student_id
	 * @param pages_no
	 * @param perpage_number
	 * @param token
	 * @return
	 */
	@PostMapping("/queryTestList.do")
    @ResponseBody
    public Map<String, Object> queryTestList(Long student_id,Integer pages_no,Integer perpage_number, String token) {
    	if (pages_no == null || pages_no < 1) {
    		pages_no = 1;
		}
    	if (perpage_number == null || perpage_number < 1) {
    		perpage_number = 10;
    	}
    	// 验证token
    	String msg = tokenService.checkToken(token);
    	if (msg != null) {
    		return ResponseEntityUtil.getFailEntity(msg);
    	}
    	
    	if (student_id == null) {
    		return ResponseEntityUtil.getFailEntity("必要参数不能为空");
    	}
    	
    	// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
		}
    	// 总条数
    	int total = testService.findTestCnt(student_id);
    	// 总页数
    	int pages_total = 0;
    	if (total % perpage_number == 0) {
    		pages_total = total / perpage_number;
		}else {
			pages_total = total / perpage_number + 1;
		}

    	int startNum = (pages_no - 1) * perpage_number;
    	int limitNum = perpage_number;

    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("student_id", student_id);
    	paramMap.put("startNum", startNum);
    	paramMap.put("limitNum", limitNum);
    	List<Map<String, Object>> test_list = testService.queryTestList(paramMap);
    	
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("pages_total", pages_total);
    	data.put("test_list", test_list);
    	return ResponseEntityUtil.getSucEntity(data);
    }
	
	/**
	 * 5.3.5 订单支付列表查询接口
	 * 查询订单表（tab_order）,只查询支付成功的。
	 * @param student_id
	 * @param pages_no
	 * @param perpage_number
	 * @param token
	 * @return
	 */
	@PostMapping("/queryOrderList.do")
	@ResponseBody
	public Map<String, Object> queryOrderList(Long student_id,Integer pages_no,Integer perpage_number, String token) {
		if (pages_no == null || pages_no < 1) {
			pages_no = 1;
		}
		if (perpage_number == null || perpage_number < 1) {
			perpage_number = 10;
		}
		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}
		
		if (student_id == null) {
			return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}
		
		// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
		}
		// 总条数
		int total = orderService.queryOrderListCnt(student_id);
		// 总页数
		int pages_total = 0;
		if (total % perpage_number == 0) {
			pages_total = total / perpage_number;
		}else {
			pages_total = total / perpage_number + 1;
		}
		
		int startNum = (pages_no - 1) * perpage_number;
		int limitNum = perpage_number;
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("student_id", student_id);
		paramMap.put("startNum", startNum);
		paramMap.put("limitNum", limitNum);
		List<Map<String, Object>> order_list = orderService.queryOrderList(paramMap);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("pages_total", pages_total);
		data.put("order_list", order_list);
		return ResponseEntityUtil.getSucEntity(data);
	}

	/**
	 * 5.3.6 学员获取未付订单信息接口
	 * @param student_id
	 * @param token
	 * @return
	 */
	@PostMapping("/getChargeInfo.do")
	@ResponseBody
	public Map<String, Object> getChargeInfo(Long student_id, String token) {
		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}
		
		if (student_id == null) {
			return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}
		
		// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
		}

		BigDecimal amountBalance = student.getAmountBalance();
		if(null == amountBalance){
			amountBalance = new BigDecimal(0);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("charge_account", charge_account);
		data.put("amount_balance", amountBalance);
		data.put("amount_minimum", prepaid_expenses);
		
		TabOrder order = orderService.findUnPayOrderByStudentId(student_id);
		if (order == null) {
			return ResponseEntityUtil.getSucEntity("不欠费",data);
		}

		data.put("order_no", order.getOrderNo());
		data.put("test_id", order.getTestId());
		data.put("test_money", order.getTestMoney());
		
		return ResponseEntityUtil.getFailEntity("欠费", data);
	}

	/**
	 * 学员付款
	 * @param student_id
	 * @param order_no
	 * @param token
	 * @return
	 */
	@PostMapping("/h5Pay.do")
	@ResponseBody
	public Map<String, Object> h5Pay(Long student_id, String order_no, String token) {
		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}
		if (student_id == null || StringUtils.isEmpty(order_no)) {
			return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}
		
		// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经被禁用");
		}
		
		TabOrder order = orderService.selectByOrderNo(order_no);
		if (order == null) {
			return ResponseEntityUtil.getSucEntity("订单不存在");
		}
		if (order.getPayState().intValue() != 0) {
			return ResponseEntityUtil.getSucEntity("订单已经支付");
		}
		if (order.getStudentId().longValue() != student_id) {
			return ResponseEntityUtil.getSucEntity("订单不属于此学员");
		}

		try {
			String total_fee = String.valueOf(order.getTestMoney().multiply(new BigDecimal(100)).intValue());
			Map<String, String> dataToH5 = h5Pay(student.getOpenid(), order_no, order_no, total_fee);
			if(!dataToH5.containsKey("err_code")) {
				return ResponseEntityUtil.getSucStringEntity(dataToH5);
			}else {
				return ResponseEntityUtil.getFailEntity(dataToH5.get("err_code_des"));
			}
		} catch (Exception e) {
			logger.info("微信发起支付失败，订单号=" + order_no + ": " + e.getMessage());
			return ResponseEntityUtil.getFailEntity("微信发起支付失败，有异常");
		}
	}
	
	/**
	 * 发起微信h5支付
	 * @param opendId
	 * @param device_info
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> h5Pay(String opendId, String device_info, String out_trade_no, String total_fee) throws Exception {
		if (device_info == null || out_trade_no == null || total_fee == null) {
			throw new Exception("支付参数不能为空");
		}
		Map<String, String> dataToH5 = new HashMap<String, String>();
		Map<String, String> data = new HashMap<String, String>();
		JSONObject jsonXML = new JSONObject();
		JSONObject json = new JSONObject();
		try {
			data.put("openid", opendId);
			data.put("spbill_create_ip", InetAddress.getLocalHost().getHostAddress().toString());
			data.put("appid", app_id);
			data.put("mch_id", mch_id);
			// 设备号，这里填系统的订单号
			data.put("device_info", device_info);
			data.put("nonce_str", WeixinUtil.create_nonce_str());
			data.put("body", body);
			data.put("out_trade_no", out_trade_no);
			// 微信支付单位为分，需要乘100
			data.put("total_fee", total_fee);
			data.put("notify_url", notify_url);
			data.put("trade_type", "JSAPI");
			String sign = WXPayUtil.generateSignature(data, api_key);
			data.put("sign", sign);
			String dataXML = WeixinUtil.map2Xmlstring(data);

			// resp返回的是XML
			String resp = new HttpRequest().postData(UNIFIEDORDER, dataXML, "utf-8").toString();
			json = TypeCoverUtil.xml2JSON(resp.getBytes());

			jsonXML = (JSONObject) json.get("xml");
			logger.info("微信h5内支付，调用返回结果：" + JSONObject.toJSONString(jsonXML));
			logger.info("微信h5内支付，调用返回结果：" + JSONObject.toJSONString(jsonXML));
			if (!jsonXML.get("return_code").toString().contains("FAIL")) {
				if (jsonXML.get("result_code").toString().contains("SUCCESS")) {
					dataToH5.put("appId", app_id);
					dataToH5.put("timeStamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
					dataToH5.put("nonceStr", WeixinUtil.create_nonce_str());
					dataToH5.put("package", "prepay_id=" + jsonXML.get("prepay_id").toString().substring(1,
							jsonXML.get("prepay_id").toString().length() - 1));
					dataToH5.put("signType", "MD5");
					String sign1 = WXPayUtil.generateSignature(dataToH5, api_key);
					dataToH5.put("paySign", sign1);
				} else {
					dataToH5.put("err_code", jsonXML.get("err_code").toString());
					dataToH5.put("err_code_des", jsonXML.get("err_code_des").toString());
				}
			}else {
				dataToH5.put("err_code", jsonXML.get("return_code").toString());
				dataToH5.put("err_code_des", "该功能暂未开发");
			}
			return dataToH5;
		} catch (Exception e) {
			logger.error("微信app支付失败，原因：" + e.getMessage());
			throw e;
		}
	}

	/**
	 * 微信支付   异步回调地址
	 * @throws Exception
	 * **/
	@RequestMapping("/weixinPayResult")
	@ResponseBody
	public void weixinPayResult(HttpServletRequest request, HttpServletResponse response) {
		TabOrder order = null;
		try {
			logger.info("微信回调");
	        InputStream inStream = request.getInputStream();
	        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int len = 0;
	        while ((len = inStream.read(buffer)) != -1) {
	            outSteam.write(buffer, 0, len);
	        }
	        String resultxml = new String(outSteam.toByteArray(), "utf-8");
	        Map<String, String> params = WXPayUtil.xmlToMap(resultxml);
	        outSteam.close();
	        inStream.close();
	        logger.info("微信回调，params = " + JSONObject.toJSONString(params));
	        // 拿到微信返回的信息之后，可以给微信回信息了
	        //返回给微信应答
	        String returnxml = "<xml>" +
	                    "   <return_code><![CDATA[SUCCESS]]></return_code>" +
	                    "   <return_msg><![CDATA]></return_msg>" +
	                    "</xml>";
	        response.getWriter().write(returnxml);
	        response.getWriter().flush();

	        if (!WXPayUtil.isSignatureValid(params, api_key)) {  //验证签名
	            // 支付失败
	        	logger.error("支付失败，验证签名失败，回调的结果：" + JSONObject.toJSONString(params));
	        }else {		
	        	//付款成功
	        	//验证订单是否存在orderNo
	        	String device_info = params.get("device_info");
	        	order = orderService.selectByOrderNo(device_info);
	        	if (order == null) {
	        		logger.info("订单不存在，orderNo=" + device_info);
					return ;
				}

				//根据订单查找设备，增加设备总的已支付金额
				logger.info("根据订单查找设备，DeviceId = " + order.getDeviceId());
				TabDevice device = deviceService.selectByPrimaryKey(order.getDeviceId());
				if (device != null) {
					device.setTotalTestPay(device.getTotalTestPay().add(order.getTestMoney()));
					int i = deviceService.updateByPrimaryKeySelective(device);
					if (i > 0) {
						logger.info("累加设备已支付金额，TotalTestPay = " + device.getTotalTestPay());
					}

					PayResult payResult = new PayResult(device.getDeviceNo(), order.getOrderNo(), order.getStudentId().intValue(), order.getCoachId().intValue(), 0
							, "支付成功，感谢使用", new Date());
					GlobalSessionMap.sendNormalCommand(device.getDeviceNo(), OperationResult, OperationPayResult,
							0, new Gson().toJson(payResult));
				}

	        	if (order.getPayState() != 0) {
					//0表示未支付，1已支付
	        		logger.info("订单已经处理了，orderNo=" + device_info);
					return ;
				}

				long testId = order.getTestId();
				if(testId == 0){  //学员充值
					TabStudent student = studentService.selectByPrimaryKey(order.getStudentId());
					// 查找学员
					if (student == null) {
						logger.error("考试学员账号不存在.");
						return ;
					}
					BigDecimal amountBalance = student.getAmountBalance();
					if(amountBalance == null){
						amountBalance = new BigDecimal(0);
					}
					student.setAmountBalance(amountBalance.add(order.getTestMoney()));
					if(order.getMoneyGive() != null && order.getMoneyGive().compareTo(new BigDecimal(0))>0){
						BigDecimal amountGive = student.getAmountGive();
						if(null == amountGive){
							amountGive = new BigDecimal(0);
						}
						student.setAmountGive(amountGive.add(order.getMoneyGive()));
					}
					int i = studentService.updateByPrimaryKeySelective(student);
					if(i <= 0){
						logger.error("更新学员可用余额失败.");
						return;
					}

					order.setSeparateStatus(1);
					order.setPayState(1);
					i = orderService.updateByPrimaryKeySelective(order);
					if(i <= 0){
						logger.error("更新学员订单表失败.");
						return;
					}

					TabAmount amount = amountService.selectByPrimaryKey((long)1);
					if(null == amount){
						logger.error("平台账户表没有记录.");
						return;
					}
					amount.setAmountBalance(amount.getAmountBalance().add(order.getTestMoney()));
					amount.setAmountIncome(amount.getAmountIncome().add(order.getTestMoney()));
					i = amountService.updateByPrimaryKeySelective(amount);
					if(i <= 0){
						logger.error("更新平台账户表失败.");
						return;
					}
					TabAmountDetailed amountDetailed = new TabAmountDetailed();
					amountDetailed.setDetailedType(0);
					amountDetailed.setAmount(order.getTestMoney());
					amountDetailed.setOrderId(order.getOrderId());
					amountDetailed.setTestId((long)0);
					amountDetailed.setCoachId(order.getCoachId());
					amountDetailed.setStudentId(order.getStudentId());
					amountDetailed.setOrderNo(order.getOrderNo());
					amountDetailed.setPayTime(new Date());
					i = amountDetailedService.insertSelective(amountDetailed);
					if(i <= 0){
						logger.error("插入平台收支明细表失败.");
						return;
					}


				}else { //考试
					TabAmountDetailed amountDetailed = new TabAmountDetailed();
					amountDetailed.setDetailedType(0);
					amountDetailed.setAmount(order.getTestMoney());
					amountDetailed.setOrderId(order.getOrderId());
					amountDetailed.setTestId(order.getTestId());
					amountDetailed.setTestTime(order.getTestTime());
					amountDetailed.setTestDuration(order.getTestDuration());
					amountDetailed.setPlateNumber(order.getPlateNumber());
					amountDetailed.setCoachId(order.getCoachId());
					amountDetailed.setStudentId(order.getStudentId());
					amountDetailed.setOrderNo(order.getOrderNo());
					amountDetailed.setPayTime(new Date());
					int i = amountDetailedService.insertSelective(amountDetailed);
					if(i <= 0){
						logger.error("插入平台收支明细表失败.");
						return;
					}

					if (device != null) {
						device.setTotalTestPay(device.getTotalTestPay().add(order.getTestMoney()));
						i = deviceService.updateByPrimaryKeySelective(device);
						if (i > 0) {
							logger.info("累加设备已支付金额，TotalTestPay = " + device.getTotalTestPay());
						}
					}
				}
                try {
                    //如果有返回微信订单号
                    String transaction_id = params.get("transaction_id");
                    //处理业务
                    orderService.dealOrder(order, transaction_id);
                }catch (RuntimeException e) {
                    logger.error("处理业务失败(dealOrder)，" + e.getMessage());
                }


			}
		}catch (RuntimeException e) {
			logger.error("处理业务失败，" + e.getMessage());
		} catch (Exception e) {
			logger.error("payment ... weixinPayResult:{}",e.toString());
		}
	}

	/**
	 * 5.4.18 公众号充值生成订单
	 *
	 * @param token
	 * @return
	 */
	@PostMapping("/recharge.do")
	@ResponseBody
	public Map<String, Object> rechargeOrder(String token, BigDecimal recharge_money,Long student_id,long coachId, BigDecimal amount_give) {
		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}

		TabCoach tabCoach = coachService.selectByPrimaryKey(coachId);

		TabStudent student = studentService.selectByPrimaryKey(student_id);
		// 查找设备
		// 查找学员
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("考试学员账号不存在");
		}

		try{
			String order_no = "";
			TabOrder order = new TabOrder();
			order.setOrderNo(createOrderNo());
			order.setPayState(0);
			order.setSeparateStatus(0);
			order.setTestTime(new Date());
			if (student != null) {
				order.setStudentId(student.getStudentId());
				order.setStudentName(student.getStudentName());
			}
//            order.setTestDuration(test.getTestDuration());
			order.setTestId((long)0);
			order.setTestMoney(recharge_money);
			order.setCoachId(coachId);
			order.setMoneyGive(amount_give);
			if(null != tabCoach && StringUtil.isNotEmpty(tabCoach.getCoachName())){
				order.setCoachName(tabCoach.getCoachName());
			}
			order_no = order.getOrderNo();

			int i = orderService.insertSelective(order);
			if (i <= 0) {
				return ResponseEntityUtil.getFailEntity("学员充值失败 ，操作订单db出错");
			}

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("order_no", order_no);
			data.put("charge_account", charge_account);
			data.put("recharge_money", recharge_money);

			return ResponseEntityUtil.getSucEntity(data);
		}catch (Exception e){
			logger.error("学员充值失败，服务出异常: " + e.getMessage());
			return ResponseEntityUtil.getFailEntity("学员充值失败，服务出异常");
		}
	}

	/**
	 * “O” 加当前时间加四位随机数
	 * 订单编号
	 * order_no
	 *
	 * @throws Exception
	 **/
	private String createOrderNo() throws Exception {
		return "O" + DateUtils.getNowLongTime() + String.valueOf((int) (Math.random() * (9999 - 1000 + 1)) + 1000);
	}

	/**
	 * 学员金额查询接口
	 *
	 * @param student_id
	 * @param token
	 * @return
	 */
	@PostMapping("/query_withdraw.do")
	@ResponseBody
	public Map<String, Object> query_withdraw(Long student_id, String token) {

		// 验证token
		String msg = tokenService.checkToken(token);
		if (msg != null) {
			return ResponseEntityUtil.getFailEntity(msg);
		}

		if (student_id == null) {
			return ResponseEntityUtil.getFailEntity("必要参数不能为空");
		}

		// 查找学员
		TabStudent student = studentService.selectByPrimaryKey(student_id);
		if (student == null) {
			return ResponseEntityUtil.getFailEntity("学员账号不存在");
		}
		if (student.getStatus().intValue() == 0) {
			return ResponseEntityUtil.getFailEntity("该学员已经禁用");
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("amount_balance", student.getAmountBalance());
		return ResponseEntityUtil.getSucEntity(data);
	}

	/**
	 * 根据账户查询教练
	 *
	 * @param tel
	 * @return
	 */
	@PostMapping("/query_coach.do")
	@ResponseBody
	public Map<String, Object> query_coach(String tel) {
		TabCoach tabCoach = coachService.findCoachByTel(tel);
		if(tabCoach == null){
			return ResponseEntityUtil.getFailEntity("教练手机错误重新输入");
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("coachName", tabCoach.getCoachName());
		data.put("heading", tabCoach.getHeadimg());
		data.put("coachId", tabCoach.getCoachId());
		data.put("chargeRate", tabCoach.getChargeRate());
		return ResponseEntityUtil.getSucEntity(data);
	}

//	@RequestMapping("/orderTest")
//	@ResponseBody
// 分账测试接口
//	public String orderTest(){
//		TabOrder tabOrder = orderService.selectByOrderNo("O201906181851112909");
//		try{
//			orderService.dealOrder(tabOrder, "4200000319201906183305093816");
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		return "1";
//	}
}
