package com.xiaojukeji.carrera.pproxy.kafka.network;

import org.apache.kafka.common.requests.AbstractRequest;
import org.apache.kafka.common.requests.RequestAndSize;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.requests.RequestHeader;

import java.nio.ByteBuffer;

public class Request extends BaseRequest {

    private int processor;
    private RequestContext requestContext;
    private RequestHeader header;
    private volatile long requestDequeueTimeNanos = -1L;
    private volatile long apiLocalCompleteTimeNanos = -1L;
    private volatile long responseCompleteTimeNanos = -1L;
    private volatile long responseDequeueTimeNanos = -1L;
    private volatile long apiRemoteCompleteTimeNanos = -1L;
    private volatile long messageConversionsTimeNanos = 0L;
    private volatile long temporaryMemoryBytes = 0L;
    private RequestAndSize bodyAndSize;
    private int sizeOfBodyInBytes;

    public Request(int processor, RequestContext requestContext, long startTimeNanos, ByteBuffer buffer) {
        this.requestContext = requestContext;
        this.processor = processor;
        this.header = requestContext.header;
        this.bodyAndSize = requestContext.parseRequest(buffer);
        this.sizeOfBodyInBytes = bodyAndSize.size;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public AbstractRequest body() {
        return bodyAndSize.request;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public long getRequestDequeueTimeNanos() {
        return requestDequeueTimeNanos;
    }

    public void setRequestDequeueTimeNanos(long requestDequeueTimeNanos) {
        this.requestDequeueTimeNanos = requestDequeueTimeNanos;
    }

    public long getApiLocalCompleteTimeNanos() {
        return apiLocalCompleteTimeNanos;
    }

    public void setApiLocalCompleteTimeNanos(long apiLocalCompleteTimeNanos) {
        this.apiLocalCompleteTimeNanos = apiLocalCompleteTimeNanos;
    }

    public long getResponseCompleteTimeNanos() {
        return responseCompleteTimeNanos;
    }

    public void setResponseCompleteTimeNanos(long responseCompleteTimeNanos) {
        this.responseCompleteTimeNanos = responseCompleteTimeNanos;
    }

    public long getResponseDequeueTimeNanos() {
        return responseDequeueTimeNanos;
    }

    public void setResponseDequeueTimeNanos(long responseDequeueTimeNanos) {
        this.responseDequeueTimeNanos = responseDequeueTimeNanos;
    }

    public long getApiRemoteCompleteTimeNanos() {
        return apiRemoteCompleteTimeNanos;
    }

    public void setApiRemoteCompleteTimeNanos(long apiRemoteCompleteTimeNanos) {
        this.apiRemoteCompleteTimeNanos = apiRemoteCompleteTimeNanos;
    }

    public long getMessageConversionsTimeNanos() {
        return messageConversionsTimeNanos;
    }

    public void setMessageConversionsTimeNanos(long messageConversionsTimeNanos) {
        this.messageConversionsTimeNanos = messageConversionsTimeNanos;
    }

    public long getTemporaryMemoryBytes() {
        return temporaryMemoryBytes;
    }

    public void setTemporaryMemoryBytes(long temporaryMemoryBytes) {
        this.temporaryMemoryBytes = temporaryMemoryBytes;
    }

    public int getProcessor() {
        return processor;
    }

    public RequestHeader getHeader() {
        return header;
    }

    public int getSizeOfBodyInBytes() {
        return sizeOfBodyInBytes;
    }
}
