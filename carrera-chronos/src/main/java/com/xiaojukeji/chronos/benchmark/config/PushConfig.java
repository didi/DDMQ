package com.xiaojukeji.chronos.benchmark.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;

import java.util.List;


public class PushConfig implements ConfigurationValidator {
    private List<String> pproxyAddrs;
    private int proxyTimeoutMs;
    private int clientRetry;
    private int clientTimeoutMs;
    private int poolSize;

    private String topic;
    private int threadNum;
    private int msgSentCountPerThread;
    private int qpsLimit;
    private int allDelayTimeSecond;
    private int perDelayTimeSecond;
    private int baseDelayTimeSecond;
    private int msgBodyLen;
    private int expireSecond;

    private boolean sendLoop;
    private int sleepMsPerMsg;
    private int sleepMsPerLoop;

    private int runTimeMinute;

    public List<String> getPproxyAddrs() {
        return pproxyAddrs;
    }

    public void setPproxyAddrs(List<String> pproxyAddrs) {
        this.pproxyAddrs = pproxyAddrs;
    }

    public int getProxyTimeoutMs() {
        return proxyTimeoutMs;
    }

    public void setProxyTimeoutMs(int proxyTimeoutMs) {
        this.proxyTimeoutMs = proxyTimeoutMs;
    }

    public int getClientRetry() {
        return clientRetry;
    }

    public void setClientRetry(int clientRetry) {
        this.clientRetry = clientRetry;
    }

    public int getClientTimeoutMs() {
        return clientTimeoutMs;
    }

    public void setClientTimeoutMs(int clientTimeoutMs) {
        this.clientTimeoutMs = clientTimeoutMs;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getMsgSentCountPerThread() {
        return msgSentCountPerThread;
    }

    public void setMsgSentCountPerThread(int msgSentCountPerThread) {
        this.msgSentCountPerThread = msgSentCountPerThread;
    }

    public int getQpsLimit() {
        return qpsLimit;
    }

    public void setQpsLimit(int qpsLimit) {
        this.qpsLimit = qpsLimit;
    }

    public int getAllDelayTimeSecond() {
        return allDelayTimeSecond;
    }

    public void setAllDelayTimeSecond(int allDelayTimeSecond) {
        this.allDelayTimeSecond = allDelayTimeSecond;
    }

    public int getPerDelayTimeSecond() {
        return perDelayTimeSecond;
    }

    public void setPerDelayTimeSecond(int perDelayTimeSecond) {
        this.perDelayTimeSecond = perDelayTimeSecond;
    }

    public int getMsgBodyLen() {
        return msgBodyLen;
    }

    public void setMsgBodyLen(int msgBodyLen) {
        this.msgBodyLen = msgBodyLen;
    }

    public int getExpireSecond() {
        return expireSecond;
    }

    public void setExpireSecond(int expireSecond) {
        this.expireSecond = expireSecond;
    }

    public boolean isSendLoop() {
        return sendLoop;
    }

    public void setSendLoop(boolean sendLoop) {
        this.sendLoop = sendLoop;
    }

    public int getSleepMsPerMsg() {
        return sleepMsPerMsg;
    }

    public void setSleepMsPerMsg(int sleepMsPerMsg) {
        this.sleepMsPerMsg = sleepMsPerMsg;
    }

    public int getSleepMsPerLoop() {
        return sleepMsPerLoop;
    }

    public void setSleepMsPerLoop(int sleepMsPerLoop) {
        this.sleepMsPerLoop = sleepMsPerLoop;
    }

    public int getRunTimeMinute() {
        return runTimeMinute;
    }

    public void setRunTimeMinute(int runTimeMinute) {
        this.runTimeMinute = runTimeMinute;
    }

    public int getBaseDelayTimeSecond() {
        return baseDelayTimeSecond;
    }

    public void setBaseDelayTimeSecond(int baseDelayTimeSecond) {
        this.baseDelayTimeSecond = baseDelayTimeSecond;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String toString() {
        return "PushConfig{" +
                "pproxyAddrs=" + pproxyAddrs +
                ", proxyTimeoutMs=" + proxyTimeoutMs +
                ", clientRetry=" + clientRetry +
                ", clientTimeoutMs=" + clientTimeoutMs +
                ", poolSize=" + poolSize +
                ", topic='" + topic + '\'' +
                ", threadNum=" + threadNum +
                ", msgSentCountPerThread=" + msgSentCountPerThread +
                ", qpsLimit=" + qpsLimit +
                ", allDelayTimeSecond=" + allDelayTimeSecond +
                ", perDelayTimeSecond=" + perDelayTimeSecond +
                ", baseDelayTimeSecond=" + baseDelayTimeSecond +
                ", msgBodyLen=" + msgBodyLen +
                ", expireSecond=" + expireSecond +
                ", sendLoop=" + sendLoop +
                ", sleepMsPerMsg=" + sleepMsPerMsg +
                ", sleepMsPerLoop=" + sleepMsPerLoop +
                ", runTimeMinute=" + runTimeMinute +
                '}';
    }
}