package com.bootdo.coach.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

import com.bootdo.coach.api.wx.DateUtil;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.service.TabDeviceService;
import com.bootdo.coach.service.TabSeparateAccountService;
import com.bootdo.coach.vo.TabCoachVo;
import com.bootdo.common.excel.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.vo.TabAgentVo;
import com.bootdo.common.annotation.Log;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.AgentMD5;
import com.bootdo.common.utils.MD5Utils;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;
import com.bootdo.common.utils.R;
import com.bootdo.system.domain.UserDO;
import com.bootdo.system.service.UserService;

import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 下级代理商管理
 *
 * @author Administrator
 */
@RequestMapping("/coach/subAgent")
@Controller
public class SubAgentController extends BaseController {
    private String prefix = "coach/subAgent";
    @Autowired
    private TabAgentService agentService;
    @Autowired
    private UserService userService;

    @Autowired
    private TabDeviceService tabDeviceService;

    @Autowired
    private TabSeparateAccountService tabSeparateAccountService;

    @Autowired
    private TabCoachService coachService;

    //下级代理商=============>>>开始

    @RequiresPermissions("coach:subAgent:subAgent")
    @GetMapping("subAgent")
    String subAgent(Model model) {
        return prefix + "/subAgent";
    }

    @ApiOperation(value = "获取下级代理商列表", notes = "")
    @ResponseBody
    @GetMapping("/subAgentList")
    @RequiresPermissions("coach:subAgent:subAgent")
    public PageUtils subAgentList(@RequestParam Map<String, Object> params) {
        // 模糊查询
        if (params != null && params.containsKey("agentName") && !"".equals(params.get("agentName").toString())) {
            params.put("agentName", "%" + params.get("agentName").toString() + "%");
        }
        if (params != null && params.containsKey("tel") && !"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
        int total = 0;
        List<TabAgentVo> subAgentList = new ArrayList<TabAgentVo>();
        // 找出当前用户的代理商
        String bg_account = getUser().getUsername();
        TabAgent curAgent = agentService.findAgent(bg_account, null);
        Set<Long> agentIdList = new HashSet<Long>();
        Query query = new Query(params);
        if (curAgent == null) {
            PageUtils pageUtil = new PageUtils(subAgentList, total);
            return pageUtil;
        } else {
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
        // 查询列表数据
        query.put("agentIdList", agentIdList);
        subAgentList = agentService.list(query);
        for (int i = 0; i < subAgentList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("level", subAgentList.get(i).getLevel());
            map.put("agentId", subAgentList.get(i).getAgentId());
            BigDecimal bigDecimal = tabSeparateAccountService.finAgentTotalRevenue(map);
            if (subAgentList.get(i).getLevel() == 0) {
                subAgentList.get(i).setAgentOneProfit(bigDecimal);
            } else if (subAgentList.get(i).getLevel() == 1) {
                subAgentList.get(i).setAgentTwoProfit(bigDecimal);

            } else if (subAgentList.get(i).getLevel() == 2) {
                subAgentList.get(i).setAgentThreeProfit(bigDecimal);
            }
        }
        total = agentService.count(query);
        PageUtils pageUtil = new PageUtils(subAgentList, total);
        return pageUtil;
    }

    @RequiresPermissions("coach:subAgent:subAgentAdd")
    @Log("添加下级代理商")
    @GetMapping("/subAgentAdd")
    String subAgentAdd() {
        return prefix + "/subAgentAdd";
    }

    @RequiresPermissions("coach:subAgent:subAgentEdit")
    @Log("编辑下级代理商")
    @GetMapping("/subAgentEdit/{agentId}")
    String subAgentEdit(Model model, @PathVariable("agentId") Long agentId) {
        TabAgent agent = agentService.selectByPrimaryKey(agentId);
        model.addAttribute("agent", agent);
        return prefix + "/subAgentEdit";
    }

    @RequiresPermissions("coach:subAgent:subAgentAdd")
    @Log("保存下级代理商")
    @PostMapping("/saveSubAgent")
    @ResponseBody
    R saveSubAgent(TabAgent agent) {
        // 找出当前用户的代理商
        String bg_account = getUser().getUsername();
        TabAgent curAgent = agentService.findAgent(bg_account, null);
        if (curAgent == null) {
            return R.error("当前登录用户不是代理商");
        }
        if (curAgent.getLevel().intValue() == 2) {
            return R.error("当前登录用户是三级代理商，不能增加下级代理商");
        }

        agent.setHigherAgentId(curAgent.getAgentId());
        agent.setLevel(curAgent.getLevel().intValue() + 1);// 设置级别
        String oldpwd = agent.getBgPassword();
        agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));

        if (agentService.insertSelective(agent) > 0) {
            addUser(agent.getAgentName(), agent.getBgAccount(), oldpwd);
            return R.ok();
        }
        return R.error();
    }

