package com.xiaojukeji.carrera.config.v4.pproxy;

import java.util.List;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.apache.commons.collections4.CollectionUtils;


public class KafkaConfiguration implements ConfigurationValidator {
    private List<String> bootstrapServers;
    private String acks;
    private int retries;
    private int batchSize;
    private String clientId;
    private long retryBackoff;
    private long metadataMaxAge;
    private long metadataFetchTimeout;
    private int ackTimeout;
    private long bufferMemory;

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public List<String> getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(List<String> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getRetryBackoff() {
        return retryBackoff;
    }

    public void setRetryBackoff(long retryBackoff) {
        this.retryBackoff = retryBackoff;
    }

    public long getMetadataMaxAge() {
        return metadataMaxAge;
    }

    public void setMetadataMaxAge(long metadataMaxAge) {
        this.metadataMaxAge = metadataMaxAge;
    }

    @Override
    public String toString() {
        return "KafkaConfiguration{" +
                "bootstrapServers=" + bootstrapServers +
                ", acks='" + acks + '\'' +
                ", retries=" + retries +
                ", batchSize=" + batchSize +
                ", clientId='" + clientId + '\'' +
                ", retryBackoff=" + retryBackoff +
                ", metadataMaxAge=" + metadataMaxAge +
                ", metadataFetchTimeout=" + metadataFetchTimeout +
                ", ackTimeout=" + ackTimeout +
                ", bufferMemory=" + bufferMemory +
                '}';
    }

    @Override
    public boolean validate() {
        return CollectionUtils.isNotEmpty(bootstrapServers)
                && acks != null
                && retries >= 0
                && batchSize > 0
                && clientId != null
                && retryBackoff > 0
                && metadataMaxAge > 0
                && metadataFetchTimeout > 0
                && ackTimeout > 0
                && bufferMemory > 0
                ;
    }

    public long getMetadataFetchTimeout() {
        return metadataFetchTimeout;
    }

    public void setMetadataFetchTimeout(long metadataFetchTimeout) {
        this.metadataFetchTimeout = metadataFetchTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public long getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(long bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        KafkaConfiguration that = (KafkaConfiguration) o;

        if (retries != that.retries)
            return false;
        if (batchSize != that.batchSize)
            return false;
        if (retryBackoff != that.retryBackoff)
            return false;
        if (metadataMaxAge != that.metadataMaxAge)
            return false;
        if (metadataFetchTimeout != that.metadataFetchTimeout)
            return false;
        if (ackTimeout != that.ackTimeout)
            return false;
        if (bufferMemory != that.bufferMemory)
            return false;
        if (bootstrapServers != null ? !bootstrapServers.equals(that.bootstrapServers) : that.bootstrapServers != null)
            return false;
        if (acks != null ? !acks.equals(that.acks) : that.acks != null)
            return false;
        return clientId != null ? clientId.equals(that.clientId) : that.clientId == null;
    }

    @Override public int hashCode() {
        int result = bootstrapServers != null ? bootstrapServers.hashCode() : 0;
        result = 31 * result + (acks != null ? acks.hashCode() : 0);
        result = 31 * result + retries;
        result = 31 * result + batchSize;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (int) (retryBackoff ^ (retryBackoff >>> 32));
        result = 31 * result + (int) (metadataMaxAge ^ (metadataMaxAge >>> 32));
        result = 31 * result + (int) (metadataFetchTimeout ^ (metadataFetchTimeout >>> 32));
        result = 31 * result + ackTimeout;
        result = 31 * result + (int) (bufferMemory ^ (bufferMemory >>> 32));
        return result;
    }
}