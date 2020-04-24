package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

import com.didi.carrera.console.dao.model.TopicConf;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;


public class TopicConfBo {

    @NotNull(message = "请选择集群配置信息")
    private Long clusterId;

    @NotBlank(message = "请选择集群配置信息")
    private String clusterName;

    private Long serverIdcId;
    private String serverIdcName;

    private Map<String, Long> clientIdcMap;

    @NotNull(message = "预估最大消息体大小不能为空")
    @Min(value = 1, message = "预估最大消息体大小必须大于0")
    private Integer msgMaxSize;

    @NotNull(message = "预估平均消息体大小不能为空")
    @Min(value = 1, message = "预估平均消息体大小必须大于0")
    private Integer msgAvgSize;

    @NotNull(message = "限流不能为空")
    @Min(value = 1, message = "限流必须大于0")
    private Integer produceTps;

    private Map<String, String> operationParams;

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

    public Integer getMsgMaxSize() {
        return msgMaxSize;
    }

    public void setMsgMaxSize(Integer msgMaxSize) {
        this.msgMaxSize = msgMaxSize;
    }

    public Integer getMsgAvgSize() {
        return msgAvgSize;
    }

    public void setMsgAvgSize(Integer msgAvgSize) {
        this.msgAvgSize = msgAvgSize;
    }

    public Integer getProduceTps() {
        return produceTps;
    }

    public void setProduceTps(Integer produceTps) {
        this.produceTps = produceTps;
    }

    public Map<String, String> getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(Map<String, String> operationParams) {
        this.operationParams = operationParams;
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

    public Map<String, Long> getClientIdcMap() {
        return clientIdcMap;
    }

    public void setClientIdcMap(Map<String, Long> clientIdcMap) {
        this.clientIdcMap = clientIdcMap;
    }

    public TopicConf buildTopicConf() {
        TopicConf topicConf = new TopicConf();
        BeanUtils.copyProperties(this, topicConf);
        topicConf.setTopicConfClientIdc(clientIdcMap);

        return topicConf;
    }

    @Override
    public String toString() {
        return "TopicConfBo{" +
                "clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", msgMaxSize=" + msgMaxSize +
                ", msgAvgSize=" + msgAvgSize +
                ", produceTps=" + produceTps +
                ", operationParams=" + operationParams +
                '}';
    }
}