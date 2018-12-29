package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.dao.model.ConsumeGroupCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface ConsumeGroupMapper {
    long countByExample(ConsumeGroupCriteria example);

    int deleteByExample(ConsumeGroupCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(ConsumeGroup record);

    int insertSelective(ConsumeGroup record);

    List<ConsumeGroup> selectByExample(ConsumeGroupCriteria example);

    ConsumeGroup selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConsumeGroup record, @Param("example") ConsumeGroupCriteria example);

    int updateByExample(@Param("record") ConsumeGroup record, @Param("example") ConsumeGroupCriteria example);

    int updateByPrimaryKeySelective(ConsumeGroup record);

    int updateByPrimaryKey(ConsumeGroup record);
}