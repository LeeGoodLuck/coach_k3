package com.bootdo.coach.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

import com.bootdo.coach.domain.TabAgent;
import com.bootdo.coach.domain.TabSeparateAccount;
import com.bootdo.coach.service.TabDeviceService;
import com.bootdo.coach.service.TabSeparateAccountService;
import com.bootdo.coach.vo.TabSeparateAccountVo;
import com.bootdo.common.excel.ExcelUtil;
import com.bootdo.system.domain.UserDO;
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

import com.bootdo.coach.domain.TabCoach;
import com.bootdo.coach.domain.TabTopAgent;
import com.bootdo.coach.result.ResponseEntityUtil;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.vo.TabAgentVo;
import com.bootdo.coach.vo.TabCoachVo;
import com.bootdo.common.annotation.Log;
import com.bootdo.common.controller.BaseController;
import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.Query;
import com.bootdo.common.utils.R;

import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 教练端
 *
 * @author Administrator
 */
@RequestMapping("/coach/coach")
@Controller
public class CoachController extends BaseController {
    private String prefix = "coach/coach";
    @Autowired
    private TabAgentService agentService;

    @Autowired
    private TabCoachService coachService;

    @Autowired
    private TabDeviceService tabDeviceService;

    @Autowired
    private TabSeparateAccountService tabSeparateAccountService;

    //教练=============>>>开始

    @RequiresPermissions("coach:coach:list")
    @GetMapping("list")
    String coach(Model model) {
        return prefix + "/coach";
    }

