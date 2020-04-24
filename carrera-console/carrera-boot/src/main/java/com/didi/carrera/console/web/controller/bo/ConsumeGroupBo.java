package com.didi.carrera.console.web.controller.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.dao.model.custom.ConsumeGroupConfig;
import com.didi.carrera.console.web.controller.validator.AnotherFieldEqualsSpecifiedValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;


@AnotherFieldEqualsSpecifiedValue.List({
        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeMode", fieldValue = "2", dependFieldName = "consumeModeMapper", message = "消费模式不能为空")
})
public class ConsumeGroupBo extends BaseBo {

    @NotNull(message = "消费组Id不能为空")
    private Long groupId;

    @Length(min = 3, max = 128, message = "消费组长度必须3-128个字符")
    @Pattern(regexp = "^[cg_][a-zA-Z0-9_\\-]+$", message = "消费组必须以cg_开头,其余只能使用字母数字、下划线、减号")
    private String groupName;

    @NotBlank(message = "分摊方不能为空")
    private String service;

    @NotBlank(message = "部门不能为空")
    private String department;

    @NotBlank(message = "负责RD不能为空")
    private String contacters;

    private List<String> alarmGroup;

    @NotNull(message = "报警级别不能为空")
    @Range(min = 1, max = 3, message = "报警级别为1-3级")
    private Byte alarmLevel;

    @NotNull(message = "启用报警不能为空")
    @Range(min = 0, max = 1, message = "启用报警只能为0或1")
    private Byte alarmIsEnable;

    @NotNull(message = "消息积压阈值不能为空")
    @Min(value = 1, message = "消息积压阈值必须大于0")
    private Integer alarmMsgLag;

    @NotNull(message = "消息延迟时间不能为空")
    @Min(value = 1, message = "消息延迟时间必须大于0，且为整数")
    private Integer alarmDelayTime;

    private Byte broadcastConsume = IsEnable.DISABLE.getIndex();

    private Map<String, String> extraParams;

    private Map<String, String> operationParams;

    @NotNull(message = "消费模式不能为空")
    @Range(min = 0, max = 2, message = "消费模式只能为同机房或跨机房或其他")
    private Byte consumeMode;

    /**
     * 自定义消费模式，map结构，key为client idc，value为cproxy idc列表
     * default =
     * length = 2048
     */
    private Map<String, List<Long>> consumeModeMapper;

    public Map<String, String> getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(Map<String, String> operationParams) {
        this.operationParams = operationParams;
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
        this.groupName = groupName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContacters() {
        return contacters;
    }

    public void setContacters(String contacters) {
        this.contacters = contacters;
    }

    public List<String> getAlarmGroup() {
        return alarmGroup;
    }

    public void setAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
    }

    public Byte getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(Byte alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public Byte getAlarmIsEnable() {
        return alarmIsEnable;
    }

    public void setAlarmIsEnable(Byte alarmIsEnable) {
        this.alarmIsEnable = alarmIsEnable;
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

    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    public Map<String, List<Long>> getConsumeModeMapper() {
        return consumeModeMapper;
    }

    @JSONField(serialize = false)
    public Map<Long, List<Long>> getConsumeModeLongMapper() {
        if (MapUtils.isEmpty(consumeModeMapper)) {
            return null;
        }
        Map<Long, List<Long>> mapper = Maps.newHashMap();
        for (Map.Entry<String, List<Long>> entry : consumeModeMapper.entrySet()) {
            mapper.put(Long.parseLong(entry.getKey()), Lists.newArrayList(entry.getValue()));
        }
        return mapper;
    }

    public void setConsumeModeMapper(Map<String, List<Long>> consumeModeMapper) {
        this.consumeModeMapper = consumeModeMapper;
    }

    @JSONField(serialize = false)
    public boolean isModify() {
        return groupId != null && groupId > 0;
    }

    public ConsumeGroup buildConsumeGroup() {
        ConsumeGroup group = new ConsumeGroup();
        BeanUtils.copyProperties(this, group);
        group.setId(this.getGroupId());
        group.setGroupAlarmGroup(this.getAlarmGroup());
        group.setGroupExtraParams(this.getExtraParams());

        String contacters = getContacters();
        if (!";".equals(contacters.substring(contacters.length() - 1))) {
            setContacters(getContacters() + ";");
            group.setContacters(getContacters());
        }
        return group;
    }

    public static ConsumeGroupBo buildConsumeGroupBo(ConsumeGroup group) {
        ConsumeGroupBo groupBo = new ConsumeGroupBo();
        BeanUtils.copyProperties(group, groupBo);
        groupBo.setGroupId(group.getId());
        groupBo.setAlarmGroup(group.getGroupAlarmGroup());
        groupBo.setExtraParams(group.getGroupExtraParams());

        if (MapUtils.isEmpty(groupBo.getOperationParams())) {
            groupBo.setOperationParams(Maps.newHashMap());
        }
        ConsumeGroupConfig config = group.getConsumeGroupConfig();
        groupBo.getOperationParams().put(ConsumeGroupConfig.key_asyncThreads, String.valueOf(config.getAsyncThreads()));
        groupBo.getOperationParams().put(ConsumeGroupConfig.key_redisConfigStr, StringUtils.isEmpty(config.getRedisConfigStr()) ? "" : config.getRedisConfigStr());

        return groupBo;
    }

    @Override
    public String toString() {
        return "ConsumeGroupBo{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", service='" + service + '\'' +
                ", department='" + department + '\'' +
                ", contacters='" + contacters + '\'' +
                ", alarmGroup=" + alarmGroup +
                ", alarmLevel=" + alarmLevel +
                ", alarmIsEnable=" + alarmIsEnable +
                ", alarmMsgLag=" + alarmMsgLag +
                ", alarmDelayTime=" + alarmDelayTime +
                ", broadcastConsume=" + broadcastConsume +
                ", extraParams=" + extraParams +
                ", operationParams=" + operationParams +
                ", consumeMode=" + consumeMode +
                ", consumeModeMapper=" + consumeModeMapper +
                "} " + super.toString();
    }
}