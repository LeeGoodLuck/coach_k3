package com.bootdo.coach.service.impl;

import com.bootdo.coach.dao.TabSeparateAccountMapper;
import com.bootdo.coach.dao.ext.ExtTabSeparateAccountMapper;
import com.bootdo.coach.domain.TabSeparateAccount;
import com.bootdo.coach.service.TabSeparateAccountService;
import com.bootdo.coach.vo.TabSeparateAccountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TabSeparateAccountServiceImpl implements TabSeparateAccountService {
    @Autowired
    private TabSeparateAccountMapper mapper;
    @Autowired
    private ExtTabSeparateAccountMapper extMapper;

	@Override
	public int deleteByPrimaryKey(Long id) {
		return mapper.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(TabSeparateAccount record) {
		return mapper.insert(record);
	}

	@Override
	public int insertSelective(TabSeparateAccount record) {
		return mapper.insertSelective(record);
	}

	@Override
	public TabSeparateAccount selectByPrimaryKey(Long id) {
		return mapper.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(TabSeparateAccount record) {
		return mapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(TabSeparateAccount record) {
		return mapper.updateByPrimaryKey(record);
	}

	@Override
	public BigDecimal findCoachMoney(Map<String, Object> paramMap) {
		return extMapper.findCoachMoney(paramMap);
	}

	@Override
	public int findCoachMoneyCnt(Long coach_id) {
		return extMapper.findCoachMoneyCnt(coach_id);
	}
	
	@Override
	public List<Map<String, Object>> getCoachIncomeList(Map<String, Object> paramMap) {
		return extMapper.getCoachIncomeList(paramMap);
	}

	@Override
	public List<Map<String, Object>> getStudentIncomeList(Map<String, Object> paramMap) {
		return extMapper.getstudentIncomeList(paramMap);
	}

	@Override
	public int count(Map<String, Object> query) {
		return extMapper.count(query);
	}

	@Override
	public List<TabSeparateAccountVo> list(Map<String, Object> query) {
		return extMapper.list(query);
	}
	
	@Override
	public BigDecimal getAgentTotalMoney(Map<String, Object> params) {
		List<TabSeparateAccountVo> list = extMapper.getAgentTotalMoney(params);
		BigDecimal sum = new BigDecimal(0.00);
		if (list != null) {
			for (TabSeparateAccountVo tabSeparateAccountVo : list) {
				switch (tabSeparateAccountVo.getLevel()) {
				case 0:
					sum = sum.add(tabSeparateAccountVo.getAgent1Money());
					break;
				case 1:
					sum = sum.add(tabSeparateAccountVo.getAgent2Money());
					break;
				case 2:
					sum = sum.add(tabSeparateAccountVo.getAgent3Money());
					break;

				default:
					break;
				}
			}
		}
		return sum;
	}

	@Override
	public int findAgentMoneyCnt(Long agent_id) {
		return extMapper.findAgentMoneyCnt(agent_id);
	}

	@Override
	public List<Map<String, Object>> getAgentIncomeList(Map<String, Object> paramMap) {
		return extMapper.getAgentIncomeList(paramMap);
	}
	@Override
	public BigDecimal findDayRevenue(Long coachId) { return extMapper.getDayRevenue(coachId);	}

	@Override
	public BigDecimal findSevenRevenue(Long coachId) {
		return extMapper.getSevenRevenue(coachId);
	}

	@Override
	public BigDecimal findMonthRevenue(Long coachId) {
		return extMapper.getMonthRevenue(coachId);
	}

	@Override
	public BigDecimal findTotalRevenue(Long coachId) {
		return extMapper.getTotalRevenue(coachId);
	}
	@Override
	public BigDecimal findMonthRevenue(Map<String, Object> params) {
		List<TabSeparateAccountVo> list = extMapper.getMonthRevenues(params);
		BigDecimal sum = new BigDecimal(0.00);
		if (list != null) {
			for (TabSeparateAccountVo tabSeparateAccountVo : list) {
				switch (tabSeparateAccountVo.getLevel()) {
					case 0:
						sum = sum.add(tabSeparateAccountVo.getAgent1Money());
						break;
					case 1:
						sum = sum.add(tabSeparateAccountVo.getAgent2Money());
						break;
					case 2:
						sum = sum.add(tabSeparateAccountVo.getAgent3Money());
						break;

					default:
						break;
				}
			}
		}
		return sum;
	}
	@Override
	public BigDecimal findSevenRevenue(Map<String, Object> params) {
		List<TabSeparateAccountVo> list = extMapper.getSevenRevenues(params);
		BigDecimal sum = new BigDecimal(0.00);
		if (list != null) {
			for (TabSeparateAccountVo tabSeparateAccountVo : list) {
				switch (tabSeparateAccountVo.getLevel()) {
					case 0:
						sum = sum.add(tabSeparateAccountVo.getAgent1Money());
						break;
					case 1:
						sum = sum.add(tabSeparateAccountVo.getAgent2Money());
						break;
					case 2:
						sum = sum.add(tabSeparateAccountVo.getAgent3Money());
						break;

					default:
						break;
				}
			}
		}
		return sum;
	}
	@Override
	public BigDecimal findDayRevenue(Map<String, Object> params) {
		List<TabSeparateAccountVo> list = extMapper.getDayRevenues(params);
		BigDecimal sum = new BigDecimal(0.00);
		if (list != null) {
			for (TabSeparateAccountVo tabSeparateAccountVo : list) {
				switch (tabSeparateAccountVo.getLevel()) {
					case 0:
						sum = sum.add(tabSeparateAccountVo.getAgent1Money());
						break;
					case 1:
						sum = sum.add(tabSeparateAccountVo.getAgent2Money());
						break;
					case 2:
						sum = sum.add(tabSeparateAccountVo.getAgent3Money());
						break;

					default:
						break;
				}
			}
		}
		return sum;
	}

	@Override
	public BigDecimal finAgentTotalRevenue(Map<String, Object> params) {
		return extMapper.finAgentTotalRevenue(params);
	}
	@Override
	public List<TabSeparateAccount> oneAgentMoney(Long agentId) {
		return extMapper.getOneAgentMoney(agentId);
	}

	@Override
	public List<TabSeparateAccount> twoAgentMoney(Long agentId) {
		return extMapper.getTwoAgentMoney(agentId);
	}

	@Override
	public List<TabSeparateAccount> threeAgentMoney(Long agentId) {
		return extMapper.getThreeAgentMoney(agentId);
	}

	@Override
	public List<TabSeparateAccount> wholeMoney() {
		return extMapper.getWholeMoney();
	}
	@Override
	public List<TabSeparateAccount> timeWholeMoney(Long typeId) {
		return extMapper.getTimeWholeMoney(typeId);
	}

	@Override
	public List<TabSeparateAccount> timeAgentMoney(TabSeparateAccountVo account) {
		return extMapper.getTimeAgentMoney(account);
	}
}
