package com.didi.carrera.console.service.vo;

import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.web.controller.bo.TopicOrderBo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;


public class TopicOrderVo extends TopicOrderBo<TopicConfVo> {

    public void addConf(TopicConfVo vo) {
        if (CollectionUtils.isEmpty(getConf())) {
            setConf(Lists.newArrayList());
        }

        getConf().add(vo);
    }

    public static TopicOrderVo buildTopicVo(Topic topic) {
        TopicOrderVo vo = new TopicOrderVo();
        BeanUtils.copyProperties(topic, vo);
        vo.setTopicId(topic.getId());
        vo.setSchema(topic.getTopicSchema());
        vo.setAlarmGroup(topic.getTopicAlarmGroup());
        vo.setExtraParams(topic.getTopicExtraParams());

        return vo;
    }
}