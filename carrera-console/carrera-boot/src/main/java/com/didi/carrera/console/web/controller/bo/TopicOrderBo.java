package com.didi.carrera.console.web.controller.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.dao.model.custom.TopicConfConfig;
import com.didi.carrera.console.dao.model.custom.TopicConfig;
import com.didi.carrera.console.web.controller.validator.AnotherFieldEqualsSpecifiedValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.BeanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AnotherFieldEqualsSpecifiedValue.List({
        @AnotherFieldEqualsSpecifiedValue(fieldName = "enableSchemaVerify", fieldValue = "0", dependFieldName = "schema", message = "schema不能为空")
})
public class TopicOrderBo<T extends TopicConfBo> extends BaseOrderBo {

    @NotNull(message = "topicId不能为空")
    private Long topicId;

    @NotBlank(message = "Topic名称不能为空")
    @Length(min = 3, max = 128, message = "Topic长度必须3-128个字符")
    @Pattern(regexp = "[0-9a-zA-Z_\\-]+", message = "Topic名称只能输入数字、大小写字母、下划线、减号")
    private String topicName;

    @NotBlank(message = "分摊方不能为空")
    private String service;

    @NotBlank(message = "部门不能为空")
    private String department;

    @NotBlank(message = "负责RD不能为空")
    private String contacters;

    private String schema;

    private List<String> alarmGroup;

    @NotNull(message = "topic描述信息不能为空")
    private String description;

    @Range(min = 0, max = 1, message = "启用报警只能为0或1")
    private Byte alarmIsEnable = IsEnable.ENABLE.getIndex();

    @NotNull(message = "延时Topic设置不能为空")
    private Byte delayTopic;

    private Map<String, String> extraParams;

    private Byte enableSchemaVerify = IsEnable.DISABLE.getIndex();

    private Map<String, String> operationParams;

    @Valid
    @NotEmpty(message = "请选择集群配置信息")
    private List<T> conf;

    private Byte defaultPass = DefaultPassType.CONDITION_PASS.getIndex();

    @NotNull(message = "生产模式不能为空")
    @Range(min = 0, max = 1, message = "生产模式只能为同机房生产或其他")
    private Byte produceMode;

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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<String> getAlarmGroup() {
        return alarmGroup;
    }

    public void setAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public List<T> getConf() {
        return conf;
    }

