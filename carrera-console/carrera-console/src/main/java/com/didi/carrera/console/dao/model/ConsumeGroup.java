package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.dao.model.custom.ConsumeGroupConfig;
import org.apache.commons.lang3.StringUtils;


public class ConsumeGroup implements Serializable {
    /**
     * 主键id
     * default = null
     * length = 20
     */
    private Long id;

    /**
     * groupname
     * default =
     * length = 256
     */
    private String groupName;

    /**
     * 成本分摊方
     * default =
     * length = 256
     */
    private String service;

    /**
     * 部门，;分割
     * default =
     * length = 256
     */
    private String department;

    /**
     * 负责rd，;分割
     * default =
     * length = 512
     */
    private String contacters;

    /**
     * 0启用报警 1禁用报警
     * default = 0
     * length = 3
     */
    private Byte alarmIsEnable;

    /**
     * 报警组信息, ;分割
     * default =
     * length = 256
     */
    private List<String> alarmGroup;

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
     * 是否广播消费 0启用 1禁用，默认1
     * default = 1
     * length = 3
     */
    private Byte broadcastConsume;

    /**
     * 0同机房消费 1跨机房消费 2自定义，默认1
     * default = 1
     * length = 3
     */
    private Byte consumeMode;

    /**
     * 自定义消费模式，map结构，key为client idc，value为cproxy idc列表
     * default =
     * length = 2048
     */
    private Map<Long, List<Long>> consumeModeMapper;

    /**
     * 额外参数
     * default =
     * length = 2048
     */
    private Map<String, String> extraParams;

    /**
     * 运维端配置参数
     * default =
     * length = 2048
     */
    private ConsumeGroupConfig config;

