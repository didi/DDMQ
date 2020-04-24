package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ClusterCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface ClusterMapper {
    long countByExample(ClusterCriteria example);

    int deleteByExample(ClusterCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(Cluster record);

    int insertSelective(Cluster record);

    List<Cluster> selectByExample(ClusterCriteria example);

    Cluster selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Cluster record, @Param("example") ClusterCriteria example);

    int updateByExample(@Param("record") Cluster record, @Param("example") ClusterCriteria example);

    int updateByPrimaryKeySelective(Cluster record);

    int updateByPrimaryKey(Cluster record);
}