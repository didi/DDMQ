package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.dao.model.custom.TopicConfig;
import org.apache.commons.lang3.StringUtils;


public class Topic implements Serializable {
    /**
     * 主键id
     * default = null
     * length = 20
     */
    private Long id;

    /**
     * topic名称
     * default =
     * length = 256
     */
    private String topicName;

    /**
     * 成本分摊方
     * default =
     * length = 256
     */
    private String service;

    /**
     * 部门 一级部门 - 二级部门
     * default =
     * length = 256
     */
    private String department;

    /**
     * 联系人，;分割
     * default =
     * length = 1024
     */
    private String contacters;

    /**
     * 报警组信息, ;分割
     * default =
     * length = 256
     */
    private List<String> alarmGroup;

    /**
     * 是否启用报警，0 启用 1禁用，默认启用
     * default = 0
     * length = 3
     */
    private Byte alarmIsEnable;

    /**
     * 是否是延时Topic，0 延时 1非延时，默认1
     * default = 1
     * length = 3
     */
    private Byte delayTopic;

    /**
     * 是否需要审核订阅信息 0需要审核订阅信息 1不需要审核订阅信息，默认1
     * default = 1
     * length = 3
     */
    private Byte needAuditSubinfo;

    /**
     * 是否启用schema校验 0启用 1禁用，默认1
     * default = 1
     * length = 3
     */
    private Byte enableSchemaVerify;

    /**
     * 0 同机房生产 1 自定义，默认0
     * default = 0
     * length = 3
     */
    private Byte produceMode;

    /**
     * 生产模式自定义，map结构，key为client idc，value为pproxy idc列表
     * default =
     * length = 2048
     */
    private Map<String/**client idc*/, List<String>/*pproxy idc*/> produceModeMapper;

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
    private TopicConfig config;

    /**
     * topic描述信息
     * default =
     * length = 1024
     */
    private String description;

