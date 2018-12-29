package com.didi.carrera.console.dao.model.custom;

import java.io.Serializable;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.didi.carrera.console.common.util.FastJsonUtils;
import org.apache.commons.lang3.StringUtils;


public class CustomTopicConf implements Serializable {

    private Long topicId;
    private Long confId;
    private Long clusterId;
    private String clusterName;
    private Long mqServerId;
    private String mqServerName;
    private Long serverIdcId;
    private String serverIdcName;
    private Map<String, Long> clientIdc;
    private Integer produceTps;
    private Integer msgAvgSize;
    private Integer msgMaxSize;
    private Integer consumerNum;
    private TopicConfConfig config;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getConfId() {
        return confId;
    }

    public void setConfId(Long confId) {
        this.confId = confId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

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

    public Integer getProduceTps() {
        return produceTps;
    }

    public void setProduceTps(Integer produceTps) {
        this.produceTps = produceTps;
    }

    public Integer getMsgAvgSize() {
        return msgAvgSize;
    }

    public void setMsgAvgSize(Integer msgAvgSize) {
        this.msgAvgSize = msgAvgSize;
    }

    public Integer getMsgMaxSize() {
        return msgMaxSize;
    }

    public void setMsgMaxSize(Integer msgMaxSize) {
        this.msgMaxSize = msgMaxSize;
    }

    public Integer getConsumerNum() {
        return consumerNum;
    }

    public void setConsumerNum(Integer consumerNum) {
        this.consumerNum = consumerNum;
    }

    public TopicConfConfig getTopicConfConfig() {
        return config;
    }

    public void setTopicConfig(TopicConfConfig config) {
        this.config = config;
    }

    public String getConfig() {
        return this.config == null ? null : FastJsonUtils.toJsonString(config);
    }

    public void setConfig(String config) {
        this.config = StringUtils.isBlank(config) ? null : FastJsonUtils.toObject(config, TopicConfConfig.class);
    }

    public Long getServerIdcId() {
        return serverIdcId;
    }

    public void setServerIdcId(Long serverIdcId) {
        this.serverIdcId = serverIdcId;
    }

    public String getServerIdcName() {
        return serverIdcName;
    }

    public void setServerIdcName(String serverIdcName) {
        this.serverIdcName = serverIdcName;
    }



    public String getClientIdc() {
        return this.clientIdc == null ? null : FastJsonUtils.toJsonString(clientIdc);
    }

    public void setClientIdc(String clientIdc) {
        this.clientIdc = StringUtils.isBlank(clientIdc) ? null : FastJsonUtils.toObject(clientIdc, new TypeReference<Map<String, Long>>() {
        });
    }

    public Map<String, Long> getTopicConfClientIdc() {
        return clientIdc;
    }

    public void setTopicConfClientIdc(Map<String, Long> clientIdcMapper) {
        this.clientIdc = clientIdcMapper;
    }
}