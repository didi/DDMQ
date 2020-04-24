package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.dao.model.TopicCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface TopicMapper {
    long countByExample(TopicCriteria example);

    int deleteByExample(TopicCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(Topic record);

    int insertSelective(Topic record);

    List<Topic> selectByExampleWithBLOBs(TopicCriteria example);

    List<Topic> selectByExample(TopicCriteria example);

    Topic selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Topic record, @Param("example") TopicCriteria example);

    int updateByExampleWithBLOBs(@Param("record") Topic record, @Param("example") TopicCriteria example);

    int updateByExample(@Param("record") Topic record, @Param("example") TopicCriteria example);

    int updateByPrimaryKeySelective(Topic record);

    int updateByPrimaryKeyWithBLOBs(Topic record);

    int updateByPrimaryKey(Topic record);
}