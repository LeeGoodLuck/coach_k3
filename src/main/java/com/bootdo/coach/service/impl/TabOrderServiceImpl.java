package com.bootdo.coach.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bootdo.coach.api.wx.*;
import com.bootdo.coach.dao.*;
import com.bootdo.coach.dao.ext.*;
import com.bootdo.coach.domain.*;
import com.bootdo.coach.service.TabAgentService;
import com.bootdo.coach.service.TabCoachService;
import com.bootdo.coach.service.TabOrderService;
import com.bootdo.coach.service.TabTopAgentService;
import com.bootdo.coach.vo.TabOrderVo;
import com.bootdo.common.utils.DateUtils;
import com.github.pagehelper.util.StringUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.*;

@Service
public class TabOrderServiceImpl implements TabOrderService {
    private static final Logger logger = LoggerFactory.getLogger(TabOrderServiceImpl.class);
    @Autowired
    private TabOrderMapper mapper;

    @Autowired
    private TabTestMapper testMapper;

    @Autowired
    private TabCoachMapper coachMapper;

    @Autowired
    private TabSeparateAccountMapper separateAccountMapper;

    @Autowired
    private ExtTabSeparateAccountMapper extSeparateAccountMapper;

    @Autowired
    private ExtTabTopAgentAccountMapper extTabTopAgentAccountMapper;

    @Autowired
    private TabTopAgentAccountMapper topAgentAccountMapper;

    @Autowired
    private ExtTabTopAgentMapper extTopAgentMapper;

    @Autowired
    private TabAgentMapper agentMapper;

    @Autowired
    private ExtTabOrderMapper extMapper;
    @Autowired
    private TabCoachService coachService;
    @Autowired
    private TabAgentService agentService;
    @Autowired
    private TabTopAgentService topAgentService;
    @Autowired
    private TabWithdrawApplyMapper withdrawApplyMapper;
    @Autowired
    private ExtTabCoachMapper extTabCoachMapper;
    @Autowired
    private ExtTabAgentMapper extTabAgentMapper;

    /**
     * 运营商的账号
     **/
    @Value("${operator.openid}")
    private String operatorOpenid;

    /**
     * 运营商的账号
     **/
    @Value("${operator.name}")
    private String operatorName;

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

    @Override
    public int deleteByPrimaryKey(Long orderId) {
        return mapper.deleteByPrimaryKey(orderId);
    }

    @Override
    public int insert(TabOrder record) {
        return mapper.insert(record);
    }

    @Override
    public int insertSelective(TabOrder record) {
        return mapper.insertSelective(record);
    }

    @Override
    public TabOrder selectByPrimaryKey(Long orderId) {
        return mapper.selectByPrimaryKey(orderId);
    }

