package com.didi.carrera.console.dao.mapper;

import java.util.List;

import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.dao.model.MqServerCriteria;
import org.apache.ibatis.annotations.Param;


public interface MqServerMapper {
    long countByExample(MqServerCriteria example);

    int deleteByExample(MqServerCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(MqServer record);

    int insertSelective(MqServer record);

    List<MqServer> selectByExample(MqServerCriteria example);

    MqServer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MqServer record, @Param("example") MqServerCriteria example);

    int updateByExample(@Param("record") MqServer record, @Param("example") MqServerCriteria example);

    int updateByPrimaryKeySelective(MqServer record);

    int updateByPrimaryKey(MqServer record);
}