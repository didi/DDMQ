package com.didi.carrera.console.dao.model.custom;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.AppendContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ConsumeSubscriptionConfig implements Serializable {

    public static final String  key_fetchThreads = "fetchThreads";
    public static final String  key_concurrency = "concurrency";
    public static final String  key_maxPullBatchSize = "maxPullBatchSize";
    public static final String  key_httpMaxTps = "httpMaxTps";
    public static final String  key_maxConsumeLag = "maxConsumeLag";


    private Integer fetchThreads = 1;

    private Integer concurrency = 1024;

    private Integer maxPullBatchSize = 8;

    private Double httpMaxTps = -1D;

    private List<AppendContext> appendContext = Lists.newArrayList(AppendContext.values());

    private boolean needResetOffset = false;

    private Integer maxConsumeLag = -1;

    private boolean isOldDbData = false;

    private boolean binlog = false;

    private Map<String/*proxyCluster*/, Set<String>> proxies;

    public Integer getFetchThreads() {
        return fetchThreads;
    }

    public void setFetchThreads(Integer fetchThreads) {
        this.fetchThreads = fetchThreads;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public Integer getMaxPullBatchSize() {
        return maxPullBatchSize;
    }

    public void setMaxPullBatchSize(Integer maxPullBatchSize) {
        this.maxPullBatchSize = maxPullBatchSize;
    }

    public List<AppendContext> getAppendContext() {
        return appendContext;
    }

    public void setAppendContext(List<AppendContext> appendContext) {
        this.appendContext = appendContext;
    }

    public boolean isNeedResetOffset() {
        return needResetOffset;
    }

    public void setNeedResetOffset(boolean needResetOffset) {
        this.needResetOffset = needResetOffset;
    }

    public Integer getMaxConsumeLag() {
        return maxConsumeLag;
    }

    public void setMaxConsumeLag(Integer maxConsumeLag) {
        this.maxConsumeLag = maxConsumeLag;
    }

    public boolean isOldDbData() {
        return isOldDbData;
    }

    public void setOldDbData(boolean oldDbData) {
        isOldDbData = oldDbData;
    }

    public Double getHttpMaxTps() {
        return httpMaxTps;
    }

    public void setHttpMaxTps(Double httpMaxTps) {
        this.httpMaxTps = httpMaxTps;
    }

    public boolean isBinlog() {
        return binlog;
    }

    public void setBinlog(boolean binlog) {
        this.binlog = binlog;
    }

    public Map<String, Set<String>> getProxies() {
        return proxies;
    }

    public void setProxies(Map<String, Set<String>> proxies) {
        this.proxies = proxies;
    }

    @Override
    public String toString() {
        return "ConsumeSubscriptionConfig{" +
                "fetchThreads=" + fetchThreads +
                ", concurrency=" + concurrency +
                ", maxPullBatchSize=" + maxPullBatchSize +
                ", httpMaxTps=" + httpMaxTps +
                ", appendContext=" + appendContext +
                ", needResetOffset=" + needResetOffset +
                ", maxConsumeLag=" + maxConsumeLag +
                ", isOldDbData=" + isOldDbData +
                ", binlog=" + binlog +
                ", proxies=" + proxies +
                '}';
    }
}