    @Override
    public int updateByPrimaryKeySelective(TabOrder record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(TabOrder record) {
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public TabOrder findUnPayOrderByStudentId(Long student_id) {

        return extMapper.findUnPayOrderByStudentId(student_id);
    }

    @Override
    public int queryOrderListCnt(Long student_id) {
        return extMapper.queryOrderListCnt(student_id);
    }

    @Override
    public List<Map<String, Object>> queryOrderList(Map<String, Object> paramMap) {

        return extMapper.queryOrderList(paramMap);
    }

    @Override
    public TabOrder selectByOrderNo(String order_no) {
        return extMapper.selectByOrderNo(order_no);
    }

    @Override
    public void dealOrder(TabOrder order, String transaction_id) throws Exception {
        int dbResult = 0;
        // 修改订单状态
        order.setTransactionId(transaction_id);
        order.setPayState(1);
        order.setPayTime(new Date());
        dbResult = mapper.updateByPrimaryKeySelective(order);
        if (dbResult <= 0) {
            throw new RuntimeException("修改订单信息失败，还没调用微信分账接口");
        }
        logger.info("修改订单信息成功");
        // 修改tab_test支付状态
        TabTest test = testMapper.selectByPrimaryKey(order.getTestId());
        if (test != null) {
            test.setPayStatus(1);
            test.setPayWay(1);
            dbResult = testMapper.updateByPrimaryKeySelective(test);
            if (dbResult <= 0) {
                throw new RuntimeException("修改tab_test支付状态失败，还没调用微信分账接口");
            }
            logger.info("修改tab_test支付状态成功");
        }

        TabCoach coach = coachMapper.selectByPrimaryKey(order.getCoachId());
        // 教练的代理
        TabAgent agent = agentMapper.selectByPrimaryKey(coach.getAgentId());
        TabAgent agent0 = null;
        TabAgent agent1 = null;
        TabAgent agent2 = null;

        BigDecimal mul = new BigDecimal(100);
        BigDecimal agent_1_divide = new BigDecimal(0.00);
        BigDecimal agent_2_divide = new BigDecimal(0.00);
        BigDecimal agent_3_divide = new BigDecimal(0.00);
        //0:一级，1：二级：2：三级
        switch (agent.getLevel().intValue()) {
            case 0:
                // agent一级代理
                agent0 = agent;
                agent_1_divide = agent0.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                break;
            case 1:
                // agent二级代理
                agent1 = agent;
                agent_2_divide = agent1.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                // 找一级代理
                agent0 = agentMapper.selectByPrimaryKey(agent1.getHigherAgentId());
                agent_1_divide = agent0.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                break;
            case 2:
                // agent三级代理
                agent2 = agent;
                agent_3_divide = agent2.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                // 找二级代理
                agent1 = agentMapper.selectByPrimaryKey(agent2.getHigherAgentId());
                agent_2_divide = agent1.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                // 找一级代理
                agent0 = agentMapper.selectByPrimaryKey(agent1.getHigherAgentId());
                agent_1_divide = agent0.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                break;

            default:
                break;
        }

        // 分账总金额
        BigDecimal totalMoney = order.getTestMoney();

        // 分账
        /**
         顶级代理：30%
         一级代理：20%
         二级代理：30%
         三级代理：40%
         教练：40%
         如果一次练车60元
         教练：60*40%
         运营商：60*(1-40%)*(1-20%)
         顶级代理：60*(1-40%)*(1-20%)*30%
         一级代理60*(1-40%)*20%*(1-30%)
         二级代理60*(1-40%)*20%*30%(1-40%)
         三级代理60*(1-40%)*20%*30%*40%

         */
        BigDecimal bd1 = new BigDecimal(1);
        BigDecimal agent1Money = null;//一级代理分成
        BigDecimal agent2Money = null;//二级代理分成
        BigDecimal agent3Money = null;//三级代理分成
        BigDecimal coachMoney = null;
        BigDecimal operatorMoney = null;
        if (coach.getDivideType() == 0) {
            // 教练获取的分成=总金额*教练的分销比例
            BigDecimal coach_devide = coach.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
            coachMoney = totalMoney.multiply(coach_devide).setScale(2, BigDecimal.ROUND_DOWN);
            if (agent0 != null) {
                //一级代理分成
                agent1Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(bd1.subtract(agent_2_divide)).setScale(2, BigDecimal.ROUND_DOWN);
            }
            if (agent1 != null) {
                //二级代理分成
                agent2Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(bd1.subtract(agent_3_divide)).setScale(2, BigDecimal.ROUND_DOWN);
            }
            if (agent2 != null) {
                //三级代理分成
                agent3Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(agent_3_divide).setScale(2, BigDecimal.ROUND_DOWN);
            }
            //写入tab_separate_account
            //运营商：60*(1-40%)*(1-20%)
            operatorMoney = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(bd1.subtract(agent_1_divide)).setScale(2, BigDecimal.ROUND_DOWN);
        } else if (coach.getDivideType() == 1) {
            //教练分成=总金额-设置的固定金额
            //教练分成=（费率-固额）/费率*总分帐的金额
            coachMoney = coach.getChargeRate().subtract(coach.getDivide());
            coachMoney = coachMoney.divide(coach.getChargeRate(),2, BigDecimal.ROUND_HALF_UP);
            coachMoney = coachMoney.multiply(totalMoney);
            if (agent0 != null) {
                //一级代理分成
                agent1Money = (totalMoney.subtract(coachMoney)).multiply(agent_1_divide).multiply(bd1.subtract(agent_2_divide)).setScale(2, BigDecimal.ROUND_DOWN);
            }
            if (agent1 != null) {
                //二级代理分成
                agent2Money = (totalMoney.subtract(coachMoney)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(bd1.subtract(agent_3_divide)).setScale(2, BigDecimal.ROUND_DOWN);
            }
            if (agent2 != null) {
                //三级代理分成
                agent3Money = (totalMoney.subtract(coachMoney)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(agent_3_divide).setScale(2, BigDecimal.ROUND_DOWN);
            }
            //写入tab_separate_account
            //运营商：60*(1-40%)*(1-20%)
            operatorMoney = (totalMoney.subtract(coachMoney)).multiply(bd1.subtract(agent_1_divide)).setScale(2, BigDecimal.ROUND_DOWN);

        }

        TabSeparateAccount separateAccount = new TabSeparateAccount();

        if (agent0 != null) {
            separateAccount.setAgent1Id(agent0.getAgentId());
        }
        if (agent1 != null) {
            separateAccount.setAgent2Id(agent1.getAgentId());
        }
        if (agent2 != null) {
            separateAccount.setAgent3Id(agent2.getAgentId());
        }

        separateAccount.setAgent1Money(agent1Money);
        separateAccount.setAgent2Money(agent2Money);
        separateAccount.setAgent3Money(agent3Money);
        separateAccount.setCoachId(coach.getCoachId());
        separateAccount.setCoachMoney(coachMoney);
        separateAccount.setCoachName(coach.getCoachName());
        separateAccount.setCreateTime(new Date());
        separateAccount.setOperatorMoney(operatorMoney);
        separateAccount.setOrderId(order.getOrderId());
        separateAccount.setPlateNumber(order.getPlateNumber());
        separateAccount.setStudentId(order.getStudentId());
        separateAccount.setStudentName(order.getStudentName());
        separateAccount.setTestDuration(order.getTestDuration());
        separateAccount.setTestId(order.getTestId());
        separateAccount.setTestMoney(order.getTestMoney());
        separateAccount.setTestTime(order.getTestTime());
        dbResult = extSeparateAccountMapper.insertSelectiveAndReturnId(separateAccount);
        if (dbResult <= 0) {
            throw new RuntimeException("增加tab_separate_account分账记录失败，还没调用微信分账接口");
        }
        logger.info("增加tab_separate_account分账记录成功");

        JSONObject result = null;
        // 运营商分账，失败就回写状态
        String operator_separate_order_no = createOrderNo();
        result = transfers(operator_separate_order_no, operatorMoney, operatorOpenid, "NO_CHECK", operatorName);
        separateAccount.setOperatorSeparateOrderNo(operator_separate_order_no);
        separateAccount.setOperatorSeparateStatus(result.getIntValue("code"));
        separateAccount.setOperatorSeparateRemark(result.getString("msg"));
        separateAccount.setOperatorSeparatePaymentNo(result.getString("payment_no"));
        dbResult = separateAccountMapper.updateByPrimaryKeySelective(separateAccount);
        if (dbResult <= 0) {
            logger.error("修改运营商分账信息失败，不回滚数据");
        } else {
            logger.info("修改运营商分账信息成功");
        }

        // 教练分账，失败就回写状态
        logger.info("开始给教练" + coach.getCoachName() + "分账");
        String coach_separate_order_no = createOrderNo();
        result = transfers(coach_separate_order_no, coachMoney, coach.getOpenid(), "NO_CHECK", coach.getCoachName());
        logger.info("教练分账结果：" + JSONObject.toJSONString(result));
        separateAccount.setCoachSeparateOrderNo(coach_separate_order_no);
        separateAccount.setCoachSeparateStatus(result.getIntValue("code"));
        separateAccount.setCoachSeparateRemark(result.getString("msg"));
        separateAccount.setCoachSeparatePaymentNo(result.getString("payment_no"));
        dbResult = separateAccountMapper.updateByPrimaryKeySelective(separateAccount);
        if (dbResult <= 0) {
            logger.error("修改教练分账信息失败，不回滚数据");
        } else {
            logger.info("修改教练分账信息成功");
        }
        if (result.getIntValue("code") != 1) {
            // 分账失败
            // 增加教练的amount_desirable可取金额
            if (coachService.addAmountDesirable(coach.getCoachId(), coachMoney)) {
                logger.info("增加教练的amount_desirable可取金额成功");
            } else {
                logger.error("增加教练的amount_desirable可取金额失败，不回滚数据");
            }
        }

        // 一级代理分账，失败就回写状态
        if (agent0 != null) {
            String agent_1_separate_order_no = createOrderNo();
            logger.info("开始给一级代理" + agent0.getAgentName() + "分账");
            result = transfers(agent_1_separate_order_no, agent1Money, agent0.getOpenid(), "NO_CHECK", agent0.getAgentName());
            logger.info("一级代理分账结果：" + JSONObject.toJSONString(result));
            separateAccount.setAgent1SeparateOrderNo(agent_1_separate_order_no);
            separateAccount.setAgent1SeparateStatus(result.getIntValue("code"));
            separateAccount.setAgent1SeparateRemark(result.getString("msg"));
            separateAccount.setAgent1SeparatePaymentNo(result.getString("payment_no"));
            dbResult = separateAccountMapper.updateByPrimaryKeySelective(separateAccount);
            if (dbResult <= 0) {
                logger.error("修改一级代理分账信息失败，不回滚数据");
            } else {
                logger.info("修改一级代理分账信息成功");
            }
            if (result.getIntValue("code") != 1) {
                // 分账失败
                // 增加agent0的amount_desirable可取金额
                if (agentService.addAmountDesirable(agent0.getAgentId(), agent1Money)) {
                    logger.info("增加一级代理的amount_desirable可取金额成功");
                } else {
                    logger.error("增加一级代理的amount_desirable可取金额失败，不回滚数据");
                }
            }
        }
        // 二级代理分账，失败就回写状态
        if (agent1 != null) {
            String agent_2_separate_order_no = createOrderNo();
            logger.info("开始给二级代理" + agent1.getAgentName() + "分账");
            result = transfers(agent_2_separate_order_no, agent2Money, agent1.getOpenid(), "NO_CHECK", agent1.getAgentName());
            logger.info("二级代理分账结果：" + JSONObject.toJSONString(result));
            separateAccount.setAgent2SeparateOrderNo(agent_2_separate_order_no);
            separateAccount.setAgent2SeparateStatus(result.getIntValue("code"));
            separateAccount.setAgent2SeparateRemark(result.getString("msg"));
            separateAccount.setAgent2SeparatePaymentNo(result.getString("payment_no"));
            dbResult = separateAccountMapper.updateByPrimaryKeySelective(separateAccount);
            if (dbResult <= 0) {
                logger.error("修改二级代理分账信息失败，不回滚数据");
            } else {
                logger.info("修改二级代理分账信息成功");
            }
            if (result.getIntValue("code") != 1) {
                // 分账失败
                // 增加agent0的amount_desirable可取金额
                if (agentService.addAmountDesirable(agent1.getAgentId(), agent2Money)) {
                    logger.info("增加二级代理的amount_desirable可取金额成功");
                } else {
                    logger.error("增加二级代理的amount_desirable可取金额失败，不回滚数据");
                }
            }
        }
        // 三级代理分账，失败就回写状态
        if (agent2 != null) {
            String agent_3_separate_order_no = createOrderNo();
            logger.info("开始给三级代理" + agent2.getAgentName() + "分账");
            result = transfers(agent_3_separate_order_no, agent3Money, agent2.getOpenid(), "NO_CHECK", agent2.getAgentName());
            logger.info("三级代理分账结果：" + JSONObject.toJSONString(result));
            separateAccount.setAgent3SeparateOrderNo(agent_3_separate_order_no);
            separateAccount.setAgent3SeparateStatus(result.getIntValue("code"));
            separateAccount.setAgent3SeparateRemark(result.getString("msg"));
            separateAccount.setAgent3SeparatePaymentNo(result.getString("payment_no"));
            dbResult = separateAccountMapper.updateByPrimaryKeySelective(separateAccount);
            if (dbResult <= 0) {
                logger.error("修改三级代理分账信息失败，不回滚数据");
            } else {
                logger.info("修改三级代理分账信息成功");
            }
            if (result.getIntValue("code") != 1) {
                // 分账失败
                // 增加agent0的amount_desirable可取金额
                if (agentService.addAmountDesirable(agent2.getAgentId(), agent3Money)) {
                    logger.info("增加三级代理的amount_desirable可取金额成功");
                } else {
                    logger.error("增加三级代理的amount_desirable可取金额失败，不回滚数据");
                }
            }
        }

        //顶级代理商分账tab_top_agent_account
        // 已经分出去多少了
        BigDecimal left_top_money = operatorMoney;
        // 找出所有的顶级代理
        List<TabTopAgent> topAgentList = extTopAgentMapper.findAllTopAgentList();
        if (topAgentList != null) {
            for (TabTopAgent tabTopAgent : topAgentList) {
                //顶级代理商的分成比例
                BigDecimal topAgentDevide = tabTopAgent.getDivide().divide(mul, 2, BigDecimal.ROUND_DOWN);
                BigDecimal separate_money = operatorMoney.multiply(topAgentDevide).setScale(2, BigDecimal.ROUND_DOWN);
                if (left_top_money.compareTo(separate_money) >= 0) {
                    //可以分
                    TabTopAgentAccount topAgentAccount = new TabTopAgentAccount();
                    topAgentAccount.setAccount(tabTopAgent.getAccount());
                    topAgentAccount.setCreateTime(new Date());
                    topAgentAccount.setOrderId(order.getOrderId());
                    topAgentAccount.setPlateNumber(order.getPlateNumber());
                    topAgentAccount.setSeparateMoney(separate_money);
                    topAgentAccount.setTestId(order.getTestId());
                    topAgentAccount.setTestMoney(order.getTestMoney());
                    topAgentAccount.setTestTime(order.getTestTime());
                    topAgentAccount.setTopAgentId(tabTopAgent.getTopAgentId());
                    topAgentAccount.setTopAgentName(tabTopAgent.getTopAgentName());
                    dbResult = extTabTopAgentAccountMapper.insertSelectiveAndReturnId(topAgentAccount);
                    if (dbResult <= 0) {
                        logger.error("处理顶级代理商得分账失败了，插入记录失败，继续处理下一个。" + JSONObject.toJSONString(tabTopAgent));
                        // 插入记录失败了
                        // 继续处理下一个
                        continue;
                    }
                    // 调用微信分账接口，失败就回写状态
                    String separate_order_no = createOrderNo();
                    logger.info("开始给顶级代理" + tabTopAgent.getTopAgentName() + "分账");
                    result = transfers(separate_order_no, separate_money, tabTopAgent.getOpenid(), "NO_CHECK", tabTopAgent.getTopAgentName());
                    logger.info("顶级代理分账结果：" + JSONObject.toJSONString(result));
                    topAgentAccount.setSeparateOrderNo(separate_order_no);
                    topAgentAccount.setSeparateStatus(result.getIntValue("code"));
                    topAgentAccount.setSeparateRemark(result.getString("msg"));
                    topAgentAccount.setSeparatePaymentNo(result.getString("payment_no"));
                    dbResult = topAgentAccountMapper.updateByPrimaryKeySelective(topAgentAccount);
                    if (dbResult <= 0) {
                        logger.error("修改顶级代理商分账信息失败，不回滚数据");
                    } else {
                        logger.info("修改顶级代理商分账信息成功");
                        // 修改剩余可分账的金额
                        left_top_money = left_top_money.subtract(separate_money).setScale(2, BigDecimal.ROUND_DOWN);
                    }
                    if (result.getIntValue("code") != 1) {
                        // 分账失败
                        // 增加顶级代理商的amount_desirable可取金额
                        Map<String, Object> paramMap = new HashMap<String, Object>();
                        paramMap.put("top_agent_id", tabTopAgent.getTopAgentId());
                        paramMap.put("amount_desirable", separate_money);
                        if (extTabAgentMapper.addAmountDesirable(paramMap) > 0) {
                            logger.info("增加顶级代理商的amount_desirable可取金额成功");
                        } else {
                            logger.error("增加顶级代理商的amount_desirable可取金额失败，不回滚数据");
                        }
                    }
                } else {
                    //不可以分
                    logger.error("处理顶级代理商得分账失败了，剩下得钱不够，继续处理下一个。" + JSONObject.toJSONString(tabTopAgent));
                }
            }
        }
    }

    public static void main(String[] args) {
        BigDecimal mul = new BigDecimal(100);
        BigDecimal agent_1_divide = new BigDecimal(0.2);
        agent_1_divide = agent_1_divide.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal agent_2_divide = new BigDecimal(0.3);
        agent_2_divide = agent_2_divide.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal agent_3_divide = new BigDecimal(0.4);
        agent_3_divide = agent_3_divide.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 分账总金额
        BigDecimal totalMoney = new BigDecimal(1000);

        // 教练获取的分成
        BigDecimal coach_devide = new BigDecimal(0.4);
        coach_devide = coach_devide.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal coachMoney = totalMoney.multiply(coach_devide);
        coachMoney = coachMoney.setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal bd1 = new BigDecimal(1);
        BigDecimal agent1Money = null;//一级代理分成
        BigDecimal agent2Money = null;//二级代理分成
        BigDecimal agent3Money = null;//三级代理分成

        //一级代理分成
        agent1Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(bd1.subtract(agent_2_divide));
        agent1Money = agent1Money.setScale(2, BigDecimal.ROUND_HALF_UP);
        //二级代理分成
        agent2Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(bd1.subtract(agent_3_divide));
        agent2Money = agent2Money.setScale(2, BigDecimal.ROUND_HALF_UP);
        //三级代理分成
        agent3Money = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(agent_1_divide).multiply(agent_2_divide).multiply(agent_3_divide);
        agent3Money = agent3Money.setScale(2, BigDecimal.ROUND_HALF_UP);

        //写入tab_separate_account
        //运营商：60*(1-40%)*(1-20%)
        BigDecimal operatorMoney = totalMoney.multiply(bd1.subtract(coach_devide)).multiply(bd1.subtract(agent_1_divide));
        operatorMoney = operatorMoney.setScale(2, BigDecimal.ROUND_HALF_UP);

        System.out.println("总考试金额：" + totalMoney);
        System.out.println("教练分成比例：" + coach_devide + "，分得：" + coachMoney);
        System.out.println("一级代理比例：" + agent_1_divide + "，分得：" + agent1Money);
        System.out.println("二级代理比例：" + agent_2_divide + "，分得：" + agent2Money);
        System.out.println("三级代理比例：" + agent_3_divide + "，分得：" + agent3Money);
        System.out.println("运营商，分得：" + operatorMoney);

        //顶级代理商分账tab_top_agent_account
        // 已经分出去多少了
        BigDecimal left_top_money = operatorMoney;
        // 找出所有的顶级代理
        List<TabTopAgent> topAgentList = new ArrayList<TabTopAgent>();
        TabTopAgent tabTopAgent = new TabTopAgent();
        tabTopAgent.setDivide(new BigDecimal(30));
        tabTopAgent.setTopAgentId(1l);
        TabTopAgent tabTopAgent1 = new TabTopAgent();
        tabTopAgent1.setTopAgentId(2l);
        tabTopAgent1.setDivide(new BigDecimal(40));
        TabTopAgent tabTopAgent2 = new TabTopAgent();
        tabTopAgent2.setTopAgentId(3l);
        tabTopAgent2.setDivide(new BigDecimal(31));
        topAgentList.add(tabTopAgent);
        topAgentList.add(tabTopAgent1);
        topAgentList.add(tabTopAgent2);
        if (topAgentList != null) {
            System.out.println("总共有顶级代理商：" + topAgentList.size() + "个，他们要划分金额：" + operatorMoney);
            for (TabTopAgent item : topAgentList) {
                //顶级代理商的分成比例
                BigDecimal topAgentDevide = item.getDivide().divide(mul, 2, BigDecimal.ROUND_HALF_UP);
                topAgentDevide = topAgentDevide.setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal separate_money = operatorMoney.multiply(topAgentDevide);
                separate_money = separate_money.setScale(2, BigDecimal.ROUND_HALF_UP);
                System.out.println("处理顶级代理商" + item.getTopAgentId() + "的分成：");
                System.out.println("分成比例：" + topAgentDevide + "，需要分得：" + separate_money);
                System.out.println("现有金额：" + left_top_money);
                if (left_top_money.compareTo(separate_money) >= 0) {
                    left_top_money = left_top_money.subtract(separate_money);
                    System.out.println("可以分，分了之后还剩下：" + left_top_money);
                } else {
                    System.out.println("不可再分，分成失败");
                }
            }
        }
    }

    /**
     * 微信分账接口，成功就返回微信付款单号
     *
     * @param orderNo
     * @param amount       企业付款金额
     * @param openid       支付给用户openid
     * @param check_name   NO_CHECK：不校验真实姓名，FORCE_CHECK：强校验真实姓名
     * @param re_user_name 收款用户真实姓名。 如果check_name设置为FORCE_CHECK，则必填用户真实姓名
     * @throws Exception
     */
    private JSONObject transfers(String orderNo, BigDecimal amount, String openid, String check_name, String re_user_name) {
        logger.info("分账参数，openid=" + openid + "，名字=" + re_user_name + ",金额：" + amount);
        JSONObject result = new JSONObject();
        result.put("payment_no", "");
        try {
            //单位为分
            amount = amount.multiply(new BigDecimal(100));
            //1.0 拼凑企业支付需要的参数
            String nonce_str = WeixinUtil.create_nonce_str(); //生成随机数
            String partner_trade_no = orderNo; //生成商户订单号
            String desc = "分账";   //企业付款操作说明信息。必填。
            String spbill_create_ip = InetAddress.getLocalHost().getHostAddress().toString();

            //2.0 生成map集合
            Map<String, String> packageParams = new HashMap<String, String>();
            packageParams.put("mch_appid", app_id);         //微信公众号的appid
            packageParams.put("mchid", mch_id);       //商务号
            packageParams.put("nonce_str", nonce_str);  //随机生成后数字，保证安全性

            packageParams.put("partner_trade_no", partner_trade_no); //生成商户订单号
            packageParams.put("openid", openid);            // 支付给用户openid
            packageParams.put("check_name", check_name);    //是否验证真实姓名呢
            packageParams.put("re_user_name", re_user_name);//收款用户姓名
            packageParams.put("amount", String.valueOf(amount.intValue()));  //企业付款金额，单位为分
            packageParams.put("desc", desc);                   //企业付款操作说明信息。必填。
            packageParams.put("spbill_create_ip", spbill_create_ip); //调用接口的机器Ip地址

            //3.0 生成自己的签名
            String sign = WXPayUtil.generateSignature(packageParams, api_key);

            //4.0 封装退款对象
            packageParams.put("sign", sign);

            //5.0将当前的map结合转化成xml格式
            String reuqestXml = WeixinUtil.map2Xmlstring(packageParams);

            //6.0获取需要发送的url地址
            String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; //获取退款的api接口

            CloseableHttpResponse response = HttpUtil.post(wxUrl, reuqestXml, true, mch_id);

            JSONObject jsonXML = new JSONObject();
            JSONObject json = new JSONObject();
            // resp返回的是XML
            String resp = EntityUtils.toString(response.getEntity(), "utf-8");
            logger.info("分账返回的数据：" + resp);
            json = TypeCoverUtil.xml2JSON(resp.getBytes());
            jsonXML = (JSONObject) json.get("xml");
            logger.info("jsonXML：" + JSONObject.toJSONString(jsonXML));
            if (!jsonXML.get("return_code").toString().contains("FAIL")) {
                if (jsonXML.get("result_code").toString().contains("SUCCESS")) {
                    result.put("code", 1);
                    result.put("msg", "付款成功");
                    result.put("payment_no", jsonXML.get("payment_no").toString());
                } else {
                    // 错误码信息，注意：出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果
                    if (jsonXML.get("err_code").toString().contains("SYSTEMERROR")) {
                        //请先调用查询接口，查看此次付款结果，如结果为不明确状态（如订单号不存在），请务必使用原商户订单号进行重试。
                        return gettransferinfo(orderNo, 1);
                    } else {
                        result.put("code", 2);
                        result.put("msg", jsonXML.get("err_code_des").toString());
                    }
                }
            } else {
                result.put("code", 2);
                result.put("msg", jsonXML.get("return_msg").toString());
            }
        } catch (Exception e) {
            result.put("code", 2);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 查询企业付款(付款成功返回true，付款失败返回false)
     *
     * @param orderNo    商户调用企业付款API时使用的商户订单号
     * @param queryCount 查询次数，查询3次之后不在查询
     * @throws Exception
     */
    private JSONObject gettransferinfo(String orderNo, int queryCount) throws Exception {
        JSONObject result = new JSONObject();
        result.put("payment_no", "");
        if (queryCount > 3) {
            logger.error("订单号：" + orderNo + "已经查询了3次");
            result.put("code", 3);
            result.put("msg", "订单号：" + orderNo + "已经查询了3次");
            return result;
        }
        queryCount++;
        String nonce_str = WeixinUtil.create_nonce_str(); //生成随机数
        String partner_trade_no = orderNo;
        Map<String, String> packageParams = new HashMap<String, String>();
        packageParams.put("mch_appid", app_id);         //微信公众号的appid
        packageParams.put("mchid", mch_id);       //商务号
        packageParams.put("nonce_str", nonce_str);  //随机生成后数字，保证安全性
        packageParams.put("partner_trade_no", partner_trade_no); //生成商户订单号
        //3.0 生成自己的签名
        String sign = WXPayUtil.generateSignature(packageParams, api_key);
        //4.0 封装退款对象
        packageParams.put("sign", sign);

        //5.0将当前的map结合转化成xml格式
        String reuqestXml = WeixinUtil.map2Xmlstring(packageParams);

        //6.0获取需要发送的url地址
        //查询企业付款
        String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo";

        JSONObject jsonXML = new JSONObject();
        JSONObject json = new JSONObject();
        // resp返回的是XML
        String resp = new HttpRequest().postData(wxUrl, reuqestXml, "utf-8").toString();
        logger.info("查询企业付款返回的数据：" + resp);
        json = TypeCoverUtil.xml2JSON(resp.getBytes());
        jsonXML = (JSONObject) json.get("xml");
        logger.info("jsonXML：" + JSONObject.toJSONString(jsonXML));
        if (!jsonXML.get("return_code").toString().contains("FAIL")) {
            if (jsonXML.get("result_code").toString().contains("SUCCESS")) {
                //jsonXML.get("reason").toString();//失败原因
                if (jsonXML.get("status").toString().contains("SUCCESS")) {
                    result.put("code", 1);
                    result.put("msg", "付款成功");
                    result.put("payment_no", jsonXML.get("detail_id").toString());
                    return result;
                }
                if (jsonXML.get("status").toString().contains("FAILED")) {
                    logger.error("查询订单号：" + orderNo + "企业付款接口结果，付款失败，原因：" + jsonXML.get("reason").toString());
                    result.put("code", 2);
                    result.put("msg", jsonXML.get("reason").toString());
                }
                //PROCESSING:处理中
                return gettransferinfo(orderNo, queryCount);
            } else {
                logger.error("查询订单号：" + orderNo + "企业付款接口结果：" + jsonXML.get("err_code_des").toString());
                logger.error("查询订单号：" + orderNo + "企业付款接口结果：" + jsonXML.get("err_code_des").toString());
                result.put("code", 2);
                result.put("msg", jsonXML.get("err_code_des").toString());
            }
        } else {
            logger.error("查询订单号：" + orderNo + "企业付款接口结果：" + jsonXML.get("return_msg").toString());
            result.put("code", 2);
            result.put("msg", jsonXML.get("return_msg").toString());
        }

        return result;
    }

    /**
     * “F” 加当前时间加四位随机数
     * 订单编号
     * order_no
     *
     * @throws Exception
     **/
    private String createOrderNo() throws Exception {
        return "F" + DateUtils.getNowLongTime() + String.valueOf((int) (Math.random() * (9999 - 1000 + 1)) + 1000);
    }


    @Override
    public int count(Map<String, Object> query) {
        return extMapper.count(query);
    }

    @Override
    public void widthdraw(Integer applyId) throws Exception {
        TabWithdrawApply tabWithdrawApply = withdrawApplyMapper.selectByPrimaryKey(applyId);
        if (null == tabWithdrawApply) {
            throw new RuntimeException("提现id错误:tabWithdrawApply=" + tabWithdrawApply);
        }

        JSONObject result = null;
        //提现类型0：教练员，1：代理商，2：顶级代理商
        int applyType = tabWithdrawApply.getApplyType();
        Long applicantId = tabWithdrawApply.getApplicantId();
        boolean isSuccess = true;

        if (applyType == 0) {  //教练员
            TabCoach tabCoach = coachMapper.selectByPrimaryKey(applicantId);
            if (null == tabCoach) {
                throw new RuntimeException("教练提现，提现人id错误：applicantId=" + applicantId);
            }

            // 教练提现，失败就回写状态
            logger.info("开始给教练" + tabCoach.getCoachName() + "提现");
            String coach_separate_order_no = createOrderNo();
            result = transfers(coach_separate_order_no, tabWithdrawApply.getAmountApply(), tabCoach.getOpenid(), "NO_CHECK", tabCoach.getCoachName());
            logger.info("教练提现结果：" + JSONObject.toJSONString(result));
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coach_id", applicantId);
            paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
            //申请提现失败，减少冻结amount_freeze，增加可用amount_desirable
            if (extTabCoachMapper.reduceFreezeAndAddAmountDesirable(paramMap) <= 0) {
                throw new RuntimeException("修改教练金额失败");
            }
            if (result.getIntValue("code") != 1) {
                // 提现失败
                // 增加教练的amount_desirable可取金额
                isSuccess = false;
                paramMap = new HashMap<String, Object>();
                paramMap.put("coach_id", applicantId);
                paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
                //申请提现成功，减少冻结amount_freeze，增加已提现amount_withdraw
                if (extTabCoachMapper.reduceFreezeAndAddAmountWithdraw(paramMap) <= 0) {
                    throw new RuntimeException("修改教练金额失败");
                }
            }

        } else if (applyId == 1) { //代理商
            TabAgent tabAgent = agentService.selectByPrimaryKey(applicantId);
            if (null == tabAgent) {
                throw new RuntimeException("代理商提现，提现人id错误：applicantId=" + applicantId);
            }
            // 代理提现，失败就回写状态
            logger.info("开始给代理" + tabAgent.getAgentName() + "提现");
            String coach_separate_order_no = createOrderNo();
            result = transfers(coach_separate_order_no, tabWithdrawApply.getAmountApply(), tabAgent.getOpenid(), "NO_CHECK", tabAgent.getAgentName());
            logger.info("代理提现结果：" + JSONObject.toJSONString(result));
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("agent_id", applicantId);
            paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
            if (extTabAgentMapper.reduceFreezeAndAddAmountWithdraw(paramMap) <= 0) {
                throw new RuntimeException("修改代理商金额失败");
            }
            if (result.getIntValue("code") != 1) {
                // 提现失败
                // 增加代理的amount_desirable可取金额
                isSuccess = false;
                paramMap = new HashMap<String, Object>();
                paramMap.put("agent_id", applicantId);
                paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
                if (extTabAgentMapper.reduceFreezeAndAddAmountDesirable(paramMap) <= 0) {
                    throw new RuntimeException("修改代理商金额失败");
                }
            }

        } else if (applyId == 2) {//顶级代理商
            TabTopAgent tabTopAgent = topAgentService.selectByPrimaryKey(applicantId);
            if (null == tabTopAgent) {
                throw new RuntimeException("顶级代理商提现，提现人id错误：applicantId=" + applicantId);
            }

            // 顶级代理提现，失败就回写状态
            logger.info("开始给顶级代理" + tabTopAgent.getTopAgentName() + "提现");
            String coach_separate_order_no = createOrderNo();
            result = transfers(coach_separate_order_no, tabWithdrawApply.getAmountApply(), tabTopAgent.getOpenid(), "NO_CHECK", tabTopAgent.getTopAgentName());
            logger.info("顶级代理提现结果：" + JSONObject.toJSONString(result));
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("top_agent_id", applicantId);
            paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
            if (extTopAgentMapper.reduceFreezeAndAddAmountWithdraw(paramMap) <= 0) {
                throw new RuntimeException("修改顶级代理商金额失败");
            }
            if (result.getIntValue("code") != 1) {
                // 提现失败
                // 增加代理的amount_desirable可取金额
                isSuccess = false;
                paramMap = new HashMap<String, Object>();
                paramMap.put("top_agent_id", applicantId);
                paramMap.put("amount_apply", tabWithdrawApply.getAmountApply());
                if (extTopAgentMapper.reduceFreezeAndAddAmountDesirable(paramMap) <= 0) {
                    throw new RuntimeException("修改顶级代理商金额失败");
                }
            }
        }

        String msg = result.getString("msg");
        String paymentNo = result.getString("payment_no");
        String orderNo = result.getString("orderNo");

        //更新提现表
        if (isSuccess) {
            tabWithdrawApply.setState(1);
        } else {
            tabWithdrawApply.setState(2);
        }
        tabWithdrawApply.setOrderNo(orderNo);
        if (StringUtil.isNotEmpty(msg)) {
            tabWithdrawApply.setMsg(msg);
        }
        if (StringUtil.isNotEmpty(paymentNo)) {
            tabWithdrawApply.setPaymentNo(paymentNo);
        }
        int count = withdrawApplyMapper.updateByPrimaryKeySelective(tabWithdrawApply);
        if (count <= 0) {
            throw new RuntimeException("修改提现表失败");
        }
    }

    @Override
    public List<TabOrderVo> list(Map<String, Object> query) {
        return extMapper.list(query);
    }
}
