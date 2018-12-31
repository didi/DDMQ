package com.xiaojukeji.carrera.config.v4.cproxy;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaojukeji.carrera.config.Actions;
import com.xiaojukeji.carrera.config.AppendContext;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.utils.GroovyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;


public class UpstreamTopic implements ConfigurationValidator, Serializable, Cloneable {
    public static final Logger LOGGER = getLogger(UpstreamTopic.class);

    private static final long serialVersionUID = -5774216290569923768L;
    public static final List<Integer> DEFAULT_RETRY_INTERVAL = Lists.newArrayList(100, 150, 300);
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";

    public static final String ORDER_BY_QID = "QID";
    public static final String ORDER_BY_KEY = "KEY";

    public static final int DEFAULT_MAX_CONSUME_LAG_FACTOR = 3;

    private String brokerCluster;
    private Map<String/*proxyCluster*/, Set<String>> proxies = Maps.newHashMap();

    private boolean isEnabled;  // is subscription enabled

    private String topic;
    private List<String> tags;
    private List<String> actions;
    private int fetchThreads = 1;       // rmq / kafka client 的消费线程数.
    private double maxTps;              // kafka/rmq client拉取消息的最大tps
    private double totalMaxTps;         // kafka/rmq 真实最大tps
    private int concurrency = 1024;     // 同时未完成消费的消息数量.
    private long maxConsumeLag = -1;    // 第一条未完成消费的消息的offset 与 最后一条已完成消费的消息的offset的 最大差值. 默认-1,表示不限制. 取值为0表示：concurrency*DEFAULT_MAX_CONSUME_LAG_FACTOR;
    private String groovyScript;
    private int timeout = 5000;         //http 推送请求的超时时间; SDK消费时, 消息消费的超时时间.超时后,消息可以被重新消费.
    private boolean isPressureTraffic = false;

    /**
     * QID：按照qid有序消费。
     * KEY: 按照消息的key顺序消费。
     * JsonPath：按照指定的字段的取值有序消费。
     */
    private String orderKey = null;

    /**
     * 异常情况下的重试次数。叫maxErrorRetry更合适。
     * <p>
     * http 推送模式：由于异常或者status code!=200的原因导致的重试次数。
     * thrift 拉取模式：由于超时响应（包括网络异常）导致最大重试次数。
     * -1表示无限重试。
     */
    private int maxRetry = 2;

    /**
     * 业务逻辑要求的重试次数。
     * <p>
     * http 推送模式：对方返回非 com.xiaojukeji.carrera.cproxy.actions.AsyncHttpAction.HttpErrNo 中定义的错误码时的重试间隔。
     * thrift 拉取模式：client返回false时的重试间隔。
     * 重试次数为数组长度。
     * 最后一位为-1时，表示按照上一个重试间隔无限重试。
     */
    private List<Integer> retryIntervals = DEFAULT_RETRY_INTERVAL;

    //http 推送模式相关配置
    private List<String> urls;
    private String httpMethod = HTTP_POST; //POST 或者 GET。
    private Map<String, String/*JsonPath*/> queryParams;

    private List<AppendContext> appendContext; //在form parameters中添加一些字段。
    private Map<String/*JsonPath*/, String/*JsonPath*/> transit;
    private String tokenKey = ""; //在form parameters中添加token
    private double httpMaxTps = -1; //通过http推送的最大tps。默认-1，表示等于maxTps.

    private int maxPullBatchSize = 8; //SDK消费时,client一次拉取的小大消息数量.

    // 写入 hdfs 相关配置
    private HdfsConfiguration hdfsConfiguration;

    // 写入 hbase 相关配置
    private HBaseConfiguration hbaseconfiguration;

    private boolean enableAlarm = true;
    private long delayTimeThreshold = 300000L;
    private long committedLagThreshold = 10000L;

