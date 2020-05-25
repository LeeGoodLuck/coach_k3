package com.bootdo.coach.dao;

import com.bootdo.coach.domain.TabCoach;

public interface TabCoachMapper {
    int deleteByPrimaryKey(Long coachId);

    int insert(TabCoach record);

    int insertSelective(TabCoach record);

    TabCoach selectByPrimaryKey(Long coachId);

    int updateByPrimaryKeySelective(TabCoach record);

    int updateByPrimaryKey(TabCoach record);
}