package com.bootdo.coach.service;

import java.util.List;
import java.util.Map;

import com.bootdo.coach.domain.TabDevice;
import com.bootdo.coach.vo.TabDeviceVo;

public interface TabDeviceService {
	
    int deleteByPrimaryKey(Long deviceId);

    int insert(TabDevice record);

    int insertSelective(TabDevice record);

    TabDevice selectByPrimaryKey(Long deviceId);

    int updateByPrimaryKeySelective(TabDevice record);

    int updateByPrimaryKey(TabDevice record);

	TabDevice selectByDeviceNo(String device_no);

	List<TabDeviceVo> list(Map<String, Object> params);

	int count(Map<String, Object> params);

	boolean exitDeviceNo(Map<String, Object> params);

    int countCoachDevice(Long coachId);

    List<TabDevice> sumDeviceRanking(Map<String, Object> params);

    List<TabDeviceVo> findCoachDevice(Map<String, Object> params);

    int countsDevice(Map<String, Object> params);

}
