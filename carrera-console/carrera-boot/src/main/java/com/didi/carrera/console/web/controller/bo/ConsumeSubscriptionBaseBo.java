package com.didi.carrera.console.web.controller.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionApiType;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionBigDataType;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionConsumeType;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionMsgPushType;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionMsgType;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.web.controller.validator.AnotherFieldEqualsSpecifiedValue;
import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.Actions;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


@AnotherFieldEqualsSpecifiedValue.List({
        @AnotherFieldEqualsSpecifiedValue(fieldName = "apiType", fieldValue = "1", dependFieldName = "msgType", message = "消息类型不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "apiType", fieldValue = "1", dependFieldName = "consumeType", message = "消费类型不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "apiType", fieldValue = "1", dependFieldName = "enableOrder", message = "是否启用顺序消费不能为空"),

        @AnotherFieldEqualsSpecifiedValue(fieldName = "msgType", fieldValue = "1", dependFieldName = "enableGroovy", message = "是否启用Groovy不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "msgType", fieldValue = "1", dependFieldName = "enableTransit", message = "是否启用Transit不能为空"),

        @AnotherFieldEqualsSpecifiedValue(fieldName = "enableGroovy", fieldValue = "0", dependFieldName = "groovy", message = "groovy不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "enableTransit", fieldValue = "0", dependFieldName = "transit", message = "transit不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "enableOrder", fieldValue = "0", dependFieldName = "orderKey", message = "orderKey不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeType", fieldValue = "2", dependFieldName = "urls", message = "urls不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeType", fieldValue = "2", dependFieldName = "httpMethod", message = "HttpMethod不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeType", fieldValue = "2", dependFieldName = "pushMaxConcurrency", message = "推送并发不能为空"),

        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeType", fieldValue = "3", dependFieldName = "bigDataType", message = "写入类型不能为空"),
        @AnotherFieldEqualsSpecifiedValue(fieldName = "consumeType", fieldValue = "3", dependFieldName = "bigDataConfig", message = "写入配置不能为空")
})
public class ConsumeSubscriptionBaseBo extends BaseOrderBo {

    @NotNull(message = "subId不能为空")
    private Long subId;

    @NotNull(message = "消费组id不能为空")
    private Long groupId;
    private String groupName;

    @NotNull(message = "topicId不能为空")
    private Long topicId;
    private String topicName;

    @NotNull(message = "消费限流不能为空")
    @Min(value = 1, message = "消费限流必须大于0")
    private Double maxTps;

    @NotNull(message = "是否接收压测流量不能为空")
    @Range(min = 0, max = 1, message = "是否接收压测流量只能是0不接收 1接收")
    private Byte pressureTraffic;

    @NotNull(message = "报警类型不能为空")
    @Range(min = 0, max = 1, message = "报警类型只能是继承消费组配置或单独配置")
    private Byte alarmType;

    private Byte alarmIsEnable = 0;
    private Integer alarmMsgLag = 10000;
    private Integer alarmDelayTime = 300000;

    @NotNull(message = "是否启用lowlevel不能为空")
    @Range(min = 1, max = 2, message = "是否启用lowlevel只能是1禁用 2启用")
    private Byte apiType;

    private Integer consumeTimeout = 1000;

    private Integer errorRetryTimes = 3;
    private List<Integer> retryIntervals = Lists.newArrayList(50, 100, 150);

    @Range(min = 1, max = 3, message = "消息类型 1Json 2Text 3Bytes")
    private Byte msgType = ConsumeSubscriptionMsgType.BINARY.getIndex();

    @Range(min = 0, max = 1, message = "是否启用Groovy只能是 0启用 1禁用")
    private Byte enableGroovy = IsEnable.DISABLE.getIndex();

    @Range(min = 0, max = 1, message = "是否启用Transit只能是 0启用 1禁用")
    private Byte enableTransit = IsEnable.DISABLE.getIndex();
    private String groovy;
    private Map<String, String> transit;

    @Range(min = 0, max = 1, message = "是否启用顺序消费只能是 0启用 1禁用")
    private Byte enableOrder = IsEnable.DISABLE.getIndex();
    private String orderKey;

    @Range(min = 1, max = 3, message = "消费类型只能是 1SDK 2HTTP 3直写第三方组件")
    private Byte consumeType = ConsumeSubscriptionConsumeType.SDK.getIndex();
    private List<String> urls;

    private Byte httpMethod;
    private Map<String, String> httpHeaders;
    private Map<String, String> httpQueryParams;

    private Byte msgPushType;
    private String httpToken;
    private Integer pushMaxConcurrency;

    private Byte bigDataType;
    private String bigDataConfig;

    private Map<String, String> extraParams;

    private Map<String, String> operationParams;

    private Boolean useNonBlockAsync = false;

    public Long getSubId() {
        return subId;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
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

    public List<Integer> getRetryIntervals() {
        return retryIntervals;
    }

    public void setRetryIntervals(List<Integer> retryIntervals) {
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

    public String getGroovy() {
        return groovy;
    }

    public void setGroovy(String groovy) {
        this.groovy = groovy;
    }

    public Map<String, String> getTransit() {
        return transit;
    }

    public void setTransit(Map<String, String> transit) {
        this.transit = transit;
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
        this.orderKey = orderKey;
    }

    public Byte getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(Byte consumeType) {
        this.consumeType = consumeType;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public Byte getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Byte httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public Map<String, String> getHttpQueryParams() {
        return httpQueryParams;
    }

    public void setHttpQueryParams(Map<String, String> httpQueryParams) {
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
        this.httpToken = httpToken;
    }

    public Integer getPushMaxConcurrency() {
        return pushMaxConcurrency;
    }

    public void setPushMaxConcurrency(Integer pushMaxConcurrency) {
        this.pushMaxConcurrency = pushMaxConcurrency;
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
        this.bigDataConfig = bigDataConfig;
    }

    public Map<String, String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, String> extraParams) {
        this.extraParams = extraParams;
    }

    public Byte getPressureTraffic() {
        return pressureTraffic;
    }

    public void setPressureTraffic(Byte pressureTraffic) {
        this.pressureTraffic = pressureTraffic;
    }

    public Map<String, String> getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(Map<String, String> operationParams) {
        this.operationParams = operationParams;
    }

    @JSONField(serialize = false)
    public boolean isUseNonBlockAsync() {
        return useNonBlockAsync;
    }

    public void setUseNonBlockAsync(boolean useNonBlockAsync) {
        this.useNonBlockAsync = useNonBlockAsync;
    }

    @JSONField(serialize = false)
    public boolean isModify() {
        return subId != null && subId > 0;
    }

    public ConsumeSubscription buildConsumeSubscription() {
        ConsumeSubscription subscription = new ConsumeSubscription();
        BeanUtils.copyProperties(this, subscription);
        subscription.setId(this.getSubId());
        subscription.setSubExtraParams(this.getExtraParams());
        subscription.setSubActions(this.buildActions());
        subscription.setSubHttpHeaders(this.getHttpHeaders());
        subscription.setSubHttpQueryParams(this.getHttpQueryParams());
        subscription.setSubRetryIntervals(this.getRetryIntervals());
        subscription.setSubTransit(this.getTransit());
        subscription.setSubUrls(this.getUrls());

        return subscription;
    }

    private void validate() {
        if (msgType != null && (msgType == ConsumeSubscriptionMsgType.TEXT.getIndex() || msgType == ConsumeSubscriptionMsgType.BINARY.getIndex()) && IsEnable.isEnable(enableOrder) && StringUtils.isNotEmpty(orderKey) && !ORDER_BY_QID_KEY.equalsIgnoreCase(orderKey) && !ORDER_BY_MSGKEY_KEY.equalsIgnoreCase(orderKey)) {
            throw new RuntimeException("JsonPath顺序消费只能消息格式为Json类型的使用");
        }
    }

    /**
     * 规则：MsgType=JSON, Transit、GroovyFilter、FormParams2、QueryParams时，Json action开头
     * MsgType != JSON, 以Async开头，
     *
     * @return
     */
    public List<String> buildActions() {
        validate();

        List<String> actionList = Lists.newArrayList();

        if (this.getApiType() == ConsumeSubscriptionApiType.LOW_LEVEL.getIndex()) {
            actionList.add(Actions.LowLevel);
            actionList.add(Actions.PULL_SERVER);
            return actionList;
        }

        if (containsHdfsAction()) {
            actionList.add(Actions.ASYNC);
            actionList.add(Actions.HDFS);
            return actionList;
        }

        if (containsHbaseAction()) {
            actionList.add(Actions.ASYNC);
            actionList.add(Actions.HBASE);
            return actionList;
        }

        //头部顺序
        if (containsJsonAction()) {
            if (actionIsEnableOrderByJson()) {
                actionList.add(Actions.JSON);
                actionList.add(Actions.ASYNC);
            } else {
                actionList.add(Actions.ASYNC);
                actionList.add(Actions.JSON);
            }
        } else {
            actionList.add(Actions.ASYNC);
        }

        //中部顺序
        if (containsActionOrderTransitGroovy()) {
            if (containsTransitAction()) {
                actionList.add(Actions.TRANSIT);
            }
            if (containsGroovyAction()) {
                actionList.add(Actions.GROOVY);
            }
        } else {
            if (containsGroovyAction()) {
                actionList.add(Actions.GROOVY);
            }
            if (containsTransitAction()) {
                actionList.add(Actions.TRANSIT);
            }
        }


        //结尾顺序
        if (containsRedisAction()) {
            actionList.add(Actions.REDIS);
        } else if (containsPullServerAction()) {
            actionList.add(Actions.PULL_SERVER);
        } else if (containsHttpAction()) {
            if (MapUtils.isNotEmpty(this.getHttpQueryParams())) {
                actionList.add(Actions.QueryParams);
            }
            if (containsFormParamsAction()) {
                actionList.add(Actions.FormParams);
            } else if (containsFormParams2Action()) {
                actionList.add(Actions.FormParams2);
            }

            actionList.add(Actions.ASYNC_HTTP);
        }

        if (containsForceSyncFlag() && containsGroovyAction() && actionList.indexOf(Actions.ASYNC) > -1) {
            actionList.remove(Actions.ASYNC);
        }

        int index;
        if (isUseNonBlockAsync() && (index = actionList.indexOf(Actions.ASYNC)) > -1) {
            actionList.set(index, Actions.NONBLOCKASYNC);
        }

        return actionList;
    }

    private boolean containsForceSyncFlag() {
        return MapUtils.isNotEmpty(getExtraParams()) && getExtraParams().containsKey(SUB_FLAG_ACTION_IGNORE_ASYNC) && "true".equalsIgnoreCase(getExtraParams().get(SUB_FLAG_ACTION_IGNORE_ASYNC));
    }

    public static final String SUB_FLAG_ACTION_REDIS = "ACTION_REDIS";
    public static final String SUB_FLAG_ACTION_ORDER_TRANSIT_GROOVY = "ACTION_ORDER_TRANSIT_GROOVY";
    public static final String SUB_FLAG_ACTION_FORMPARAMS_HTTP_IGNORE_JSON = "FORMPARAMS_HTTP_IGNORE_JSON";
    public static final String SUB_FLAG_ACTION_FORMPARAMS2_HTTP_IGNORE_JSON = "FORMPARAMS2_HTTP_IGNORE_JSON";

    public static final String ORDER_BY_QID_KEY = "QID";
    public static final String ORDER_BY_MSGKEY_KEY = "KEY";

    public static final String SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER = "SUB_MQCLUSTER";
    public static final String SUB_FLAG_ACTION_IGNORE_ASYNC = "forceSync";

    private boolean containsFormParams2Action() {
        return this.getMsgPushType() != null && this.getMsgPushType() == ConsumeSubscriptionMsgPushType.FORM_PARAMS2.getIndex();
    }

    private boolean containsFormParamsAction() {
        return this.getMsgPushType() != null && this.getMsgPushType() == ConsumeSubscriptionMsgPushType.FORM_PARAMS.getIndex();
    }

    private boolean containsPullServerAction() {
        return this.getConsumeType() != null && this.getConsumeType() == ConsumeSubscriptionConsumeType.SDK.getIndex();
    }

    private boolean containsHttpAction() {
        return this.getConsumeType() != null && this.getConsumeType() == ConsumeSubscriptionConsumeType.HTTP.getIndex();
    }

    private boolean containsHdfsAction() {
        return this.getConsumeType() != null && this.getConsumeType() == ConsumeSubscriptionConsumeType.BIG_DATA.getIndex() && this.getBigDataType() != null && this.getBigDataType() == ConsumeSubscriptionBigDataType.HDFS.getIndex();
    }

    private boolean containsHbaseAction() {
        return this.getConsumeType() != null && this.getConsumeType() == ConsumeSubscriptionConsumeType.BIG_DATA.getIndex() && this.getBigDataType() != null && this.getBigDataType() == ConsumeSubscriptionBigDataType.HBASE.getIndex();
    }

    private boolean containsActionOrderTransitGroovy() {
        return MapUtils.isNotEmpty(this.getExtraParams()) && "true".equalsIgnoreCase(this.getExtraParams().get(SUB_FLAG_ACTION_ORDER_TRANSIT_GROOVY));
    }

    private boolean containsRedisAction() {
        return MapUtils.isNotEmpty(this.getExtraParams()) && "true".equalsIgnoreCase(this.getExtraParams().get(SUB_FLAG_ACTION_REDIS));
    }

    private boolean containsFormHttpIgnoreJson() {
        return MapUtils.isNotEmpty(this.getExtraParams()) && "true".equalsIgnoreCase(this.getExtraParams().get(SUB_FLAG_ACTION_FORMPARAMS_HTTP_IGNORE_JSON));
    }

    private boolean containsForm2HttpIgnoreJson() {
        return MapUtils.isNotEmpty(this.getExtraParams()) && "true".equalsIgnoreCase(this.getExtraParams().get(SUB_FLAG_ACTION_FORMPARAMS2_HTTP_IGNORE_JSON));
    }

    private boolean formParamsHttpIgnoreJson() {
        return containsFormParamsAction() && containsFormHttpIgnoreJson();
    }

    private boolean formParams2HttpIgnoreJson() {
        return containsFormParams2Action() && containsForm2HttpIgnoreJson();
    }

    private boolean formParamsContainsJson() {
        return containsHttpAction() && containsFormParamsAction() && !formParamsHttpIgnoreJson();
    }

    private boolean formParams2ContainsJson() {
        return containsHttpAction() && containsFormParams2Action() && !formParams2HttpIgnoreJson();
    }

    private boolean containsJsonAction() {
        return actionIsEnableOrderByJson() || containsGroovyAction() || containsTransitAction() || containsRedisAction()
                || (
                (formParamsContainsJson() || formParams2ContainsJson() || (MapUtils.isNotEmpty(this.getHttpQueryParams())))
                        && msgType == ConsumeSubscriptionMsgType.JSON.getIndex()
        );
    }

    private boolean actionIsEnableOrderByJson() {
        return IsEnable.isEnable(this.getEnableOrder()) && StringUtils.isNotEmpty(this.getOrderKey()) && !ORDER_BY_QID_KEY.equalsIgnoreCase(orderKey) && !ORDER_BY_MSGKEY_KEY.equalsIgnoreCase(this.getOrderKey());
    }

    private boolean containsTransitAction() {
        return IsEnable.isEnable(this.getEnableTransit()) && MapUtils.isNotEmpty(this.getTransit());
    }

    private boolean containsGroovyAction() {
        return IsEnable.isEnable(this.getEnableGroovy()) && StringUtils.isNotEmpty(this.getGroovy());
    }

    @Override
    public String toString() {
        return "ConsumeSubscriptionOrderBo{" +
                "subId=" + subId +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", maxTps=" + maxTps +
                ", pressureTraffic=" + pressureTraffic +
                ", alarmType=" + alarmType +
                ", alarmIsEnable=" + alarmIsEnable +
                ", alarmMsgLag=" + alarmMsgLag +
                ", alarmDelayTime=" + alarmDelayTime +
                ", apiType=" + apiType +
                ", consumeTimeout=" + consumeTimeout +
                ", errorRetryTimes=" + errorRetryTimes +
                ", retryIntervals=" + retryIntervals +
                ", msgType=" + msgType +
                ", enableGroovy=" + enableGroovy +
                ", enableTransit=" + enableTransit +
                ", groovy='" + groovy + '\'' +
                ", transit=" + transit +
                ", enableOrder=" + enableOrder +
                ", orderKey='" + orderKey + '\'' +
                ", consumeType=" + consumeType +
                ", urls=" + urls +
                ", httpMethod=" + httpMethod +
                ", httpHeaders=" + httpHeaders +
                ", httpQueryParams=" + httpQueryParams +
                ", msgPushType=" + msgPushType +
                ", httpToken='" + httpToken + '\'' +
                ", pushMaxConcurrency=" + pushMaxConcurrency +
                ", bigDataType=" + bigDataType +
                ", bigDataConfig='" + bigDataConfig + '\'' +
                ", extraParams=" + extraParams +
                ", operationParams=" + operationParams +
                ", useNonBlockAsync=" + useNonBlockAsync +
                "} " + super.toString();
    }
}