    @RequiresPermissions("coach:subAgent:subAgentEdit")
    @Log("更新下级代理商")
    @PostMapping("/updateSubAgent")
    @ResponseBody
    R updateSubAgent(TabAgent agent) {

        TabAgent temp = agentService.selectByPrimaryKey(agent.getAgentId());
        if (!temp.getBgPassword().equals(agent.getBgPassword())) {
            agent.setBgPassword(AgentMD5.md5(agent.getBgPassword()));
        }

        if (agentService.updateByPrimaryKeySelective(agent) > 0) {
            return R.ok();
        }
        return R.error();
    }

    /**
     * 查询代理商名称是否存在
     *
     * @param params
     * @return
     */
    @PostMapping("/exitAgentName")
    @ResponseBody
    boolean exitAgentName(@RequestParam Map<String, Object> params) {
        // 存在，不通过，false
        return !agentService.exitAgentName(params);
    }

    /**
     * 查询后台登录账号是否存在
     *
     * @param params
     * @return
     */
    @PostMapping("/exitBgAccount")
    @ResponseBody
    boolean exitBgAccount(@RequestParam Map<String, Object> params) {
        // 存在，不通过，false
        return !agentService.exitBgAccount(params);
    }

    /**
     * 增加后台账号
     *
     * @param agentName
     * @param userName
     * @param password
     */
    private void addUser(String agentName, String userName, String password) {
        UserDO user = new UserDO();
        user.setUsername(userName);
        user.setPassword(MD5Utils.encrypt(userName, password));
        user.setName(agentName);
        List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(60l);
        user.setRoleIds(roleIds);
        user.setStatus(1);
        //{"deptName":"","email":"lifaqiu@163.com","name":"测试","password":"25bb7c42604b0e7aba7bcae50e7762a9","roleIds":[60],"status":1,"username":"test"}
        userService.save(user);
    }

    @RequiresPermissions("coach:subAgent:subDetails")
    @Log("获取代理id")
    @GetMapping("/subDetails/{agentId}")
    String subDetails(Model model, @PathVariable("agentId") Long agentId) {
        TabAgent agent = new TabAgent();
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        List<TabCoachVo> coachList = coachService.list(params);
        Integer countAgentDevice = 0;
        for (int i = 0; i < coachList.size(); i++) {
            Long coachId = coachList.get(i).getCoachId();
            //统计出教练名下的设备数量
            int countCoach = tabDeviceService.countCoachDevice(coachId);
            countAgentDevice = countAgentDevice + countCoach;
        }
        agent.setAgentId(agentId);
        agent.setCountAgentDevice(countAgentDevice);
        model.addAttribute("agent", agent);
        return prefix + "/subDetails";
    }

