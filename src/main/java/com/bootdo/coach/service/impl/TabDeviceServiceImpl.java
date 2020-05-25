package com.bootdo.coach.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootdo.coach.dao.TabDeviceMapper;
import com.bootdo.coach.dao.ext.ExtTabDeviceMapper;
import com.bootdo.coach.domain.TabDevice;
import com.bootdo.coach.service.TabDeviceService;
import com.bootdo.coach.vo.TabDeviceVo;

@Service
public class TabDeviceServiceImpl implements TabDeviceService {
    @Autowired
    private TabDeviceMapper mapper;
    @Autowired
    private ExtTabDeviceMapper extMapper;

	@Override
	public int deleteByPrimaryKey(Long deviceId) {
		return mapper.deleteByPrimaryKey(deviceId);
	}

	@Override
	public int insert(TabDevice record) {
		return mapper.insert(record);
	}

	@Override
	public int insertSelective(TabDevice record) {
		return mapper.insertSelective(record);
	}

	@Override
	public TabDevice selectByPrimaryKey(Long deviceId) {
		return mapper.selectByPrimaryKey(deviceId);
	}

	@Override
	public int updateByPrimaryKeySelective(TabDevice record) {
		return mapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(TabDevice record) {
		return mapper.updateByPrimaryKey(record);
	}

	@Override
	public TabDevice selectByDeviceNo(String device_no) {
		List<TabDevice> list = extMapper.selectByDeviceNo(device_no);
    	if (list == null || list.isEmpty()) {
    		return null;
		}
    	return list.get(0);
	}
	
	@Override
	public int count(Map<String, Object> query) {
		return extMapper.count(query);
	}

	@Override
	public List<TabDeviceVo> list(Map<String, Object> query) {
		return extMapper.list(query);
	}
	
	@Override
	public boolean exitDeviceNo(Map<String, Object> params) {
		boolean exit;
        exit = extMapper.list(params).size() > 0;
        return exit;
	}

	@Override
	public int countCoachDevice(Long coachId) {
		return extMapper.countCoachDevice(coachId);
	}
	@Override
	public  List<TabDevice> sumDeviceRanking(Map<String, Object> params) {
		return extMapper.getSumDeviceRanking(params);
	}
	@Override
	public List<TabDeviceVo> findCoachDevice(Map<String, Object> params) {
		return extMapper.getCoachDevice(params);
	}

	@Override
	public int countsDevice(Map<String, Object> params) {
		return extMapper.countsDevice(params);
	}
}
