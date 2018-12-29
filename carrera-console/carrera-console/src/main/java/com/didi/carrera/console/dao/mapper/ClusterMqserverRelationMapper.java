package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.ClusterMqserverRelation;
import com.didi.carrera.console.dao.model.ClusterMqserverRelationCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface ClusterMqserverRelationMapper {
    long countByExample(ClusterMqserverRelationCriteria example);

    int deleteByExample(ClusterMqserverRelationCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(ClusterMqserverRelation record);

    int insertSelective(ClusterMqserverRelation record);

    List<ClusterMqserverRelation> selectByExample(ClusterMqserverRelationCriteria example);

    ClusterMqserverRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ClusterMqserverRelation record, @Param("example") ClusterMqserverRelationCriteria example);

    int updateByExample(@Param("record") ClusterMqserverRelation record, @Param("example") ClusterMqserverRelationCriteria example);

    int updateByPrimaryKeySelective(ClusterMqserverRelation record);

    int updateByPrimaryKey(ClusterMqserverRelation record);
}