    @RequiresPermissions("coach:subAgent:subDetails")
    @GetMapping("/details")
    @ResponseBody
    @Log("查询代理商名下的教练详情")
    PageUtils details(@RequestParam Map<String, Object> params) {
        // 模糊查询
        if (params != null && params.containsKey("coachName") && !"".equals(params.get("coachName").toString())) {
            params.put("coachName", "%" + params.get("coachName").toString() + "%");
        }
        if (params != null && params.containsKey("tel") && !"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
        Query query = new Query(params);
        List<TabCoachVo> coachList = coachService.list(query);
        for (int i = 0; i < coachList.size(); i++) {
            Long coachId = coachList.get(i).getCoachId();
            //统计出教练名下的设备数量
            int countCoach = tabDeviceService.countCoachDevice(coachId);
            //添加到list集合里面去
            coachList.get(i).setCountDevice(countCoach);
            BigDecimal dayRevenue = tabSeparateAccountService.findDayRevenue(coachId);
            coachList.get(i).setDayRevenue(dayRevenue);
            //查询近7天的收益
            BigDecimal sevenRevenue = tabSeparateAccountService.findSevenRevenue(coachId);
            coachList.get(i).setSevenRevenue(sevenRevenue);
            //查询本月的收益
            BigDecimal monthRevenue = tabSeparateAccountService.findMonthRevenue(coachId);
            coachList.get(i).setMonthRevenue(monthRevenue);
            //查询总收益
            BigDecimal totalRevenue = tabSeparateAccountService.findTotalRevenue(coachId);
            coachList.get(i).setTotalRevenue(totalRevenue);
        }
        int total = coachService.count(query);
        PageUtils pageUtil = new PageUtils(coachList, total);
        return pageUtil;
    }
    /**
     * 导出报表
     *
     * @return
     */
    @RequestMapping(value = "/subAgentExcel")
    @ResponseBody
    @RequiresPermissions("coach:subAgent:subAgentExcel")
    public void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取数据
        List<TabAgentVo> subAgentList = new ArrayList<TabAgentVo>();
        // 找出当前用户的代理商
        String bg_account = getUser().getUsername();
        TabAgent curAgent = agentService.findAgent(bg_account, null);
        Set<Long> agentIdList = new HashSet<Long>();
        Map<String, Object> params = new HashMap<>();
        if (curAgent == null) {
        } else {
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
        // 查询列表数据
        params.put("agentIdList", agentIdList);
        subAgentList = agentService.list(params);
        for (int i = 0; i < subAgentList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("level", subAgentList.get(i).getLevel());
            map.put("agentId", subAgentList.get(i).getAgentId());
            BigDecimal bigDecimal = tabSeparateAccountService.finAgentTotalRevenue(map);
            if (subAgentList.get(i).getLevel() == 0) {
                subAgentList.get(i).setAgentOneProfit(bigDecimal);
            } else if (subAgentList.get(i).getLevel() == 1) {
                subAgentList.get(i).setAgentTwoProfit(bigDecimal);

            } else if (subAgentList.get(i).getLevel() == 2) {
                subAgentList.get(i).setAgentThreeProfit(bigDecimal);
            }
        }
        //excel标题
        String[] title = {"id", "代理商名称", "联系人", "联系电话", "微信号", "分销比例", "总收益", "备注", "状态", "是否离职"};
        //excel文件名
        String fileName = "下级代理商表" + System.currentTimeMillis() + ".xls";
        //sheet名currentTimeMillis
        String sheetName = "下级代理商表";
        String[][] content = new String[subAgentList.size()][];
        for (int i = 0; i < subAgentList.size(); i++) {
            content[i] = new String[title.length];
            TabAgentVo obj = subAgentList.get(i);
            content[i][0] = String.valueOf(obj.getAgentId());
            content[i][1] = obj.getAgentName();
            content[i][2] = obj.getContacts();
            content[i][3] = obj.getTel();
            content[i][4] = obj.getAccount();
            content[i][5] = String.valueOf(obj.getDivide()) + "%";
            if (obj.getLevel() == 0) {
                content[i][6] = String.valueOf(obj.getAgentOneProfit());
            } else if (obj.getLevel() == 1) {
                content[i][6] = String.valueOf(obj.getAgentTwoProfit());
            } else if (obj.getLevel() == 2) {
                content[i][6] = String.valueOf(obj.getAgentThreeProfit());
            }
            content[i][7] = obj.getRemarks();
            if (obj.getStatus() == 0) {
                content[i][8] = "禁用";
            } else if (obj.getStatus() == 1) {
                content[i][8] = "正常";
            }
            content[i][9] = String.valueOf(obj.getIsDelete());
            if (obj.getIsDelete() == 1) {
                content[i][9] = "是";
            } else if (obj.getIsDelete() == 0) {
                content[i][9] = "否";
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