    /**
     * 额外参数
     * default =
     * length = 2048
     */
    private Map<String, String> extraParams;

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
     * topicschema
     * default = null
     * length = 65535
     */
    private String topicSchema;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName == null ? null : topicName.trim();
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service == null ? null : service.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department == null ? null : department.trim();
    }

    public String getContacters() {
        return contacters;
    }

    public void setContacters(String contacters) {
        this.contacters = contacters == null ? null : contacters.trim();
    }

    public String getAlarmGroup() {
        return this.alarmGroup == null ? null : FastJsonUtils.toJsonString(alarmGroup);
    }

    public void setAlarmGroup(String alarmGroup) {
        this.alarmGroup = StringUtils.isBlank(alarmGroup) ? null : FastJsonUtils.toObject(alarmGroup, List.class);
    }

    public List<String> getTopicAlarmGroup() {
        return alarmGroup;
    }

    public void setTopicAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
    }

    public Byte getAlarmIsEnable() {
        return alarmIsEnable;
    }

    public void setAlarmIsEnable(Byte alarmIsEnable) {
        this.alarmIsEnable = alarmIsEnable;
    }

    public Byte getDelayTopic() {
        return delayTopic;
    }

    public void setDelayTopic(Byte delayTopic) {
        this.delayTopic = delayTopic;
    }

    public Byte getNeedAuditSubinfo() {
        return needAuditSubinfo;
    }

    public void setNeedAuditSubinfo(Byte needAuditSubinfo) {
        this.needAuditSubinfo = needAuditSubinfo;
    }

    public Byte getEnableSchemaVerify() {
        return enableSchemaVerify;
    }

    public void setEnableSchemaVerify(Byte enableSchemaVerify) {
        this.enableSchemaVerify = enableSchemaVerify;
    }

    public Byte getProduceMode() {
        return produceMode;
    }

    public void setProduceMode(Byte produceMode) {
        this.produceMode = produceMode;
    }

    public String getProduceModeMapper() {
        return this.produceModeMapper == null ? null : FastJsonUtils.toJsonString(produceModeMapper);
    }

    public void setProduceModeMapper(String produceModeMapper) {
        this.produceModeMapper = StringUtils.isBlank(produceModeMapper) ? null : FastJsonUtils.toObject(produceModeMapper, new TypeReference<Map<String/**client idc*/, List<String>/*pproxy idc*/>>() {
        });
    }

    public Map<String/**client idc*/, List<String>/*pproxy idc*/> getTopicProduceModeMapper() {
        return produceModeMapper;
    }

    public void setTopicProduceModeMapper(Map<String/**client idc*/, List<String>/*pproxy idc*/> produceModeMapper) {
        this.produceModeMapper = produceModeMapper;
    }


    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public TopicConfig getTopicConfig() {
        return config;
    }

    public void setTopicConfig(TopicConfig config) {
        this.config = config;
    }

    public String getConfig() {
        return this.config == null ? null : FastJsonUtils.toJsonString(config);
    }

    public void setConfig(String config) {
        this.config = StringUtils.isBlank(config) ? null : FastJsonUtils.toObject(config, TopicConfig.class);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getExtraParams() {
        return this.extraParams == null ? null : FastJsonUtils.toJsonString(extraParams);
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = StringUtils.isBlank(extraParams) ? null : FastJsonUtils.toObject(extraParams, Map.class);
    }

    public Map<String, String> getTopicExtraParams() {
        return extraParams;
    }

    public void setTopicExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
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

    public String getTopicSchema() {
        return topicSchema;
    }

    public void setTopicSchema(String topicSchema) {
        this.topicSchema = topicSchema == null ? null : topicSchema.trim();
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
        Topic other = (Topic) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
                && (this.getService() == null ? other.getService() == null : this.getService().equals(other.getService()))
                && (this.getDepartment() == null ? other.getDepartment() == null : this.getDepartment().equals(other.getDepartment()))
                && (this.getContacters() == null ? other.getContacters() == null : this.getContacters().equals(other.getContacters()))
                && (this.getAlarmGroup() == null ? other.getAlarmGroup() == null : this.getAlarmGroup().equals(other.getAlarmGroup()))
                && (this.getAlarmIsEnable() == null ? other.getAlarmIsEnable() == null : this.getAlarmIsEnable().equals(other.getAlarmIsEnable()))
                && (this.getDelayTopic() == null ? other.getDelayTopic() == null : this.getDelayTopic().equals(other.getDelayTopic()))
                && (this.getNeedAuditSubinfo() == null ? other.getNeedAuditSubinfo() == null : this.getNeedAuditSubinfo().equals(other.getNeedAuditSubinfo()))
                && (this.getEnableSchemaVerify() == null ? other.getEnableSchemaVerify() == null : this.getEnableSchemaVerify().equals(other.getEnableSchemaVerify()))
                && (this.getProduceMode() == null ? other.getProduceMode() == null : this.getProduceMode().equals(other.getProduceMode()))
                && (this.getProduceModeMapper() == null ? other.getProduceModeMapper() == null : this.getProduceModeMapper().equals(other.getProduceModeMapper()))
                && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
                && (this.getConfig() == null ? other.getConfig() == null : this.getConfig().equals(other.getConfig()))
                && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
                && (this.getExtraParams() == null ? other.getExtraParams() == null : this.getExtraParams().equals(other.getExtraParams()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()))
                && (this.getTopicSchema() == null ? other.getTopicSchema() == null : this.getTopicSchema().equals(other.getTopicSchema()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getService() == null) ? 0 : getService().hashCode());
        result = prime * result + ((getDepartment() == null) ? 0 : getDepartment().hashCode());
        result = prime * result + ((getContacters() == null) ? 0 : getContacters().hashCode());
        result = prime * result + ((getAlarmGroup() == null) ? 0 : getAlarmGroup().hashCode());
        result = prime * result + ((getAlarmIsEnable() == null) ? 0 : getAlarmIsEnable().hashCode());
        result = prime * result + ((getDelayTopic() == null) ? 0 : getDelayTopic().hashCode());
        result = prime * result + ((getNeedAuditSubinfo() == null) ? 0 : getNeedAuditSubinfo().hashCode());
        result = prime * result + ((getEnableSchemaVerify() == null) ? 0 : getEnableSchemaVerify().hashCode());
        result = prime * result + ((getProduceMode() == null) ? 0 : getProduceMode().hashCode());
        result = prime * result + ((getProduceModeMapper() == null) ? 0 : getProduceModeMapper().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getConfig() == null) ? 0 : getConfig().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getExtraParams() == null) ? 0 : getExtraParams().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
        result = prime * result + ((getTopicSchema() == null) ? 0 : getTopicSchema().hashCode());
        return result;
    }

    @Override
    public String toString() {
        String sb = getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", topicName=" + topicName +
                ", service=" + service +
                ", department=" + department +
                ", contacters=" + contacters +
                ", alarmGroup=" + alarmGroup +
                ", alarmIsEnable=" + alarmIsEnable +
                ", delayTopic=" + delayTopic +
                ", needAuditSubinfo=" + needAuditSubinfo +
                ", enableSchemaVerify=" + enableSchemaVerify +
                ", produceMode=" + produceMode +
                ", produceModeMapper=" + produceModeMapper +
                ", state=" + state +
                ", config=" + config +
                ", description=" + description +
                ", extraParams=" + extraParams +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", topicSchema=" + topicSchema +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}