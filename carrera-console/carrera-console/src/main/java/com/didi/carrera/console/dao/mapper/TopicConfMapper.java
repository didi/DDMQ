package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.dao.model.TopicConfCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface TopicConfMapper {
    long countByExample(TopicConfCriteria example);

    int deleteByExample(TopicConfCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(TopicConf record);

    int insertSelective(TopicConf record);

    List<TopicConf> selectByExample(TopicConfCriteria example);

    TopicConf selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TopicConf record, @Param("example") TopicConfCriteria example);

    int updateByExample(@Param("record") TopicConf record, @Param("example") TopicConfCriteria example);

    int updateByPrimaryKeySelective(TopicConf record);

    int updateByPrimaryKey(TopicConf record);
}