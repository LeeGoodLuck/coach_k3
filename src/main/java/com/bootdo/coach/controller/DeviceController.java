package com.bootdo.coach.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

import com.bootdo.coach.domain.*;
import com.bootdo.coach.service.*;
import com.bootdo.common.excel.ExcelUtil;
import com.bootdo.nio.mina.server.GlobalSessionMap;
import com.bootdo.system.domain.UserDO;
import com.google.gson.Gson;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootdo.coach.vo.TabDeviceVo;
import com.bootdo.common.annotation.Log;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;
import com.bootdo.common.utils.R;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.bootdo.nio.constant.NormalCmdIdType.DeviceStatus;
import static com.bootdo.nio.constant.OperationType.OperationLogin;

/**
 * 设备端
 * 
 * @author Administrator
 *
 */
@RequestMapping("/coach/device")
@Controller
public class DeviceController extends BaseController {
	private String prefix = "coach/device";
	// 设备=============>>>开始
	
	@Autowired
	private TabDeviceService deviceService;
	
	@Autowired
	private TabDeviceTypeService deviceTypeService;
	
	@Autowired
	private TabSellTypeService sellTypeService;
	
	@Autowired
	private TabCoachService coachService;
	
	@Autowired
	private TabChargeTypeService chargeTypeService;

	@Autowired
	private TabAgentService tabAgentService;

	@Autowired
	private TabAgentService agentService;

	@RequiresPermissions("coach:device:device")
	@GetMapping("device")
	String device(Model model) {
		// 设备类型
		Map<String, Object> query = new HashMap<String, Object>();
		List<TabDeviceType> deviceTypeList = deviceTypeService.list(query);
		if (deviceTypeList == null) {
			deviceTypeList = new ArrayList<TabDeviceType>();
		}
		TabDeviceType deviceType = new TabDeviceType();
		deviceType.setDeviceTypeId(0);
		deviceType.setDeviceTypeName("请选择设备类型");
		deviceTypeList.add(0, deviceType);
		model.addAttribute("deviceTypeList", deviceTypeList);
		return prefix + "/device";
	}

	@RequiresPermissions("coach:device:device")
	@GetMapping("deviceBuyout")
	String deviceBuyout(Model model) {
		Map<String, Object> query = new HashMap<String, Object>();
		List<TabDeviceType> deviceTypeList = deviceTypeService.list(query);
		if (deviceTypeList == null) {
			deviceTypeList = new ArrayList<TabDeviceType>();
		}
		TabDeviceType deviceType = new TabDeviceType();
		deviceType.setDeviceTypeId(0);
		deviceType.setDeviceTypeName("请选择设备类型");
		deviceTypeList.add(0, deviceType);
		model.addAttribute("deviceTypeList", deviceTypeList);
		return prefix + "/deviceBuyout";
	}
	
	@RequiresPermissions("coach:device:deviceType")
	@GetMapping("deviceType")
	String deviceType(Model model) {
		return prefix + "/deviceType";
	}

