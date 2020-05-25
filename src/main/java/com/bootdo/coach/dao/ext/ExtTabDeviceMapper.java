package com.bootdo.coach.dao.ext;

import java.util.List;
import java.util.Map;

import com.bootdo.coach.domain.TabDevice;
import com.bootdo.coach.vo.TabDeviceVo;
import org.apache.ibatis.annotations.Param;

public interface ExtTabDeviceMapper {
	List<TabDevice> selectByDeviceNo(String device_no);

	int count(Map<String, Object> query);

	List<TabDeviceVo> list(Map<String, Object> query);

	int countCoachDevice(@Param("coachId") Long coachId);

	List<TabDevice> getSumDeviceRanking(Map<String, Object> params);

	List<TabDeviceVo> getCoachDevice(Map<String, Object> params);

	int countsDevice(Map<String, Object> params);
}