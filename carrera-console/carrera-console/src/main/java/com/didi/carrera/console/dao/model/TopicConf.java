package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.dao.model.custom.TopicConfConfig;
import org.apache.commons.lang3.StringUtils;


public class TopicConf implements Serializable {
    /**
     * 主键id
     * default = null
     * length = 20
     */
    private Long id;

    /**
     * topic 主键id
     * default = 0
     * length = 19
     */
    private Long topicId;

    /**
     * topic名称
     * default =
     * length = 256
     */
    private String topicName;

    /**
     * 集群id
     * default = 0
     * length = 19
     */
    private Long clusterId;

    /**
     * 集群名称
     * default =
     * length = 256
     */
    private String clusterName;

    /**
     * mqserver id
     * default = 0
     * length = 19
     */
    private Long mqServerId;

    /**
     * mqserver名称
     * default =
     * length = 256
     */
    private String mqServerName;

    /**
     * server对应的机房id
     * default = 0
     * length = 19
     */
    private Long serverIdcId;

    /**
     * server对应的机房名称
     * default =
     * length = 256
     */
    private String serverIdcName;

    /**
     * clientidc对应的id和value
     * default =
     * length = 1024
     */
    private Map<String, Long> clientIdc;

    /**
     * 预估生产tps,默认1024
     * default = 1024
     * length = 10
     */
    private Integer produceTps;

    /**
     * 消息平均大小，单位字节
     * default = 0
     * length = 10
     */
    private Integer msgAvgSize;

    /**
     * 消息最大大小，单位字节
     * default = 0
     * length = 10
     */
    private Integer msgMaxSize;

    /**
     * topic状态，0启用,1禁用
     * default = 0
     * length = 3
     */
    private Byte state;

    /**
     * 运维端配置参数
     * default =
     * length = 2048
     */
    private TopicConfConfig config;

    /**
     * 是否删除 0:未删除 1:删除
     * default = 0
     * length = 3
     */
    private Byte isDelete;

    /**
     * 创建时间
     * default = 1970-01-01 00:00:00
     * length = 19
     */
    private Date createTime;

    /**
     * 修改时间
     * default = CURRENT_TIMESTAMP
     * length = 19
     */
    private Date modifyTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.topicName = topicName == null ? null : topicName.trim();
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
        this.clusterName = clusterName == null ? null : clusterName.trim();
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
        this.mqServerName = mqServerName == null ? null : mqServerName.trim();
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
        this.serverIdcName = serverIdcName == null ? null : serverIdcName.trim();
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

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
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

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TopicConf other = (TopicConf) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTopicId() == null ? other.getTopicId() == null : this.getTopicId().equals(other.getTopicId()))
                && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
                && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
                && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()))
                && (this.getMqServerId() == null ? other.getMqServerId() == null : this.getMqServerId().equals(other.getMqServerId()))
                && (this.getMqServerName() == null ? other.getMqServerName() == null : this.getMqServerName().equals(other.getMqServerName()))
                && (this.getServerIdcId() == null ? other.getServerIdcId() == null : this.getServerIdcId().equals(other.getServerIdcId()))
                && (this.getServerIdcName() == null ? other.getServerIdcName() == null : this.getServerIdcName().equals(other.getServerIdcName()))
                && (this.getClientIdc() == null ? other.getClientIdc() == null : this.getClientIdc().equals(other.getClientIdc()))
                && (this.getProduceTps() == null ? other.getProduceTps() == null : this.getProduceTps().equals(other.getProduceTps()))
                && (this.getMsgAvgSize() == null ? other.getMsgAvgSize() == null : this.getMsgAvgSize().equals(other.getMsgAvgSize()))
                && (this.getMsgMaxSize() == null ? other.getMsgMaxSize() == null : this.getMsgMaxSize().equals(other.getMsgMaxSize()))
                && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
                && (this.getConfig() == null ? other.getConfig() == null : this.getConfig().equals(other.getConfig()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTopicId() == null) ? 0 : getTopicId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        result = prime * result + ((getMqServerId() == null) ? 0 : getMqServerId().hashCode());
        result = prime * result + ((getMqServerName() == null) ? 0 : getMqServerName().hashCode());
        result = prime * result + ((getServerIdcId() == null) ? 0 : getServerIdcId().hashCode());
        result = prime * result + ((getServerIdcName() == null) ? 0 : getServerIdcName().hashCode());
        result = prime * result + ((getClientIdc() == null) ? 0 : getClientIdc().hashCode());
        result = prime * result + ((getProduceTps() == null) ? 0 : getProduceTps().hashCode());
        result = prime * result + ((getMsgAvgSize() == null) ? 0 : getMsgAvgSize().hashCode());
        result = prime * result + ((getMsgMaxSize() == null) ? 0 : getMsgMaxSize().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getConfig() == null) ? 0 : getConfig().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        String sb = getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", topicId=" + topicId +
                ", topicName=" + topicName +
                ", clusterId=" + clusterId +
                ", clusterName=" + clusterName +
                ", mqServerId=" + mqServerId +
                ", mqServerName=" + mqServerName +
                ", serverIdcId=" + serverIdcId +
                ", serverIdcName=" + serverIdcName +
                ", clientIdc=" + clientIdc +
                ", produceTps=" + produceTps +
                ", msgAvgSize=" + msgAvgSize +
                ", msgMaxSize=" + msgMaxSize +
                ", state=" + state +
                ", config=" + config +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}