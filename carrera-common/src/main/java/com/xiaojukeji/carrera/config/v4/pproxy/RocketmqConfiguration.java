package com.xiaojukeji.carrera.config.v4.pproxy;

import java.util.List;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.apache.commons.collections4.CollectionUtils;


public class RocketmqConfiguration implements ConfigurationValidator {
    private List<String> namesrvAddrs;
    private String groupPrefix;
    private int sendMsgTimeout;
    private int compressMsgBodyOverHowmuch;
    private int retryTimesWhenSendFailed;
    private boolean retryAnotherBrokerWhenNotStoreOK;
    private int maxMessageSize;
    private int clientCallbackExecutorThreads;
    private int pollNameServerInterval;
    private int heartbeatBrokerInterval;
    private int persistConsumerOffsetInterval;

    public List<String> getNamesrvAddrs() {
        return namesrvAddrs;
    }

    public void setNamesrvAddrs(List<String> namesrvAddrs) {
        this.namesrvAddrs = namesrvAddrs;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    public int getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(int sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public int getCompressMsgBodyOverHowmuch() {
        return compressMsgBodyOverHowmuch;
    }

    public void setCompressMsgBodyOverHowmuch(int compressMsgBodyOverHowmuch) {
        this.compressMsgBodyOverHowmuch = compressMsgBodyOverHowmuch;
    }

    public int getRetryTimesWhenSendFailed() {
        return retryTimesWhenSendFailed;
    }

    public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
        this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
    }

    public boolean isRetryAnotherBrokerWhenNotStoreOK() {
        return retryAnotherBrokerWhenNotStoreOK;
    }

    public void setRetryAnotherBrokerWhenNotStoreOK(boolean retryAnotherBrokerWhenNotStoreOK) {
        this.retryAnotherBrokerWhenNotStoreOK = retryAnotherBrokerWhenNotStoreOK;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public int getPollNameServerInterval() {
        return pollNameServerInterval;
    }

    public void setPollNameServerInterval(int pollNameServerInterval) {
        this.pollNameServerInterval = pollNameServerInterval;
    }

    public int getHeartbeatBrokerInterval() {
        return heartbeatBrokerInterval;
    }

    public void setHeartbeatBrokerInterval(int heartbeatBrokerInterval) {
        this.heartbeatBrokerInterval = heartbeatBrokerInterval;
    }

    public int getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }

    public void setPersistConsumerOffsetInterval(int persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }

    @Override
    public String toString() {
        return "RocketmqConfiguration{" +
            "namesrvAddrs=" + namesrvAddrs +
            ", groupPrefix='" + groupPrefix + '\'' +
            ", sendMsgTimeout=" + sendMsgTimeout +
            ", compressMsgBodyOverHowmuch=" + compressMsgBodyOverHowmuch +
            ", retryTimesWhenSendFailed=" + retryTimesWhenSendFailed +
            ", retryAnotherBrokerWhenNotStoreOK=" + retryAnotherBrokerWhenNotStoreOK +
            ", maxMessageSize=" + maxMessageSize +
            ", clientCallbackExecutorThreads=" + clientCallbackExecutorThreads +
            ", pollNameServerInterval=" + pollNameServerInterval +
            ", heartbeatBrokerInterval=" + heartbeatBrokerInterval +
            ", persistConsumerOffsetInterval=" + persistConsumerOffsetInterval +
            '}';
    }

    @Override
    public boolean validate() {
        return CollectionUtils.isNotEmpty(namesrvAddrs)
            && groupPrefix != null
            && sendMsgTimeout >= 0
            && compressMsgBodyOverHowmuch > 0
            && retryTimesWhenSendFailed >= 0
            && maxMessageSize > 0
            && clientCallbackExecutorThreads > 0
            && pollNameServerInterval > 0
            && heartbeatBrokerInterval > 0
            && persistConsumerOffsetInterval > 0
            ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RocketmqConfiguration that = (RocketmqConfiguration) o;

        if (sendMsgTimeout != that.sendMsgTimeout)
            return false;
        if (compressMsgBodyOverHowmuch != that.compressMsgBodyOverHowmuch)
            return false;
        if (retryTimesWhenSendFailed != that.retryTimesWhenSendFailed)
            return false;
        if (retryAnotherBrokerWhenNotStoreOK != that.retryAnotherBrokerWhenNotStoreOK)
            return false;
        if (maxMessageSize != that.maxMessageSize)
            return false;
        if (clientCallbackExecutorThreads != that.clientCallbackExecutorThreads)
            return false;
        if (pollNameServerInterval != that.pollNameServerInterval)
            return false;
        if (heartbeatBrokerInterval != that.heartbeatBrokerInterval)
            return false;
        if (persistConsumerOffsetInterval != that.persistConsumerOffsetInterval)
            return false;
        if (namesrvAddrs != null ? !namesrvAddrs.equals(that.namesrvAddrs) : that.namesrvAddrs != null)
            return false;
        return groupPrefix != null ? groupPrefix.equals(that.groupPrefix) : that.groupPrefix == null;
    }

    @Override
    public int hashCode() {
        int result = namesrvAddrs != null ? namesrvAddrs.hashCode() : 0;
        result = 31 * result + (groupPrefix != null ? groupPrefix.hashCode() : 0);
        result = 31 * result + sendMsgTimeout;
        result = 31 * result + compressMsgBodyOverHowmuch;
        result = 31 * result + retryTimesWhenSendFailed;
        result = 31 * result + (retryAnotherBrokerWhenNotStoreOK ? 1 : 0);
        result = 31 * result + maxMessageSize;
        result = 31 * result + clientCallbackExecutorThreads;
        result = 31 * result + pollNameServerInterval;
        result = 31 * result + heartbeatBrokerInterval;
        result = 31 * result + persistConsumerOffsetInterval;
        return result;
    }
}