    public void setConf(List<T> conf) {
        this.conf = conf;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Byte getDelayTopic() {
        return delayTopic;
    }

    public void setDelayTopic(Byte delayTopic) {
        this.delayTopic = delayTopic;
    }

    public Byte getAlarmIsEnable() {
        return alarmIsEnable;
    }

    public void setAlarmIsEnable(Byte alarmIsEnable) {
        this.alarmIsEnable = alarmIsEnable;
    }

    public Byte getDefaultPass() {
        return defaultPass;
    }

    public void setDefaultPass(Byte defaultPass) {
        this.defaultPass = defaultPass;
    }

    public Byte getEnableSchemaVerify() {
        return enableSchemaVerify;
    }

    public void setEnableSchemaVerify(Byte enableSchemaVerify) {
        this.enableSchemaVerify = enableSchemaVerify;
    }

    @JSONField(serialize = false)
    public boolean isModify() {
        return topicId != null && topicId > 0;
    }

    public Map<String, String> getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(Map<String, String> operationParams) {
        this.operationParams = operationParams;
    }

    public Byte getProduceMode() {
        return produceMode;
    }

    public void setProduceMode(Byte produceMode) {
        this.produceMode = produceMode;
    }

    public Topic buildTopic() {
        Topic topic = new Topic();
        BeanUtils.copyProperties(this, topic);
        topic.setId(this.getTopicId());
        topic.setTopicAlarmGroup(this.getAlarmGroup());
        topic.setTopicExtraParams(this.getExtraParams());
        topic.setTopicSchema(this.getSchema());

        String contacters = getContacters();
        if (!";".equals(contacters.substring(contacters.length() - 1))) {
            setContacters(getContacters() + ";");
            topic.setContacters(getContacters());
        }

        return topic;
    }

    public static TopicOrderBo<AcceptTopicConfBo> buildTopicOrderBo(Topic topic, List<TopicConf> confs) {
        TopicOrderBo<AcceptTopicConfBo> topicOrderBo = new TopicOrderBo<>();
        BeanUtils.copyProperties(topic, topicOrderBo);
        topicOrderBo.setTopicId(topic.getId());
        topicOrderBo.setAlarmGroup(topic.getTopicAlarmGroup());
        topicOrderBo.setExtraParams(topic.getTopicExtraParams());
        topicOrderBo.setSchema(topic.getTopicSchema());
        topicOrderBo.setDefaultPass(DefaultPassType.CONDITION_PASS.getIndex());
        // operationParams
        if (MapUtils.isEmpty(topicOrderBo.getOperationParams())) {
            topicOrderBo.setOperationParams(Maps.newHashMap());
        }
        TopicConfig config = topic.getTopicConfig();
        topicOrderBo.getOperationParams().put(TopicConfig.key_autoBatch, String.valueOf(config.isAutoBatch()));
        topicOrderBo.getOperationParams().put(TopicConfig.key_useCache, String.valueOf(config.isUseCache()));
        topicOrderBo.getOperationParams().put(TopicConfig.key_compressionType, String.valueOf(config.getCompressionType()));

        List<AcceptTopicConfBo> conf = Lists.newArrayListWithCapacity(confs.size());
        confs.forEach(c -> {
            AcceptTopicConfBo acceptTopicConfBo = new AcceptTopicConfBo();
            BeanUtils.copyProperties(c, acceptTopicConfBo);
            acceptTopicConfBo.setClientIdcMap(c.getTopicConfClientIdc());
            // operationParams
            if (MapUtils.isEmpty(acceptTopicConfBo.getOperationParams())) {
                acceptTopicConfBo.setOperationParams(Maps.newHashMap());
            }
            TopicConfConfig conConfig = c.getTopicConfConfig();
            if (conConfig == null || MapUtils.isEmpty(conConfig.getProxies())) {
                acceptTopicConfBo.getOperationParams().put(TopicConfConfig.key_proxies, "");
            } else {
                acceptTopicConfBo.getOperationParams().put(TopicConfConfig.key_proxies, FastJsonUtils.toJson(conConfig.getProxies()));
            }
            conf.add(acceptTopicConfBo);
        });
        topicOrderBo.setConf(conf);
        return topicOrderBo;
    }

    public List<TopicConf> buildTopicConf() {
        List<TopicConf> list = Lists.newArrayListWithCapacity(this.getConf().size());
        this.getConf().forEach(conf -> {
            TopicConf topicConf = conf.buildTopicConf();
            topicConf.setTopicId(this.getTopicId());
            topicConf.setTopicName(this.getTopicName());
            list.add(topicConf);
        });

        return list;
    }

    @Override
    public String toString() {
        return "TopicOrderBo{" +
                "topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", service='" + service + '\'' +
                ", department='" + department + '\'' +
                ", contacters='" + contacters + '\'' +
                ", schema='" + schema + '\'' +
                ", alarmGroup=" + alarmGroup +
                ", description='" + description + '\'' +
                ", alarmIsEnable=" + alarmIsEnable +
                ", delayTopic=" + delayTopic +
                ", extraParams=" + extraParams +
                ", enableSchemaVerify=" + enableSchemaVerify +
                ", operationParams=" + operationParams +
                ", conf=" + conf +
                ", defaultPass=" + defaultPass +
                ", produceMode=" + produceMode +
                "} " + super.toString();
    }

    public enum DefaultPassType {

        PASS((byte) 0, "直接自动审批通过"),
        CONDITION_PASS((byte) 1, "符合条件自动审批通过"),
        REJECT((byte) 2, "必须人工审核");

        private byte index;

        private String name;

        DefaultPassType(byte index, String name) {
            this.index = index;
            this.name = name;
        }

        public byte getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

        public static DefaultPassType getByIndex(byte index) {
            List<DefaultPassType> all = Arrays.asList(values());
            for (DefaultPassType level : all) {
                if (level.getIndex() == index) {
                    return level;
                }
            }
            return null;
        }

    }
}