package com.didi.carrera.console.dao.model;

import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.dao.model.custom.ConsumeSubscriptionConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ConsumeSubscription implements Serializable {
    /**
	   * 自增id
	   * default = null
	   * length = 20
	   */
    private Long id;

    /**
	   * 组id
	   * default = 0
	   * length = 19
	   */
    private Long groupId;

    /**
	   * 组名称
	   * default = 
	   * length = 256
	   */
    private String groupName;

    /**
	   * topic主键id
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
	   * 0 否不接收压测流量，1 接收压测流量，默认0否不接收压测流量
	   * default = 0
	   * length = 3
	   */
    private Byte pressureTraffic;

    /**
	   * 限流tps
	   * default = 1000.00
	   * length = 11
	   */
    private Double maxTps;

    /**
	   * 报警类型：0.继承消费组配置 1.单独配置
	   * default = 0
	   * length = 3
	   */
    private Byte alarmType;

    /**
	   * 0启用报警 1禁用报警
	   * default = 0
	   * length = 3
	   */
    private Byte alarmIsEnable;

    /**
	   * 报警级别，默认 二级报警，1，2，3级报警
	   * default = 2
	   * length = 3
	   */
    private Byte alarmLevel;

    /**
	   * 消息积压报警阈值，默认积压10000条
	   * default = 10000
	   * length = 10
	   */
    private Integer alarmMsgLag;

    /**
	   * 消息延迟报警时间，单位ms，默认5分钟
	   * default = 300000
	   * length = 10
	   */
    private Integer alarmDelayTime;

    /**
	   * 使用消息接口类型，1：highlevel 2 lowlevel，默认1
	   * default = 1
	   * length = 3
	   */
    private Byte apiType;

    /**
	   * 消息超时时间，默认1000ms，单位ms
	   * default = 1000
	   * length = 10
	   */
    private Integer consumeTimeout;

    /**
	   * 消息错误重试次数，默认3次,-1 为一直重试
	   * default = 3
	   * length = 10
	   */
    private Integer errorRetryTimes;

    /**
     * 消息重试间隔，分号分隔
     * default =
     * length = 1024
     */
    private List<Integer> retryIntervals;

    /**
	   * 消息类型：1Json 2text 3二进制数据
	   * default = 0
	   * length = 3
	   */
    private Byte msgType;

    /**
	   * 是否启用groovy  0 启用 1禁用
	   * default = 0
	   * length = 3
	   */
    private Byte enableGroovy;

    /**
	   * 是否启用Transit 0 启用 1禁用
	   * default = 0
	   * length = 3
	   */
    private Byte enableTransit;

    /**
	   * 是否启用保序，0启用 1禁用
	   * default = 0
	   * length = 3
	   */
    private Byte enableOrder;

    /**
	   * 保序key
	   * default = 
	   * length = 512
	   */
    private String orderKey;

    /**
	   * 消费类型：1SDK 2HTTP 3直写第三方组件
	   * default = 0
	   * length = 3
	   */
    private Byte consumeType;

    /**
	   * 写入类型，0:hdfs 1:hbase 2:redis
	   * default = 0
	   * length = 3
	   */
    private Byte bigDataType;

    /**
	   * 大数据配置内容，json格式
	   * default = 
	   * length = 2048
	   */
    private String bigDataConfig;

    /**
	   * url列表，多个以分号分隔
	   * default = 
	   * length = 2048
	   */
    private List<String> urls;

    /**
	   * 0 Post 1Get
	   * default = 0
	   * length = 3
	   */
    private Byte httpMethod;

    /**
	   * header 分号分隔,key:val;key:val
	   * default = 
	   * length = 1024
	   */
    private Map<String, String> httpHeaders;

    /**
	   * queryParmas,分号分隔,key:val;key:val
	   * default = 
	   * length = 1024
	   */
    private Map<String, String> httpQueryParams;

    /**
	   * 消息推送方式：1.放在http消息体 2.param=<msg> 3.消息体第一层打平
	   * default = 0
	   * length = 3
	   */
    private Byte msgPushType;

    /**
	   * token 校验
	   * default = 
	   * length = 1024
	   */
    private String httpToken;

    /**
	   * http消息支持最大并发
	   * default = 0
	   * length = 10
	   */
    private Integer pushMaxConcurrency;

    /**
	   * 消息链，根据config配置推算出actions
	   * default = 
	   * length = 1024
	   */
    private List<String> actions;

    /**
	   * 运维端配置参数
	   * default = 
	   * length = 2048
	   */
    private ConsumeSubscriptionConfig config;

    /**
	   * 消息订阅状态，0:启用 1:禁用
	   * default = 0
	   * length = 3
	   */
    private Byte state;

    /**
	   * 额外参数
	   * default = 
	   * length = 2048
	   */
    private Map<String, String> extraParams;

    /**
	   * 备注
	   * default = 
	   * length = 1024
	   */
    private String remark;

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

    /**
	   * groovy脚本
	   * default = null
	   * length = 65535
	   */
    private String groovy;

    /**
     * transit json字符串，key->val
     * default =
     * length = 1024
     */
    private Map<String, String> transit;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
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

    public Byte getPressureTraffic() {
        return pressureTraffic;
    }

    public void setPressureTraffic(Byte pressureTraffic) {
        this.pressureTraffic = pressureTraffic;
    }

    public Double getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(Double maxTps) {
        this.maxTps = maxTps;
    }

    public Byte getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Byte alarmType) {
        this.alarmType = alarmType;
    }

    public Byte getAlarmIsEnable() {
        return alarmIsEnable;
    }

    public void setAlarmIsEnable(Byte alarmIsEnable) {
        this.alarmIsEnable = alarmIsEnable;
    }

    public Byte getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(Byte alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public Integer getAlarmMsgLag() {
        return alarmMsgLag;
    }

    public void setAlarmMsgLag(Integer alarmMsgLag) {
        this.alarmMsgLag = alarmMsgLag;
    }

    public Integer getAlarmDelayTime() {
        return alarmDelayTime;
    }

    public void setAlarmDelayTime(Integer alarmDelayTime) {
        this.alarmDelayTime = alarmDelayTime;
    }

    public Byte getApiType() {
        return apiType;
    }

    public void setApiType(Byte apiType) {
        this.apiType = apiType;
    }

    public Integer getConsumeTimeout() {
        return consumeTimeout;
    }

    public void setConsumeTimeout(Integer consumeTimeout) {
        this.consumeTimeout = consumeTimeout;
    }

    public Integer getErrorRetryTimes() {
        return errorRetryTimes;
    }

    public void setErrorRetryTimes(Integer errorRetryTimes) {
        this.errorRetryTimes = errorRetryTimes;
    }

    public String getRetryIntervals() {
        return this.retryIntervals == null ? null : FastJsonUtils.toJsonString(retryIntervals);
    }

    public void setRetryIntervals(String retryIntervals) {
        this.retryIntervals = StringUtils.isBlank(retryIntervals) ? null : FastJsonUtils.toObject(retryIntervals, List.class);
    }

    public List<Integer> getSubRetryIntervals() {
        return retryIntervals;
    }

    public void setSubRetryIntervals(List<Integer> retryIntervals) {
        this.retryIntervals = retryIntervals;
    }

    public Byte getMsgType() {
        return msgType;
    }

    public void setMsgType(Byte msgType) {
        this.msgType = msgType;
    }

    public Byte getEnableGroovy() {
        return enableGroovy;
    }

    public void setEnableGroovy(Byte enableGroovy) {
        this.enableGroovy = enableGroovy;
    }

    public Byte getEnableTransit() {
        return enableTransit;
    }

    public void setEnableTransit(Byte enableTransit) {
        this.enableTransit = enableTransit;
    }

    public Byte getEnableOrder() {
        return enableOrder;
    }

    public void setEnableOrder(Byte enableOrder) {
        this.enableOrder = enableOrder;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey == null ? null : orderKey.trim();
    }

    public Byte getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(Byte consumeType) {
        this.consumeType = consumeType;
    }

    public Byte getBigDataType() {
        return bigDataType;
    }

    public void setBigDataType(Byte bigDataType) {
        this.bigDataType = bigDataType;
    }

    public String getBigDataConfig() {
        return bigDataConfig;
    }

    public void setBigDataConfig(String bigDataConfig) {
        this.bigDataConfig = bigDataConfig == null ? null : bigDataConfig.trim();
    }

    public String getUrls() {
        return this.urls == null ? null : FastJsonUtils.toJsonString(urls);
    }

    public void setUrls(String urls) {
        this.urls = StringUtils.isBlank(urls) ? null : FastJsonUtils.toObject(urls, List.class);
    }

    public List<String> getSubUrls() {
        return urls;
    }

    public void setSubUrls(List<String> urls) {
        this.urls = urls;
    }

    public Byte getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Byte httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpHeaders() {
        return this.httpHeaders == null ? null : FastJsonUtils.toJsonString(httpHeaders);
    }

    public void setHttpHeaders(String httpHeaders) {
        this.httpHeaders = StringUtils.isBlank(httpHeaders) ? null : FastJsonUtils.toObject(httpHeaders, Map.class);
    }

    public Map<String, String> getSubHttpHeaders() {
        return httpHeaders;
    }

    public void setSubHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getHttpQueryParams() {
        return this.httpQueryParams == null ? null : FastJsonUtils.toJsonString(httpQueryParams);
    }

    public void setHttpQueryParams(String httpQueryParams) {
        this.httpQueryParams = StringUtils.isBlank(httpQueryParams) ? null : FastJsonUtils.toObject(httpQueryParams, Map.class);
    }

    public Map<String, String> getSubHttpQueryParams() {
        return httpQueryParams;
    }

    public void setSubHttpQueryParams(Map<String, String> httpQueryParams) {
        this.httpQueryParams = httpQueryParams;
    }

    public Byte getMsgPushType() {
        return msgPushType;
    }

    public void setMsgPushType(Byte msgPushType) {
        this.msgPushType = msgPushType;
    }

    public String getHttpToken() {
        return httpToken;
    }

    public void setHttpToken(String httpToken) {
        this.httpToken = httpToken == null ? null : httpToken.trim();
    }

    public Integer getPushMaxConcurrency() {
        return pushMaxConcurrency;
    }

    public void setPushMaxConcurrency(Integer pushMaxConcurrency) {
        this.pushMaxConcurrency = pushMaxConcurrency;
    }

    public String getActions() {
        return this.actions == null ? null : FastJsonUtils.toJsonString(actions);
    }

    public void setActions(String actions) {
        this.actions = StringUtils.isBlank(actions) ? null : FastJsonUtils.toObject(actions, List.class);
    }

    public List<String> getSubActions() {
        return actions;
    }

    public void setSubActions(List<String> actions) {
        this.actions = actions;
    }

    public String getConfig() {
        return this.config == null ? null : FastJsonUtils.toJsonString(this.config);
    }

    public void setConfig(String config) {
        this.config = StringUtils.isBlank(config) ? null : FastJsonUtils.toObject(config, ConsumeSubscriptionConfig.class);
    }

    public ConsumeSubscriptionConfig getConsumeSubscriptionConfig() {
        return config;
    }

    public void setConsumeSubscriptionConfig(ConsumeSubscriptionConfig config) {
        this.config = config;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public String getExtraParams() {
        return this.extraParams == null ? null : FastJsonUtils.toJsonString(extraParams);
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = StringUtils.isBlank(extraParams) ? null : FastJsonUtils.toObject(extraParams, Map.class);
    }

    public Map<String, String> getSubExtraParams() {
        return extraParams;
    }

    public void setSubExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public String getGroovy() {
        return groovy;
    }

    public void setGroovy(String groovy) {
        this.groovy = groovy == null ? null : groovy.trim();
    }

    public String getTransit() {
        return this.transit == null ? null : FastJsonUtils.toJsonString(transit);
    }

    public void setTransit(String transit) {
        this.transit = StringUtils.isBlank(transit) ? null : FastJsonUtils.toObject(transit, Map.class);
    }

    public Map<String, String> getSubTransit() {
        return transit;
    }

    public void setSubTransit(Map<String, String> transit) {
        this.transit = transit;
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
        ConsumeSubscription other = (ConsumeSubscription) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getGroupId() == null ? other.getGroupId() == null : this.getGroupId().equals(other.getGroupId()))
            && (this.getGroupName() == null ? other.getGroupName() == null : this.getGroupName().equals(other.getGroupName()))
            && (this.getTopicId() == null ? other.getTopicId() == null : this.getTopicId().equals(other.getTopicId()))
            && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()))
            && (this.getPressureTraffic() == null ? other.getPressureTraffic() == null : this.getPressureTraffic().equals(other.getPressureTraffic()))
            && (this.getMaxTps() == null ? other.getMaxTps() == null : this.getMaxTps().equals(other.getMaxTps()))
            && (this.getAlarmType() == null ? other.getAlarmType() == null : this.getAlarmType().equals(other.getAlarmType()))
            && (this.getAlarmIsEnable() == null ? other.getAlarmIsEnable() == null : this.getAlarmIsEnable().equals(other.getAlarmIsEnable()))
            && (this.getAlarmLevel() == null ? other.getAlarmLevel() == null : this.getAlarmLevel().equals(other.getAlarmLevel()))
            && (this.getAlarmMsgLag() == null ? other.getAlarmMsgLag() == null : this.getAlarmMsgLag().equals(other.getAlarmMsgLag()))
            && (this.getAlarmDelayTime() == null ? other.getAlarmDelayTime() == null : this.getAlarmDelayTime().equals(other.getAlarmDelayTime()))
            && (this.getApiType() == null ? other.getApiType() == null : this.getApiType().equals(other.getApiType()))
            && (this.getConsumeTimeout() == null ? other.getConsumeTimeout() == null : this.getConsumeTimeout().equals(other.getConsumeTimeout()))
            && (this.getErrorRetryTimes() == null ? other.getErrorRetryTimes() == null : this.getErrorRetryTimes().equals(other.getErrorRetryTimes()))
            && (this.getRetryIntervals() == null ? other.getRetryIntervals() == null : this.getRetryIntervals().equals(other.getRetryIntervals()))
            && (this.getMsgType() == null ? other.getMsgType() == null : this.getMsgType().equals(other.getMsgType()))
            && (this.getEnableGroovy() == null ? other.getEnableGroovy() == null : this.getEnableGroovy().equals(other.getEnableGroovy()))
            && (this.getEnableTransit() == null ? other.getEnableTransit() == null : this.getEnableTransit().equals(other.getEnableTransit()))
            && (this.getEnableOrder() == null ? other.getEnableOrder() == null : this.getEnableOrder().equals(other.getEnableOrder()))
            && (this.getOrderKey() == null ? other.getOrderKey() == null : this.getOrderKey().equals(other.getOrderKey()))
            && (this.getConsumeType() == null ? other.getConsumeType() == null : this.getConsumeType().equals(other.getConsumeType()))
            && (this.getBigDataType() == null ? other.getBigDataType() == null : this.getBigDataType().equals(other.getBigDataType()))
            && (this.getBigDataConfig() == null ? other.getBigDataConfig() == null : this.getBigDataConfig().equals(other.getBigDataConfig()))
            && (this.getUrls() == null ? other.getUrls() == null : this.getUrls().equals(other.getUrls()))
            && (this.getHttpMethod() == null ? other.getHttpMethod() == null : this.getHttpMethod().equals(other.getHttpMethod()))
            && (this.getHttpHeaders() == null ? other.getHttpHeaders() == null : this.getHttpHeaders().equals(other.getHttpHeaders()))
            && (this.getHttpQueryParams() == null ? other.getHttpQueryParams() == null : this.getHttpQueryParams().equals(other.getHttpQueryParams()))
            && (this.getMsgPushType() == null ? other.getMsgPushType() == null : this.getMsgPushType().equals(other.getMsgPushType()))
            && (this.getHttpToken() == null ? other.getHttpToken() == null : this.getHttpToken().equals(other.getHttpToken()))
            && (this.getPushMaxConcurrency() == null ? other.getPushMaxConcurrency() == null : this.getPushMaxConcurrency().equals(other.getPushMaxConcurrency()))
            && (this.getActions() == null ? other.getActions() == null : this.getActions().equals(other.getActions()))
            && (this.getConfig() == null ? other.getConfig() == null : this.getConfig().equals(other.getConfig()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
            && (this.getExtraParams() == null ? other.getExtraParams() == null : this.getExtraParams().equals(other.getExtraParams()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()))
            && (this.getGroovy() == null ? other.getGroovy() == null : this.getGroovy().equals(other.getGroovy()))
            && (this.getTransit() == null ? other.getTransit() == null : this.getTransit().equals(other.getTransit()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
        result = prime * result + ((getGroupName() == null) ? 0 : getGroupName().hashCode());
        result = prime * result + ((getTopicId() == null) ? 0 : getTopicId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        result = prime * result + ((getPressureTraffic() == null) ? 0 : getPressureTraffic().hashCode());
        result = prime * result + ((getMaxTps() == null) ? 0 : getMaxTps().hashCode());
        result = prime * result + ((getAlarmType() == null) ? 0 : getAlarmType().hashCode());
        result = prime * result + ((getAlarmIsEnable() == null) ? 0 : getAlarmIsEnable().hashCode());
        result = prime * result + ((getAlarmLevel() == null) ? 0 : getAlarmLevel().hashCode());
        result = prime * result + ((getAlarmMsgLag() == null) ? 0 : getAlarmMsgLag().hashCode());
        result = prime * result + ((getAlarmDelayTime() == null) ? 0 : getAlarmDelayTime().hashCode());
        result = prime * result + ((getApiType() == null) ? 0 : getApiType().hashCode());
        result = prime * result + ((getConsumeTimeout() == null) ? 0 : getConsumeTimeout().hashCode());
        result = prime * result + ((getErrorRetryTimes() == null) ? 0 : getErrorRetryTimes().hashCode());
        result = prime * result + ((getRetryIntervals() == null) ? 0 : getRetryIntervals().hashCode());
        result = prime * result + ((getMsgType() == null) ? 0 : getMsgType().hashCode());
        result = prime * result + ((getEnableGroovy() == null) ? 0 : getEnableGroovy().hashCode());
        result = prime * result + ((getEnableTransit() == null) ? 0 : getEnableTransit().hashCode());
        result = prime * result + ((getEnableOrder() == null) ? 0 : getEnableOrder().hashCode());
        result = prime * result + ((getOrderKey() == null) ? 0 : getOrderKey().hashCode());
        result = prime * result + ((getConsumeType() == null) ? 0 : getConsumeType().hashCode());
        result = prime * result + ((getBigDataType() == null) ? 0 : getBigDataType().hashCode());
        result = prime * result + ((getBigDataConfig() == null) ? 0 : getBigDataConfig().hashCode());
        result = prime * result + ((getUrls() == null) ? 0 : getUrls().hashCode());
        result = prime * result + ((getHttpMethod() == null) ? 0 : getHttpMethod().hashCode());
        result = prime * result + ((getHttpHeaders() == null) ? 0 : getHttpHeaders().hashCode());
        result = prime * result + ((getHttpQueryParams() == null) ? 0 : getHttpQueryParams().hashCode());
        result = prime * result + ((getMsgPushType() == null) ? 0 : getMsgPushType().hashCode());
        result = prime * result + ((getHttpToken() == null) ? 0 : getHttpToken().hashCode());
        result = prime * result + ((getPushMaxConcurrency() == null) ? 0 : getPushMaxConcurrency().hashCode());
        result = prime * result + ((getActions() == null) ? 0 : getActions().hashCode());
        result = prime * result + ((getConfig() == null) ? 0 : getConfig().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getExtraParams() == null) ? 0 : getExtraParams().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
        result = prime * result + ((getGroovy() == null) ? 0 : getGroovy().hashCode());
        result = prime * result + ((getTransit() == null) ? 0 : getTransit().hashCode());
        return result;
    }

    @Override
    public String toString() {
        String sb = getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", groupId=" + groupId +
                ", groupName=" + groupName +
                ", topicId=" + topicId +
                ", topicName=" + topicName +
                ", clusterId=" + clusterId +
                ", clusterName=" + clusterName +
                ", pressureTraffic=" + pressureTraffic +
                ", maxTps=" + maxTps +
                ", alarmType=" + alarmType +
                ", alarmIsEnable=" + alarmIsEnable +
                ", alarmLevel=" + alarmLevel +
                ", alarmMsgLag=" + alarmMsgLag +
                ", alarmDelayTime=" + alarmDelayTime +
                ", apiType=" + apiType +
                ", consumeTimeout=" + consumeTimeout +
                ", errorRetryTimes=" + errorRetryTimes +
                ", retryIntervals=" + retryIntervals +
                ", msgType=" + msgType +
                ", enableGroovy=" + enableGroovy +
                ", enableTransit=" + enableTransit +
                ", enableOrder=" + enableOrder +
                ", orderKey=" + orderKey +
                ", consumeType=" + consumeType +
                ", bigDataType=" + bigDataType +
                ", bigDataConfig=" + bigDataConfig +
                ", urls=" + urls +
                ", httpMethod=" + httpMethod +
                ", httpHeaders=" + httpHeaders +
                ", httpQueryParams=" + httpQueryParams +
                ", msgPushType=" + msgPushType +
                ", httpToken=" + httpToken +
                ", pushMaxConcurrency=" + pushMaxConcurrency +
                ", actions=" + actions +
                ", config=" + config +
                ", state=" + state +
                ", extraParams=" + extraParams +
                ", remark=" + remark +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", groovy=" + groovy +
                ", transit=" + transit +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}