	@ApiOperation(value = "获取设备列表", notes = "")
	@ResponseBody
	@GetMapping("/deviceList")
	@RequiresPermissions("coach:device:device")
	public PageUtils deviceList(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		if (params != null&&params.containsKey("remarks")&&!"".equals(params.get("remarks").toString())) {
			params.put("remarks", "%" + params.get("remarks").toString() + "%");
		}
		UserDO userDO = getUser();
		Long role = (userDO.getRoleIds()).get(0);
		Set<Long> coachIdList = new HashSet<Long>();
		List<TabCoach> coachList = null;
		Set<Long> agentIdList = new HashSet<Long>();
		if (role != 1) {
			//查询登录代理的数据
			TabAgent curAgent = tabAgentService.findAgentByTel(userDO.getUsername());
			if (curAgent != null) {
				agentIdList = new HashSet<Long>();
				agentIdList.add(curAgent.getAgentId());
				Set<Long> tempList = null;
				switch (curAgent.getLevel()) {
					case 0:
						// 找出下级
						tempList = tabAgentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
							// 找出下下级
							tempList = tabAgentService.findAgentIdList(agentIdList);
							if (tempList != null) {
								agentIdList.addAll(tempList);
							}
						}
						break;
					case 1:
						// 找出下级
						tempList = tabAgentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
						}
						break;

					default:
						break;
				}
				// 找出代理商下的教练
				coachIdList = coachService.findCoachIdList(agentIdList);
			}
			params.put("coachIdList", coachIdList);
		}

		Query query = new Query(params);
		List<TabDeviceVo> deviceList = null;
		int total = 0;
		if (role == 1) {
			deviceList = deviceService.list(query);
			for (int i=0;i<deviceList.size();i++){
				BigDecimal totalTestTime= new BigDecimal((float)deviceList.get(i).getTotalTestTime()/60);
				deviceList.get(i).setTotalTestTime(totalTestTime.intValue());
			}
			total = deviceService.count(query);
		} else {
			deviceList = deviceService.findCoachDevice(query);
			for (int i=0;i<deviceList.size();i++){
				BigDecimal totalTestTime= new BigDecimal((float)deviceList.get(i).getTotalTestTime()/60);
				deviceList.get(i).setTotalTestTime(totalTestTime.intValue());
			}
			total = deviceService.countsDevice(query);
		}
		PageUtils pageUtil = new PageUtils(deviceList, total);
		return pageUtil;
	}
	public Set<Long> agentIdLists() {
		//代理
		UserDO userDO = getUser();
		//查询当前登录角色
		Long role = (userDO.getRoleIds()).get(0);
		Set<Long> agentIdList = new HashSet<Long>();
		if (role != 1) {
			TabAgent curAgent = agentService.findAgentByTel(userDO.getUsername());
			if (curAgent != null) {
				agentIdList = new HashSet<Long>();
				agentIdList.add(curAgent.getAgentId());

				Set<Long> tempList = null;
				switch (curAgent.getLevel()) {
					case 0:
						// 找出下级
						tempList = agentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
							// 找出下下级
							tempList = agentService.findAgentIdList(agentIdList);
							if (tempList != null) {
								agentIdList.addAll(tempList);
							}
						}
						break;
					case 1:
						// 找出下级
						tempList = agentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
						}
						break;

					default:
						break;
				}
			}
		}
		return agentIdList;
	}

	@RequiresPermissions("coach:device:deviceAdd")
	@Log("添加设备")
	@GetMapping("/deviceAdd")
	String deviceAdd(Model model) {
		Map<String, Object> query = new HashMap<String, Object>();
		// 设备类型
		List<TabDeviceType> deviceTypeList = deviceTypeService.list(query);
		if (deviceTypeList == null) {
			deviceTypeList = new ArrayList<TabDeviceType>();
		}
		TabDeviceType deviceType = new TabDeviceType();
		deviceType.setDeviceTypeId(0);
		deviceType.setDeviceTypeName("请选择");
		deviceTypeList.add(0, deviceType);
		model.addAttribute("deviceTypeList", deviceTypeList);
		//销售类型
		List<TabSellType> sellTypeList = sellTypeService.findSellTypeList();
		if (sellTypeList == null) {
			sellTypeList = new ArrayList<TabSellType>();
		}
		TabSellType sellType = new TabSellType();
		sellType.setSellTypeId(0);
		sellType.setSellTypeName("请选择");
		sellTypeList.add(0, sellType);
		model.addAttribute("sellTypeList", sellTypeList);
		//代理
		Set<Long> agentIdList = agentIdLists();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if (agentIdList.size() != 0) {
			paramMap.put("agentIdList", agentIdList);
		}
		List<TabAgent> agentList = tabAgentService.findAgentList(paramMap);
		if (agentList == null) {
			agentList = new ArrayList<TabAgent>();
		}
		TabAgent agent = new TabAgent();
		agent.setAgentId(0l);
		agent.setAgentName("请选择");
		agentList.add(0, agent);
		model.addAttribute("agentList", agentList);

		//收费模式
		List<TabChargeType> chargeTypeList = chargeTypeService.findChargeTypeList();
		if (chargeTypeList == null) {
			chargeTypeList = new ArrayList<TabChargeType>();
		}
		TabChargeType chargeType = new TabChargeType();
		chargeType.setChargeTypeId(0);
		chargeType.setChargeTypeName("请选择");
		chargeTypeList.add(0, chargeType);
		model.addAttribute("chargeTypeList", chargeTypeList);
		
		return prefix + "/deviceAdd";
	}
	@Log("查询代理名下的教练")
	@RequestMapping(value = "/queryCoachList")
	@ResponseBody
	List<TabCoach> coachList(String agentId) {
		List<TabCoach> coachList = coachService.findCoachList(agentId);
		if (coachList == null) {
			coachList = new ArrayList<TabCoach>();
		}
		TabCoach coach = new TabCoach();
		coachList.add(0, coach);
		return coachList;
	}

	@ApiOperation(value = "获取设备类型列表", notes = "")
	@ResponseBody
	@GetMapping("/deviceTypeList")
	@RequiresPermissions("coach:device:deviceType")
	public PageUtils deviceTypeList(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		Query query = new Query(params);
		List<TabDeviceType> deviceTypeList = deviceTypeService.list(query);
		int total = deviceTypeService.count(query);
		PageUtils pageUtil = new PageUtils(deviceTypeList, total);
		return pageUtil;
	}
	
	@RequiresPermissions("coach:device:deviceTypeAdd")
	@Log("添加设备")
	@GetMapping("/deviceTypeAdd")
	String deviceTypeAdd(Model model) {
		return prefix + "/deviceTypeAdd";
	}

	@RequiresPermissions("coach:device:deviceEdit")
	@Log("编辑设备")
	@GetMapping("/deviceEdit/{deviceId}")
	String deviceEdit(Model model, @PathVariable("deviceId") Long deviceId) {

		Map<String, Object> query = new HashMap<String, Object>();
		// 设备类型
		List<TabDeviceType> deviceTypeList = deviceTypeService.list(query);
		if (deviceTypeList == null) {
			deviceTypeList = new ArrayList<TabDeviceType>();
		}
		TabDeviceType deviceType = new TabDeviceType();
		deviceType.setDeviceTypeId(0);
		deviceType.setDeviceTypeName("请选择");
		deviceTypeList.add(0, deviceType);
		model.addAttribute("deviceTypeList", deviceTypeList);
		//销售类型
		List<TabSellType> sellTypeList = sellTypeService.findSellTypeList();
		if (sellTypeList == null) {
			sellTypeList = new ArrayList<TabSellType>();
		}
		TabSellType sellType = new TabSellType();
		sellType.setSellTypeId(0);
		sellType.setSellTypeName("请选择");
		sellTypeList.add(0, sellType);
		model.addAttribute("sellTypeList", sellTypeList);
		//代理
		Set<Long> agentIdList = agentIdLists();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if (agentIdList.size() != 0) {
			paramMap.put("agentIdList", agentIdList);
		}
		List<TabAgent> agentList = tabAgentService.findAgentList(paramMap);
		if (agentList == null) {
			agentList = new ArrayList<TabAgent>();
		}
		TabAgent agent = new TabAgent();
		agent.setAgentId(0l);
		agent.setAgentName("请选择");
		agentList.add(0, agent);
		model.addAttribute("agentList", agentList);
		//收费模式
		List<TabChargeType> chargeTypeList = chargeTypeService.findChargeTypeList();
		if (chargeTypeList == null) {
			chargeTypeList = new ArrayList<TabChargeType>();
		}
		TabChargeType chargeType = new TabChargeType();
		chargeType.setChargeTypeId(0);
		chargeType.setChargeTypeName("请选择");
		chargeTypeList.add(0, chargeType);
		model.addAttribute("chargeTypeList", chargeTypeList);
		TabDevice device = deviceService.selectByPrimaryKey(deviceId);
		model.addAttribute("device", device);
		return prefix + "/deviceEdit";
	}

	@RequiresPermissions("coach:device:deviceAdd")
	@Log("保存设备")
	@PostMapping("/saveDevice")
	@ResponseBody
	R saveDevice(TabDevice device) {
		device.setCreateTime(new Date());
		if (device.getSellTypeId() == null || device.getSellTypeId() == 0) {
			return R.error("请选择销售类型");
		}
		if (device.getChargeTypeId() == null || device.getChargeTypeId() == 0) {
			return R.error("请选择收费模式");
		}
		if (device.getCoachId() == null || device.getCoachId() == 0l) {
			return R.error("请选择所属教练");
		}
		if (device.getDeviceTypeId() == null || device.getDeviceTypeId() == 0) {
			return R.error("请选择设备类型");
		}
		if (deviceService.insertSelective(device) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:device:deviceTypeAdd")
	@Log("保存设备类型")
	@PostMapping("/saveDeviceType")
	@ResponseBody
	R saveDevice(TabDeviceType deviceType) {
		if (deviceTypeService.insertSelective(deviceType) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("coach:device:deviceEdit")
	@Log("更新设备")
	@PostMapping("/updateDevice")
	@ResponseBody
	R updateDevice(TabDevice device) {
		if (device.getSellTypeId() == null || device.getSellTypeId() == 0) {
			return R.error("请选择销售类型");
		}
		UserDO userDO = getUser();
		Long role = (userDO.getRoleIds()).get(0);
		if (role != 1) {
			if (device.getSellTypeId() == 2) {
				return R.error("不能修改成买断设备");
			}
		}
		if (device.getChargeTypeId() == null || device.getChargeTypeId() == 0) {
			return R.error("请选择收费模式");
		}
		if (device.getCoachId() == null || device.getCoachId() == 0l) {
			return R.error("请选择所属教练");
		}
		if (device.getDeviceTypeId() == null || device.getDeviceTypeId() == 0) {
			return R.error("请选择设备类型");
		}
		if (deviceService.updateByPrimaryKeySelective(device) > 0) {
			TabDevice tabDevice = deviceService.selectByPrimaryKey(device.getDeviceId());
			GlobalSessionMap.sendNormalCommand(tabDevice.getDeviceNo(), DeviceStatus, OperationLogin,
					0, new Gson().toJson(tabDevice));
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("coach:device:deviceRemove")
	@Log("删除设备")
	@PostMapping("/removeDevice")
	@ResponseBody
	R removeDevice(Long deviceId) {
		TabDevice device = deviceService.selectByPrimaryKey(deviceId);
		String dno = "";
		if (device != null) {
			dno = device.getDeviceNo();
		}
		if (deviceService.deleteByPrimaryKey(deviceId) > 0) {
			if (dno != null && dno.length() > 0) {
				GlobalSessionMap.sendNormalCommand(dno, DeviceStatus, OperationLogin,
						-11, new Gson().toJson(device));
			}
			return R.ok();
		}
		return R.error();
	}
	
	@RequiresPermissions("coach:device:deviceTypeRemove")
	@Log("删除设备类型")
	@PostMapping("/removeDeviceType")
	@ResponseBody
	R removeDeviceType(Integer deviceTypeId) {
		if (deviceTypeService.deleteByPrimaryKey(deviceTypeId) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@PostMapping("/exitDeviceNo")
	@ResponseBody
	boolean exitDeviceNo(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !deviceService.exitDeviceNo(params);
	}
	
	@PostMapping("/exitDeviceTypeName")
	@ResponseBody
	boolean exitDeviceTypeName(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !deviceTypeService.exitDeviceTypeName(params);
	}

	// 设备=============>>>结束

	@RequiresPermissions("coach:device:deviceRanking")
	@GetMapping("deviceRanking")
	String coachRanking(Model model) {
		return prefix + "/deviceRanking";
	}


	@RequestMapping(value = "/deviceTimeTypeId/{typeId}")
	@Log("按时间查询设备排行榜")
	@RequiresPermissions("coach:coach:coachRanking")
	String timeRankingtypeId(Model model, @PathVariable("typeId") String typeId) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("typeId", typeId);
		model.addAttribute("query", query);
		return prefix + "/deviceTimeRanking";
	}

	@RequestMapping("deviceListRanking")
	@ResponseBody
	@Log("查看设备使用排行")
	PageUtils deviceListRanking(@RequestParam Map<String, Object> params) {
		//获取当前登录用户
		UserDO userDO = getUser();
		//查询当前登录角色
		Long role = (userDO.getRoleIds()).get(0);
		List<TabDevice> device = null;
		if (role == 1) {
			//管理员登录查看所有设备的情况
			device = deviceService.sumDeviceRanking(params);
		} else {
			//查询代理表的代理数据
			TabAgent tabAgent = tabAgentService.findAgentByTel(userDO.getUsername());
			if (tabAgent != null) {
				//1、根据登录的代理ID查询名下的教练2、根据教练查询出设备
			}
		}
		PageUtils pageUtil = new PageUtils(device, 0);
		return pageUtil;
	}
	/**
	 * 导出报表
	 *
	 * @return
	 */
	@RequestMapping(value = "/deviceExcel")
	@ResponseBody
	@RequiresPermissions("coach:device:deviceExcel")
	public void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> params = new HashMap<>();
		String sellTypeId = request.getParameter("sellTypeId");
		System.out.println("sellTypeId的值是====" + sellTypeId);
		params.put("sellTypeId", sellTypeId);
		UserDO userDO = getUser();
		Long role = (userDO.getRoleIds()).get(0);
		Set<Long> coachIdList = new HashSet<Long>();
		List<TabCoach> coachList = null;
		Set<Long> agentIdList = new HashSet<Long>();
		if (role != 1) {
			//查询登录代理的数据
			TabAgent curAgent = tabAgentService.findAgentByTel(userDO.getUsername());
			if (curAgent != null) {
				agentIdList = new HashSet<Long>();
				agentIdList.add(curAgent.getAgentId());

				Set<Long> tempList = null;
				switch (curAgent.getLevel()) {
					case 0:
						// 找出下级
						tempList = tabAgentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
							// 找出下下级
							tempList = tabAgentService.findAgentIdList(agentIdList);
							if (tempList != null) {
								agentIdList.addAll(tempList);
							}
						}
						break;
					case 1:
						// 找出下级
						tempList = tabAgentService.findAgentIdList(agentIdList);
						if (tempList != null) {
							agentIdList.addAll(tempList);
						}
						break;

					default:
						break;
				}
				// 找出代理商下的教练
				coachIdList = coachService.findCoachIdList(agentIdList);
			}
			params.put("coachIdList", coachIdList);
		}
		List<TabDeviceVo> deviceList = null;
		if (role == 1) {
			deviceList = deviceService.list(params);
			for (int i = 0; i < deviceList.size(); i++) {
				BigDecimal totalTestTime = new BigDecimal((float) deviceList.get(i).getTotalTestTime() / 60);
				deviceList.get(i).setTotalTestTime(totalTestTime.intValue());
			}
		} else {
			deviceList = deviceService.findCoachDevice(params);
			for (int i = 0; i < deviceList.size(); i++) {
				BigDecimal totalTestTime = new BigDecimal((float) deviceList.get(i).getTotalTestTime() / 60);
				deviceList.get(i).setTotalTestTime(totalTestTime.intValue());
			}
		}
		//excel标题
		String[] title = {"id", "设备id", "绑定车牌", "激活时间", "销售类型", "所属设备", "设备类型",
				"调试时间", "考试时长(/分)", "考试收入", "已收金额", "最后登录时间", "收费模式", "押金", "备注", "状态"};
		//excel文件名
		String sheetName = null;
		if (sellTypeId.equals("1")) {
			sheetName = "扫描设备表";
		} else if (sellTypeId.equals("2")) {
			sheetName = "买断设备表";
		} else if (sellTypeId.equals("3")) {
			sheetName = "租赁设备表";
		} else if (sellTypeId.equals("4")) {
			sheetName = "预付费设置表";
		}
		String fileName = sheetName + System.currentTimeMillis() + ".xls";
		String[][] content = new String[deviceList.size()][];
		for (int i = 0; i < deviceList.size(); i++) {
			content[i] = new String[title.length];
			TabDeviceVo obj = deviceList.get(i);
			content[i][0] = String.valueOf(obj.getDeviceId());
			content[i][1] = obj.getDeviceNo();
			content[i][2] = obj.getPlateNumber();
			content[i][3] = String.valueOf(obj.getCreateTime());
			content[i][4] = obj.getSellTypeName();
			content[i][5] = obj.getCoachName();
			content[i][6] = obj.getDeviceTypeName();
			content[i][7] = String.valueOf(obj.getFreeTime());
			content[i][8] = String.valueOf(obj.getTotalTestTime());
			content[i][9] = String.valueOf(obj.getTotalTestIncome());
			content[i][10] = String.valueOf(obj.getTotalTestPay());
			content[i][11] = String.valueOf(obj.getLastLoginTime());
			content[i][12] = obj.getChargeTypeName();
			content[i][13] = String.valueOf(obj.getDeposit());
			content[i][14] = obj.getRemarks();
			if (obj.getStatus() == 0) {
				content[i][15] = "禁用";
			} else if (obj.getStatus() == 1) {
				content[i][15] = "正常";
			}
		}
		//创建HSSFWorkbook 　　　　　　
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);
		//响应到客户端 　　　　　　
		try {
			ExcelUtil util = new ExcelUtil();
			util.setResponseHeader(response, fileName);
			OutputStream os = response.getOutputStream();
			wb.write(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}