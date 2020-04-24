package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.didi.carrera.console.dao.model.TopicConf;
import org.springframework.beans.BeanUtils;


public class AcceptTopicConfBo extends TopicConfBo {

    @NotNull(message = "MqServer Id不能为空")
    @Min(value = 1, message = "MqServerId必须大于0")
    private Long mqServerId;

    private String mqServerName;

    public Long getMqServerId() {
        return mqServerId;
    }

    public void setMqServerId(Long mqServerId) {
        this.mqServerId = mqServerId;
    }

    public String getMqServerName() {
        return mqServerName;
    }

    public void setMqServerName(String mqServerName) {
        this.mqServerName = mqServerName;
    }

    @Override
    public TopicConf buildTopicConf() {
        TopicConf topicConf = super.buildTopicConf();
        BeanUtils.copyProperties(this, topicConf);
        return topicConf;
    }

    @Override
    public String toString() {
        return "AcceptTopicConfBo{" +
                "mqServerId=" + mqServerId +
                ", mqServerName='" + mqServerName + '\'' +
                "} " + super.toString();
    }
}