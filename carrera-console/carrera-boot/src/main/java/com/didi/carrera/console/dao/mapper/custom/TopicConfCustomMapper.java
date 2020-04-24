package com.didi.carrera.console.dao.mapper.custom;

import java.util.List;

import com.didi.carrera.console.dao.model.custom.CustomTopicConf;


public interface TopicConfCustomMapper {

    List<CustomTopicConf> selectByTopicId(List<Long> list);
}