    public UpstreamTopic() {
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(String groovyScript) {
        this.groovyScript = groovyScript;
    }

    public double getHttpMaxTps() {
        return httpMaxTps;
    }

    public void setHttpMaxTps(double httpMaxTps) {
        this.httpMaxTps = httpMaxTps;
    }

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public Map<String, Set<String>> getProxies() {
        return proxies;
    }

    public void setProxies(Map<String, Set<String>> proxies) {
        this.proxies = proxies;
    }

    public boolean isEnableAlarm() {
        return enableAlarm;
    }

    public void setEnableAlarm(boolean enableAlarm) {
        this.enableAlarm = enableAlarm;
    }

    public long getDelayTimeThreshold() {
        return delayTimeThreshold;
    }

    public void setDelayTimeThreshold(long delayTimeThreshold) {
        this.delayTimeThreshold = delayTimeThreshold;
    }

    public long getCommittedLagThreshold() {
        return committedLagThreshold;
    }

    public void setCommittedLagThreshold(long committedLagThreshold) {
        this.committedLagThreshold = committedLagThreshold;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public long getMaxConsumeLag() {
        if (maxConsumeLag == 0) {
            return concurrency * DEFAULT_MAX_CONSUME_LAG_FACTOR;
        }
        return maxConsumeLag;
    }

    public void setMaxConsumeLag(long maxConsumeLag) {
        this.maxConsumeLag = maxConsumeLag;
    }

    public int getMaxPullBatchSize() {
        return maxPullBatchSize;
    }

    public void setMaxPullBatchSize(int maxPullBatchSize) {
        this.maxPullBatchSize = maxPullBatchSize;
    }

    public int getFetchThreads() {
        return fetchThreads;
    }

    public void setFetchThreads(int fetchThreads) {
        this.fetchThreads = fetchThreads;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public Map<String, String> getTransit() {
        return transit;
    }

    public List<Integer> getRetryIntervals() {
        return retryIntervals;
    }

    public void setRetryIntervals(List<Integer> retryIntervals) {
        this.retryIntervals = retryIntervals;
    }

    public void setTransit(Map<String, String> transit) {
        this.transit = transit;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public double getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(double maxTps) {
        this.maxTps = maxTps;
    }

    public double getTotalMaxTps() {
        return totalMaxTps;
    }

    public void setTotalMaxTps(double totalMaxTps) {
        this.totalMaxTps = totalMaxTps;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<AppendContext> getAppendContext() {
        return appendContext;
    }

    public void setAppendContext(List<AppendContext> appendContext) {
        this.appendContext = appendContext;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public boolean isPressureTraffic() {
        return isPressureTraffic;
    }

    public void setPressureTraffic(boolean pressureTraffic) {
        isPressureTraffic = pressureTraffic;
    }

    public HdfsConfiguration getHdfsConfiguration() {
        return hdfsConfiguration;
    }

    public void setHdfsConfiguration(HdfsConfiguration hdfsConfiguration) {
        this.hdfsConfiguration = hdfsConfiguration;
    }

    public HBaseConfiguration getHbaseconfiguration() {
        return hbaseconfiguration;
    }

    public void setHbaseconfiguration(HBaseConfiguration hbaseconfiguration) {
        this.hbaseconfiguration = hbaseconfiguration;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(this.brokerCluster))
            throw new ConfigException("[UpstreamTopic] brokerClusters empty");
        if (StringUtils.isEmpty(this.topic))
            throw new ConfigException("[UpstreamTopic] topic empty");
        if (CollectionUtils.isEmpty(this.actions))
            throw new ConfigException("[UpstreamTopic] actions empty");
        if (this.totalMaxTps <= 0)
            throw new ConfigException("[UpstreamTopic] totalMaxTps <= 0");
        if (this.concurrency <= 0)
            throw new ConfigException("[UpstreamTopic] concurrency <= 0");
        if (this.fetchThreads <= 0)
            throw new ConfigException("[UpstreamTopic] fetchThreads <= 0");
        if (StringUtils.isNotEmpty(this.orderKey)
                && !ORDER_BY_QID.equals(this.orderKey)
                && !ORDER_BY_KEY.equals(this.orderKey)
                && !validateJsonPath(this.orderKey))
            throw new ConfigException("[UpstreamTopic] orderKey error");
        for (String action : this.actions) {
            if (!Actions.isValidAction(action))
                throw new ConfigException("[UpstreamTopic] unknown action :" + action);
            if (action.equals(Actions.TRANSIT)) {
                if (MapUtils.isEmpty(transit))
                    throw new ConfigException("[UpstreamTopic] transit empty");
                for (Map.Entry<String, String> entry : transit.entrySet()) {
                    if (!validateJsonPath(entry.getKey()) || !validateJsonPath(entry.getValue()))
                        throw new ConfigException("[UpstreamTopic] invalid json path in transit:" + entry);
                }
            }
            if (action.equals(Actions.QueryParams)) {
                if (MapUtils.isEmpty(queryParams))
                    throw new ConfigException("[UpstreamTopic] queryParams empty");
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    if (!validateJsonPath(entry.getValue()))
                        throw new ConfigException("[UpstreamTopic] invalid json path in queryParams:" + entry);
                }
            }
            if (action.equals(Actions.GROOVY)) {
                if (StringUtils.isEmpty(groovyScript))
                    throw new ConfigException("[UpstreamTopic] groovyScript empty");
                if (!validateGroovy(groovyScript))
                    throw new ConfigException("[UpstreamTopic] invalid groovyScript");
            }
            if (action.equals(Actions.HDFS)) {
                if (hdfsConfiguration == null) {
                    throw new ConfigException("[UpstreamTopic] hdfsConfiguration empty");
                }
                if (!hdfsConfiguration.validate()) {
                    throw new ConfigException("[UpstreamTopic] invalid hdfsConfiguration");
                }
            }
            if (action.equals(Actions.HBASE)) {
                if (hbaseconfiguration == null) {
                    throw new ConfigException("[UpstreamTopic] hbaseconfiguration empty");
                }
                if (!hbaseconfiguration.validate()) {
                    throw new ConfigException("[UpstreamTopic] invalid hbaseconfiguration");
                }
            }
        }
        String lastAction = actions.get(actions.size() - 1);
        if (Actions.ASYNC_HTTP.equals(lastAction)) {
            if (this.timeout <= 0) throw new ConfigException("[UpstreamTopic] timeout <= 0");
            if (!StringUtils.equalsIgnoreCase(this.httpMethod, HTTP_GET)
                    && !StringUtils.equalsIgnoreCase(this.httpMethod, HTTP_POST))
                throw new ConfigException("[UpstreamTopic] invalid httpMethod:" + httpMethod);
            if (CollectionUtils.isEmpty(this.urls))
                throw new ConfigException("[UpstreamTopic] urls empty");
            for (String url : this.urls) {
                if (!url.startsWith("http://")) {
                    throw new ConfigException("[UpstreamTopic] url not start with http://");
                }
            }
        } else if (Actions.PULL_SERVER.equals(lastAction)) {
            if (this.timeout <= 0) throw new ConfigException("timeout <= 0");
            if (maxPullBatchSize <= 0)
                throw new ConfigException("[UpstreamTopic] maxPullBatchSize <= 0");
        } else if (Actions.REDIS.equals(lastAction)) {
            return true;
        } else if (Actions.HDFS.equals(lastAction)) {
            return true;
        } else if (Actions.HBASE.equals(lastAction)) {
            return true;
        } else {
            throw new ConfigException("[UpstreamTopic] invalid last action:" + lastAction);
        }
        return true;
    }

    private boolean validateJsonPath(String jsonPath) {
        try {
            CommonFastJsonUtils.setValueByPath(new JSONObject(), jsonPath, "validateJsonPath");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean validateGroovy(String groovy) {
        try {
            GroovyUtils.parseClass(groovy);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public UpstreamTopic clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), new TypeReference<UpstreamTopic>() {
        });
    }

    public boolean checkLowLevel() {
        return actions.contains(Actions.LowLevel);
    }

    @Override
    public String toString() {
        return "UpstreamTopic{" +
                ", brokerCluster='" + brokerCluster + '\'' +
                ", proxies=" + proxies +
                ", isEnabled=" + isEnabled +
                ", topic='" + topic + '\'' +
                ", tags=" + tags +
                ", actions=" + actions +
                ", fetchThreads=" + fetchThreads +
                ", maxTps=" + maxTps +
                ", totalMaxTps=" + totalMaxTps +
                ", concurrency=" + concurrency +
                ", maxConsumeLag=" + maxConsumeLag +
                ", groovyScript='" + groovyScript + '\'' +
                ", timeout=" + timeout +
                ", isPressureTraffic=" + isPressureTraffic +
                ", orderKey='" + orderKey + '\'' +
                ", maxRetry=" + maxRetry +
                ", retryIntervals=" + retryIntervals +
                ", urls=" + urls +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryParams=" + queryParams +
                ", appendContext=" + appendContext +
                ", transit=" + transit +
                ", tokenKey='" + tokenKey + '\'' +
                ", httpMaxTps=" + httpMaxTps +
                ", maxPullBatchSize=" + maxPullBatchSize +
                ", hdfsConfiguration=" + hdfsConfiguration +
                ", hbaseconfiguration=" + hbaseconfiguration +
                ", enableAlarm=" + enableAlarm +
                ", delayTimeThreshold=" + delayTimeThreshold +
                ", committedLagThreshold=" + committedLagThreshold +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpstreamTopic topic1 = (UpstreamTopic) o;

        if (isEnabled != topic1.isEnabled) return false;
        if (fetchThreads != topic1.fetchThreads) return false;
        if (Double.compare(topic1.maxTps, maxTps) != 0) return false;
        if (Double.compare(topic1.totalMaxTps, totalMaxTps) != 0) return false;
        if (concurrency != topic1.concurrency) return false;
        if (maxConsumeLag != topic1.maxConsumeLag) return false;
        if (timeout != topic1.timeout) return false;
        if (isPressureTraffic != topic1.isPressureTraffic) return false;
        if (maxRetry != topic1.maxRetry) return false;
        if (Double.compare(topic1.httpMaxTps, httpMaxTps) != 0) return false;
        if (maxPullBatchSize != topic1.maxPullBatchSize) return false;
        if (enableAlarm != topic1.enableAlarm) return false;
        if (delayTimeThreshold != topic1.delayTimeThreshold) return false;
        if (committedLagThreshold != topic1.committedLagThreshold) return false;
        if (brokerCluster != null ? !brokerCluster.equals(topic1.brokerCluster) : topic1.brokerCluster != null)
            return false;
        if (proxies != null ? !proxies.equals(topic1.proxies) : topic1.proxies != null) return false;
        if (topic != null ? !topic.equals(topic1.topic) : topic1.topic != null) return false;
        if (tags != null ? !tags.equals(topic1.tags) : topic1.tags != null) return false;
        if (actions != null ? !actions.equals(topic1.actions) : topic1.actions != null) return false;
        if (groovyScript != null ? !groovyScript.equals(topic1.groovyScript) : topic1.groovyScript != null)
            return false;
        if (orderKey != null ? !orderKey.equals(topic1.orderKey) : topic1.orderKey != null) return false;
        if (retryIntervals != null ? !retryIntervals.equals(topic1.retryIntervals) : topic1.retryIntervals != null)
            return false;
        if (urls != null ? !urls.equals(topic1.urls) : topic1.urls != null) return false;
        if (httpMethod != null ? !httpMethod.equals(topic1.httpMethod) : topic1.httpMethod != null) return false;
        if (queryParams != null ? !queryParams.equals(topic1.queryParams) : topic1.queryParams != null) return false;
        if (appendContext != null ? !appendContext.equals(topic1.appendContext) : topic1.appendContext != null)
            return false;
        if (transit != null ? !transit.equals(topic1.transit) : topic1.transit != null) return false;
        if (tokenKey != null ? !tokenKey.equals(topic1.tokenKey) : topic1.tokenKey != null) return false;
        return hdfsConfiguration != null ? hdfsConfiguration.equals(topic1.hdfsConfiguration) : topic1.hdfsConfiguration == null;
    }

    public boolean bizEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpstreamTopic topic1 = (UpstreamTopic) o;

        if (isEnabled != topic1.isEnabled) return false;
        if (fetchThreads != topic1.fetchThreads) return false;
        if (Double.compare(topic1.totalMaxTps, totalMaxTps) != 0) return false;
        if (concurrency != topic1.concurrency) return false;
        if (maxConsumeLag != topic1.maxConsumeLag) return false;
        if (timeout != topic1.timeout) return false;
        if (isPressureTraffic != topic1.isPressureTraffic) return false;
        if (maxRetry != topic1.maxRetry) return false;
        if (Double.compare(topic1.httpMaxTps, httpMaxTps) != 0) return false;
        if (maxPullBatchSize != topic1.maxPullBatchSize) return false;
        if (brokerCluster != null ? !brokerCluster.equals(topic1.brokerCluster) : topic1.brokerCluster != null)
            return false;
        if (topic != null ? !topic.equals(topic1.topic) : topic1.topic != null) return false;
        if (tags != null ? !tags.equals(topic1.tags) : topic1.tags != null) return false;
        if (actions != null ? !actions.equals(topic1.actions) : topic1.actions != null) return false;
        if (groovyScript != null ? !groovyScript.equals(topic1.groovyScript) : topic1.groovyScript != null)
            return false;
        if (orderKey != null ? !orderKey.equals(topic1.orderKey) : topic1.orderKey != null) return false;
        if (retryIntervals != null ? !retryIntervals.equals(topic1.retryIntervals) : topic1.retryIntervals != null)
            return false;
        if (urls != null ? !urls.equals(topic1.urls) : topic1.urls != null) return false;
        if (httpMethod != null ? !httpMethod.equals(topic1.httpMethod) : topic1.httpMethod != null) return false;
        if (queryParams != null ? !queryParams.equals(topic1.queryParams) : topic1.queryParams != null) return false;
        if (appendContext != null ? !appendContext.equals(topic1.appendContext) : topic1.appendContext != null)
            return false;
        if (transit != null ? !transit.equals(topic1.transit) : topic1.transit != null) return false;
        if (tokenKey != null ? !tokenKey.equals(topic1.tokenKey) : topic1.tokenKey != null) return false;
        return hdfsConfiguration != null ? hdfsConfiguration.equals(topic1.hdfsConfiguration) : topic1.hdfsConfiguration == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        long temp;
        result = 31 * result + (brokerCluster != null ? brokerCluster.hashCode() : 0);
        result = 31 * result + (proxies != null ? proxies.hashCode() : 0);
        result = 31 * result + (isEnabled ? 1 : 0);
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + fetchThreads;
        temp = Double.doubleToLongBits(maxTps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(totalMaxTps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + concurrency;
        result = 31 * result + (int) (maxConsumeLag ^ (maxConsumeLag >>> 32));
        result = 31 * result + (groovyScript != null ? groovyScript.hashCode() : 0);
        result = 31 * result + timeout;
        result = 31 * result + (isPressureTraffic ? 1 : 0);
        result = 31 * result + (orderKey != null ? orderKey.hashCode() : 0);
        result = 31 * result + maxRetry;
        result = 31 * result + (retryIntervals != null ? retryIntervals.hashCode() : 0);
        result = 31 * result + (urls != null ? urls.hashCode() : 0);
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
        result = 31 * result + (appendContext != null ? appendContext.hashCode() : 0);
        result = 31 * result + (transit != null ? transit.hashCode() : 0);
        result = 31 * result + (tokenKey != null ? tokenKey.hashCode() : 0);
        temp = Double.doubleToLongBits(httpMaxTps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + maxPullBatchSize;
        result = 31 * result + (hdfsConfiguration != null ? hdfsConfiguration.hashCode() : 0);
        result = 31 * result + (enableAlarm ? 1 : 0);
        result = 31 * result + (int) (delayTimeThreshold ^ (delayTimeThreshold >>> 32));
        result = 31 * result + (int) (committedLagThreshold ^ (committedLagThreshold >>> 32));
        return result;
    }
}