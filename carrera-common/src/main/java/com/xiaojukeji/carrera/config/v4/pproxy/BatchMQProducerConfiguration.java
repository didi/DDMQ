package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;


public class BatchMQProducerConfiguration implements ConfigurationValidator {

    private int maxBathBytes = 4096;
    private int doBatchThresholdBytes = 1024;
    private int maxBatchMessagesNumber = 32;

    private int encodeWorkerThreads = 4;
    private int maxEncodeWorkerForEachBroker = 1;

    private int batchWaitMills = 5;
    private int maxContinuouslyRunningMills = 50;

    public int getBatchWaitMills() {
        return batchWaitMills;
    }

    public void setBatchWaitMills(int batchWaitMills) {
        this.batchWaitMills = batchWaitMills;
    }

    public int getMaxBathBytes() {
        return maxBathBytes;
    }

    public void setMaxBathBytes(int maxBathBytes) {
        this.maxBathBytes = maxBathBytes;
    }

    public int getDoBatchThresholdBytes() {
        return doBatchThresholdBytes;
    }

    public void setDoBatchThresholdBytes(int doBatchThresholdBytes) {
        this.doBatchThresholdBytes = doBatchThresholdBytes;
    }

    public int getMaxBatchMessagesNumber() {
        return maxBatchMessagesNumber;
    }

    public void setMaxBatchMessagesNumber(int maxBatchMessagesNumber) {
        this.maxBatchMessagesNumber = maxBatchMessagesNumber;
    }

    public int getEncodeWorkerThreads() {
        return encodeWorkerThreads;
    }

    public void setEncodeWorkerThreads(int encodeWorkerThreads) {
        this.encodeWorkerThreads = encodeWorkerThreads;
    }

    public int getMaxEncodeWorkerForEachBroker() {
        return maxEncodeWorkerForEachBroker;
    }

    public void setMaxEncodeWorkerForEachBroker(int maxEncodeWorkerForEachBroker) {
        this.maxEncodeWorkerForEachBroker = maxEncodeWorkerForEachBroker;
    }

    @Override
    public String toString() {
        return "BatchMQProducerConfiguration{" +
                "maxBathBytes=" + maxBathBytes +
                ", doBatchThresholdBytes=" + doBatchThresholdBytes +
                ", maxBatchMessagesNumber=" + maxBatchMessagesNumber +
                ", encodeWorkerThreads=" + encodeWorkerThreads +
                ", maxEncodeWorkerForEachBroker=" + maxEncodeWorkerForEachBroker +
                ", batchWaitMills=" + batchWaitMills +
                ", maxContinuouslyRunningMills=" + maxContinuouslyRunningMills +
                '}';
    }

    public int getMaxContinuouslyRunningMills() {
        return maxContinuouslyRunningMills;
    }

    public void setMaxContinuouslyRunningMills(int maxContinuouslyRunningMills) {
        this.maxContinuouslyRunningMills = maxContinuouslyRunningMills;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (doBatchThresholdBytes < 0) {
            throw new ConfigException("[BatchMQProducerConfig] doBatchThresholdBytes <= 0");
        } else if (doBatchThresholdBytes >= maxBathBytes) {
            throw new ConfigException("[BatchMQProducerConfig] doBatchThresholdBytes >= maxBathBytes");
        } else if (maxBatchMessagesNumber <= 0) {
            throw new ConfigException("[BatchMQProducerConfig] maxBatchMessagesNumber <= 0");
        } else if (encodeWorkerThreads <= 0) {
            throw new ConfigException("[BatchMQProducerConfig] encodeWorkerThreads <= 0");
        } else if (maxEncodeWorkerForEachBroker <= 0) {
            throw new ConfigException("[BatchMQProducerConfig] maxEncodeWorkerForEachBroker <= 0");
        } else if (batchWaitMills < 0) {
            throw new ConfigException("[BatchMQProducerConfig] batchWaitMills < 0");
        } else if (maxContinuouslyRunningMills <= 0) {
            throw new ConfigException("[BatchMQProducerConfig] maxContinuouslyRunningMills <= 0");
        }
        return true;
    }
}