    /**
     * group 备注
     * default =
     * length = 256
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

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
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

    public Byte getAlarmIsEnable() {
        return alarmIsEnable;
    }

    public void setAlarmIsEnable(Byte alarmIsEnable) {
        this.alarmIsEnable = alarmIsEnable;
    }

    public String getAlarmGroup() {
        return this.alarmGroup == null ? null : FastJsonUtils.toJsonString(alarmGroup);
    }

    public void setAlarmGroup(String alarmGroup) {
        this.alarmGroup = StringUtils.isBlank(alarmGroup) ? null : FastJsonUtils.toObject(alarmGroup, List.class);
    }

    public List<String> getGroupAlarmGroup() {
        return alarmGroup;
    }

    public void setGroupAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
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

    public Byte getBroadcastConsume() {
        return broadcastConsume;
    }

    public void setBroadcastConsume(Byte broadcastConsume) {
        this.broadcastConsume = broadcastConsume;
    }

    public Byte getConsumeMode() {
        return consumeMode;
    }

    public void setConsumeMode(Byte consumeMode) {
        this.consumeMode = consumeMode;
    }

    public String getConsumeModeMapper() {
        return this.consumeModeMapper == null ? null : FastJsonUtils.toJsonString(consumeModeMapper);
    }

    public void setConsumeModeMapper(String consumeModeMapper) {
        this.consumeModeMapper = StringUtils.isBlank(consumeModeMapper) ? null : FastJsonUtils.toObject(consumeModeMapper, new TypeReference<Map<Long/**client idc*/, List<Long>/*server idc*/>>() {
        });
    }

    public Map<Long/**client idc*/, List<Long>/*server idc*/> getGroupConsumeModeMapper() {
        return consumeModeMapper;
    }

    public void setGroupConsumeModeMapper(Map<Long/**client idc*/, List<Long>/*server idc*/> consumeModeMapper) {
        this.consumeModeMapper = consumeModeMapper;
    }

    public String getExtraParams() {
        return this.extraParams == null ? null : FastJsonUtils.toJsonString(extraParams);
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = StringUtils.isBlank(extraParams) ? null : FastJsonUtils.toObject(extraParams, Map.class);
    }

    public Map<String, String> getGroupExtraParams() {
        return extraParams;
    }

    public void setGroupExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public ConsumeGroupConfig getConsumeGroupConfig() {
        return config;
    }

    public void setConsumeGroupConfig(ConsumeGroupConfig config) {
        this.config = config;
    }

    public String getConfig() {
        return this.config == null ? null : FastJsonUtils.toJsonString(config);
    }

    public void setConfig(String config) {
        this.config = StringUtils.isBlank(config) ? null : FastJsonUtils.toObject(config, ConsumeGroupConfig.class);
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
        ConsumeGroup other = (ConsumeGroup) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getGroupName() == null ? other.getGroupName() == null : this.getGroupName().equals(other.getGroupName()))
                && (this.getService() == null ? other.getService() == null : this.getService().equals(other.getService()))
                && (this.getDepartment() == null ? other.getDepartment() == null : this.getDepartment().equals(other.getDepartment()))
                && (this.getContacters() == null ? other.getContacters() == null : this.getContacters().equals(other.getContacters()))
                && (this.getAlarmIsEnable() == null ? other.getAlarmIsEnable() == null : this.getAlarmIsEnable().equals(other.getAlarmIsEnable()))
                && (this.getAlarmGroup() == null ? other.getAlarmGroup() == null : this.getAlarmGroup().equals(other.getAlarmGroup()))
                && (this.getAlarmLevel() == null ? other.getAlarmLevel() == null : this.getAlarmLevel().equals(other.getAlarmLevel()))
                && (this.getAlarmMsgLag() == null ? other.getAlarmMsgLag() == null : this.getAlarmMsgLag().equals(other.getAlarmMsgLag()))
                && (this.getAlarmDelayTime() == null ? other.getAlarmDelayTime() == null : this.getAlarmDelayTime().equals(other.getAlarmDelayTime()))
                && (this.getBroadcastConsume() == null ? other.getBroadcastConsume() == null : this.getBroadcastConsume().equals(other.getBroadcastConsume()))
                && (this.getConsumeMode() == null ? other.getConsumeMode() == null : this.getConsumeMode().equals(other.getConsumeMode()))
                && (this.getConsumeModeMapper() == null ? other.getConsumeModeMapper() == null : this.getConsumeModeMapper().equals(other.getConsumeModeMapper()))
                && (this.getExtraParams() == null ? other.getExtraParams() == null : this.getExtraParams().equals(other.getExtraParams()))
                && (this.getConfig() == null ? other.getConfig() == null : this.getConfig().equals(other.getConfig()))
                && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGroupName() == null) ? 0 : getGroupName().hashCode());
        result = prime * result + ((getService() == null) ? 0 : getService().hashCode());
        result = prime * result + ((getDepartment() == null) ? 0 : getDepartment().hashCode());
        result = prime * result + ((getContacters() == null) ? 0 : getContacters().hashCode());
        result = prime * result + ((getAlarmIsEnable() == null) ? 0 : getAlarmIsEnable().hashCode());
        result = prime * result + ((getAlarmGroup() == null) ? 0 : getAlarmGroup().hashCode());
        result = prime * result + ((getAlarmLevel() == null) ? 0 : getAlarmLevel().hashCode());
        result = prime * result + ((getAlarmMsgLag() == null) ? 0 : getAlarmMsgLag().hashCode());
        result = prime * result + ((getAlarmDelayTime() == null) ? 0 : getAlarmDelayTime().hashCode());
        result = prime * result + ((getBroadcastConsume() == null) ? 0 : getBroadcastConsume().hashCode());
        result = prime * result + ((getConsumeMode() == null) ? 0 : getConsumeMode().hashCode());
        result = prime * result + ((getConsumeModeMapper() == null) ? 0 : getConsumeModeMapper().hashCode());
        result = prime * result + ((getExtraParams() == null) ? 0 : getExtraParams().hashCode());
        result = prime * result + ((getConfig() == null) ? 0 : getConfig().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
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
                ", groupName=" + groupName +
                ", service=" + service +
                ", department=" + department +
                ", contacters=" + contacters +
                ", alarmIsEnable=" + alarmIsEnable +
                ", alarmGroup=" + alarmGroup +
                ", alarmLevel=" + alarmLevel +
                ", alarmMsgLag=" + alarmMsgLag +
                ", alarmDelayTime=" + alarmDelayTime +
                ", broadcastConsume=" + broadcastConsume +
                ", consumeMode=" + consumeMode +
                ", consumeModeMapper=" + consumeModeMapper +
                ", extraParams=" + extraParams +
                ", config=" + config +
                ", remark=" + remark +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}