    @ApiOperation(value = "获取教练列表", notes = "")
    @ResponseBody
    @GetMapping("/coachList")
    @RequiresPermissions("coach:coach:list")
    public PageUtils coachList(@RequestParam Map<String, Object> params) {
        // 查询列表数据
        if (params != null && params.containsKey("coachName") && !"".equals(params.get("coachName").toString())) {
            params.put("coachName", "%" + params.get("coachName").toString() + "%");
        }
        if (params != null && params.containsKey("tel") && !"".equals(params.get("tel").toString())) {
            params.put("tel", "%" + params.get("tel").toString() + "%");
        }
        //获取当前登录用户
        UserDO userDO = getUser();
        //查询当前登录角色
        Long role = (userDO.getRoleIds()).get(0);
        Set<Long> agentIdList = new HashSet<Long>();
        Set<Long> coachIdList = new HashSet<Long>();
        if (role != 1) {
            //查询代理表的代理数据
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
                // 找出代理商下的教练
                coachIdList = coachService.findCoachIdList(agentIdList);
            }
            params.put("coachIdList", coachIdList);
        }
        Query query = new Query(params);
        List<TabCoachVo> coachList = coachService.list(query);
        for (int i = 0; i < coachList.size(); i++) {
            Long coachId = coachList.get(i).getCoachId();
            //统计出教练名下的设备数量
            int countCoach = tabDeviceService.countCoachDevice(coachId);
            //添加到list集合里面去
            coachList.get(i).setCountDevice(countCoach);
            //查询当日的收益   教练ID:coachId
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

    @RequiresPermissions("coach:coach:coachAdd")
    @Log("添加教练")
    @GetMapping("/coachAdd")
    String coachAdd(Model model) {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("status", 1);
        List<TabAgentVo> agentList = agentService.list(query);
        if (agentList == null) {
            agentList = new ArrayList<TabAgentVo>();
        }
        TabAgentVo one = new TabAgentVo();
        one.setAgentId(0l);
        one.setAgentName("请选择");
        agentList.add(0, one);
        model.addAttribute("agentList", agentList);
        return prefix + "/coachAdd";
    }

    @RequiresPermissions("coach:coach:coachEdit")
    @Log("编辑教练")
    @GetMapping("/coachEdit/{coachId}")
    String coachEdit(Model model, @PathVariable("coachId") Long coachId) {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("status", 1);
        List<TabAgentVo> agentList = agentService.list(query);
        if (agentList == null) {
            agentList = new ArrayList<TabAgentVo>();
        }
        TabAgentVo one = new TabAgentVo();
        one.setAgentId(0l);
        one.setAgentName("请选择");
        agentList.add(0, one);
        model.addAttribute("agentList", agentList);
        TabCoach coach = coachService.selectByPrimaryKey(coachId);
        model.addAttribute("coach", coach);
        return prefix + "/coachEdit";
    }

    @RequiresPermissions("coach:coach:coachAdd")
    @Log("保存教练")
    @PostMapping("/saveCoach")
    @ResponseBody
    R saveCoach(TabCoach coach) {
        if (coach.getDivideType() == 1) {
            if (coach.getDivide().compareTo(coach.getChargeRate()) > -1) {
                return R.error("请输入正确的固定金额");
            }
        }
        coach.setCreateTime(new Date());
        if (coach.getAgentId() == null || coach.getAgentId() == 0l) {
            return R.error("请选择代理商");
        }
        if (coachService.insertSelective(coach) > 0) {
            return R.ok();
        }
        return R.error();
    }

    @RequiresPermissions("coach:coach:coachEdit")
    @Log("更新教练")
    @PostMapping("/updateCoach")
    @ResponseBody
    R updatecoach(TabCoach coach) {
        if (coach.getDivideType() == 1) {
            if (coach.getDivide().compareTo(coach.getChargeRate()) > -1) {
                return R.error("请输入正确的固定金额");
            }
        }
        if (coach.getAgentId() == null || coach.getAgentId() == 0l) {
            return R.error("请选择代理商");
        }
        if (coachService.updateByPrimaryKeySelective(coach) > 0) {
            return R.ok();
        }
        return R.error();
    }

    @RequiresPermissions("coach:coach:coachRemove")
    @Log("删除教练")
    @PostMapping("/removeCoach")
    @ResponseBody
    R removeCoach(Long coachId) {
        if (coachService.deleteByPrimaryKey(coachId) > 0) {
            return R.ok();
        }
        return R.error();
    }

    //和删除教练使用同一个权限
    @RequiresPermissions("coach:coach:coachRemove")
    @Log("教练解绑")
    @PostMapping("/untyingCoach")
    @ResponseBody
    R untyingCoach(Long coachId) {
        TabCoach coach = coachService.selectByPrimaryKey(coachId);
        if (coach != null) {
            coach.setOpenid("");
            coach.setUnionid("");
            coach.setWechatName("");
            if (coachService.updateByPrimaryKeySelective(coach) > 0) {
                return R.ok();
            }
        }

        return R.error();
    }

    @PostMapping("/exitCoachName")
    @ResponseBody
    boolean exitCoachName(@RequestParam Map<String, Object> params) {
        // 存在，不通过，false
        return !coachService.exitCoachName(params);
    }

    @PostMapping("/exitAccount")
    @ResponseBody
    boolean exitAccount(@RequestParam Map<String, Object> params) {
        TabCoach coach = coachService.findCoachByAccount(params.get("account").toString());
        if (coach != null) {
            return false;
        }
        return true;
    }

    //教练=============>>>结束

    @RequiresPermissions("coach:coach:coachRanking")
    @GetMapping("coachRanking")
    String coachRanking(Model model) {
        return prefix + "/coachRanking";
    }

    @RequestMapping(value = "/coachListRanking")
    @ResponseBody
    @Log("查询教练排行榜")
    @RequiresPermissions("coach:coach:coachRanking")
    PageUtils coachListRanking() {
        //获取当前登录用户
        UserDO userDO = getUser();
        //查询当前登录角色
        Long role = (userDO.getRoleIds()).get(0);
        List<TabSeparateAccount> coachMoney = null;
        if (role == 1) {
            coachMoney = tabSeparateAccountService.wholeMoney();
        } else {
            //查询代理表的代理数据
            TabAgent tabAgent = agentService.findAgentByTel(userDO.getUsername());
            if (tabAgent != null) {
                ;//拿到代理级别
                if (tabAgent.getLevel() == 0) {
                    //一级代理
                    coachMoney = tabSeparateAccountService.oneAgentMoney(tabAgent.getAgentId());
                } else if (tabAgent.getLevel() == 1) {
                    //二级代理
                    coachMoney = tabSeparateAccountService.twoAgentMoney(tabAgent.getAgentId());
                } else if (tabAgent.getLevel() == 2) {
                    //三级代理
                    coachMoney = tabSeparateAccountService.threeAgentMoney(tabAgent.getAgentId());
                }
            }
        }
        PageUtils pageUtil = new PageUtils(coachMoney, 0);
        return pageUtil;
    }

    @RequestMapping(value = "/timeRankingTypeId/{typeId}")
    @Log("按时间查询教练排行榜")
    @RequiresPermissions("coach:coach:coachRanking")
    String timeRankingtypeId(Model model, @PathVariable("typeId") String typeId) {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("typeId", typeId);
        model.addAttribute("query", query);
        return prefix + "/coachTimeRanking";
    }

    @RequestMapping(value = "/timeRanking")
    @ResponseBody
    @Log("按时间查询教练排行榜")
    @RequiresPermissions("coach:coach:coachRanking")
    PageUtils timeRanking(Long typeId) {
        //获取当前登录用户
        UserDO userDO = getUser();
        //查询当前登录角色
        Long role = (userDO.getRoleIds()).get(0);
        List<TabSeparateAccount> coachMoney = null;
        if (role == 1) {
            coachMoney = tabSeparateAccountService.timeWholeMoney(typeId);
        } else {
            //查询代理表的代理数据
            TabAgent tabAgent = agentService.findAgentByTel(userDO.getUsername());
            if (tabAgent != null) {
                ;//拿到代理级别
                TabSeparateAccountVo account = new TabSeparateAccountVo();
                account.setAgentId(tabAgent.getAgentId());
                account.setTypeId(typeId);
                account.setLevel(tabAgent.getLevel());
                coachMoney = tabSeparateAccountService.timeAgentMoney(account);
            }
        }
        PageUtils pageUtil = new PageUtils(coachMoney, 0);
        return pageUtil;
    }
    /**
     * 导出报表
     *
     * @return
     */
    @RequestMapping(value = "/coachExcel")
    @ResponseBody
    @RequiresPermissions("coach:coach:coachExcel")
    public void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> params = new HashMap<>();
        //获取当前登录用户
        UserDO userDO = getUser();
        //查询当前登录角色
        Long role = (userDO.getRoleIds()).get(0);
        Set<Long> agentIdList = new HashSet<Long>();
        Set<Long> coachIdList = new HashSet<Long>();
        if (role != 1) {
            //查询代理表的代理数据
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
                // 找出代理商下的教练
                coachIdList = coachService.findCoachIdList(agentIdList);
            }
            params.put("coachIdList", coachIdList);
        }
        List<TabCoachVo> coachList = coachService.list(params);
        for (int i = 0; i < coachList.size(); i++) {
            Long coachId = coachList.get(i).getCoachId();
            //统计出教练名下的设备数量
            int countCoach = tabDeviceService.countCoachDevice(coachId);
            //添加到list集合里面去
            coachList.get(i).setCountDevice(countCoach);
            //查询当日的收益   教练ID:coachId
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

        //excel标题
        String[] title = {"id", "名称", "联系电话", "微信号", "分销类型","分销", "代理商",
                "微信名", "设备统计", "今日收益", "七天收益", "本月收益", "总收益", "状态"};
        //excel文件名
        String fileName = "教练表" + System.currentTimeMillis() + ".xls";
        //sheet名currentTimeMillis
        String sheetName = "教练表";
        String[][] content = new String[coachList.size()][];
        for (int i = 0; i < coachList.size(); i++) {
            content[i] = new String[title.length];
            TabCoachVo obj = coachList.get(i);
            content[i][0] = String.valueOf(obj.getCoachId());
            content[i][1] = obj.getCoachName();
            content[i][2] = obj.getTel();
            content[i][3] = obj.getAccount();
            content[i][4] = String.valueOf(obj.getDivideType());
            content[i][5] = String.valueOf(obj.getDivide())+"%";
            content[i][6] = obj.getAgentName();
            content[i][7] = obj.getWechatName();
            content[i][8] = String.valueOf(obj.getCountDevice());
            content[i][9] =String.valueOf(obj.getDayRevenue());
            content[i][10] =String.valueOf(obj.getSevenRevenue());
            content[i][11] =String.valueOf(obj.getMonthRevenue());
            content[i][12] =String.valueOf(obj.getTotalRevenue());
            if (obj.getStatus() == 0) {
                content[i][13] = "禁用";
            } else if (obj.getStatus() == 1) {
                content[i][13] = "正常";
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