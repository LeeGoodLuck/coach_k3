package com.bootdo.coach.controller;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bootdo.common.excel.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootdo.coach.service.TabOrderService;
import com.bootdo.coach.service.TabStudentService;
import com.bootdo.coach.vo.TabOrderVo;
import com.bootdo.coach.vo.TabStudentVo;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;

import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 学员端
 * @author Administrator
 *
 */
@RequestMapping("/coach/order")
@Controller
public class OrderController extends BaseController{
	private String prefix = "coach/order";
	@Autowired
	private TabOrderService orderService;
	@RequiresPermissions("coach:order:order")
	@GetMapping("order")
	String order(Model model) {
		return prefix + "/order";
	}

	@ApiOperation(value = "获取学员列表", notes = "")
	@ResponseBody
	@GetMapping("/orderList")
	@RequiresPermissions("coach:order:order")
	public PageUtils orderList(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		Query query = new Query(params);
		List<TabOrderVo> orderList = orderService.list(query);
		int total = orderService.count(query);
		PageUtils pageUtil = new PageUtils(orderList, total);
		return pageUtil;
	}


	/**
	 * 导出报表
	 *
	 * @return
	 */
	@RequestMapping(value = "/orderExcel")
	@ResponseBody
	@RequiresPermissions("coach:order:orderExcel")
	public void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> params = new HashMap<>();
		List<TabOrderVo> orderList = orderService.list(params);
		//excel标题
		String[] title = {"id", "考试ID", "学员名称", "支付金额", "支付时间", "教练", "教练分成",
				"一级代理分成", "二级代理分成", "三级代理商分成", "运营商分成"};
		//excel文件名
		String fileName = "支付订单表" + System.currentTimeMillis() + ".xls";
		//sheet名currentTimeMillis
		String sheetName = "支付订单表";
		String[][] content = new String[orderList.size()][];
		for (int i = 0; i < orderList.size(); i++) {
			content[i] = new String[title.length];
			TabOrderVo obj = orderList.get(i);
			content[i][0] = String.valueOf(obj.getOrderId());
			content[i][1] = String.valueOf(obj.getTestId());
			content[i][2] = obj.getStudentName();
			content[i][3] = String.valueOf(obj.getTestMoney());
			content[i][4] = String.valueOf(obj.getPayTime());
			content[i][5] = obj.getCoachName();
			content[i][6] = String.valueOf(obj.getCoachMoney());
			content[i][7] = String.valueOf(obj.getAgent1Money());
			content[i][8] = String.valueOf(obj.getAgent2Money());
			content[i][9] = String.valueOf(obj.getAgent3Money());
			content[i][10] = String.valueOf(obj.getOperatorMoney());
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