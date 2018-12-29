package com.didi.carrera.console.service.vo;

import com.didi.carrera.console.dao.model.Topic;


public class TopicSimpleVo {

    private Long topicId;
    private String topicName;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String toString() {
        return "TopicSimpleVo{" +
                "topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                "} " + super.toString();
    }

    public static TopicSimpleVo buildVo(Topic topic) {
        TopicSimpleVo vo = new TopicSimpleVo();
        vo.setTopicId(topic.getId());
        vo.setTopicName(topic.getTopicName());
        return vo;
    }
}