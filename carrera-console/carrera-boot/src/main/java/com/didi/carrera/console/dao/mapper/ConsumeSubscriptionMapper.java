package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.ConsumeSubscriptionCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface ConsumeSubscriptionMapper {
    long countByExample(ConsumeSubscriptionCriteria example);

    int deleteByExample(ConsumeSubscriptionCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(ConsumeSubscription record);

    int insertSelective(ConsumeSubscription record);

    List<ConsumeSubscription> selectByExampleWithBLOBs(ConsumeSubscriptionCriteria example);

    List<ConsumeSubscription> selectByExample(ConsumeSubscriptionCriteria example);

    ConsumeSubscription selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConsumeSubscription record, @Param("example") ConsumeSubscriptionCriteria example);

    int updateByExampleWithBLOBs(@Param("record") ConsumeSubscription record, @Param("example") ConsumeSubscriptionCriteria example);

    int updateByExample(@Param("record") ConsumeSubscription record, @Param("example") ConsumeSubscriptionCriteria example);

    int updateByPrimaryKeySelective(ConsumeSubscription record);

    int updateByPrimaryKeyWithBLOBs(ConsumeSubscription record);

    int updateByPrimaryKey(ConsumeSubscription record);
}