package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.thrift.producerProxyConstants;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.utils.FastJsonUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.xiaojukeji.carrera.thrift.producerProxyConstants.CARRERA_HEADERS;
import static com.xiaojukeji.carrera.thrift.producerProxyConstants.DIDI_HEADER_RID;
import static com.xiaojukeji.carrera.thrift.producerProxyConstants.DIDI_HEADER_SPANID;


public class AddDelayMessageBuilder {
    private CarreraProducer producer = null;
    private DelayMeta delayMeta = null;

    private String topic = null;
    private byte[] body = null;
    private String tags = null;
    private Map<String, String> properties = null;
    private Map<String, String> headers = null;

    public AddDelayMessageBuilder(CarreraProducer producer) {
        this.producer = producer;
        this.properties = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public AddDelayMessageBuilder setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public AddDelayMessageBuilder setBody(byte[] body) {
        if (body != null)
            this.body = body;
        return this;
    }

    public AddDelayMessageBuilder setBody(String body) {
        if (body != null)
            this.body = body.getBytes();
        return this;
    }

    public AddDelayMessageBuilder setBody(String body, String charsetName) throws UnsupportedEncodingException {
        if (body != null && charsetName != null)
            this.body = body.getBytes(charsetName);
        return this;
    }

    public AddDelayMessageBuilder setDelayMeta(DelayMeta delayMeta) {
        this.delayMeta = delayMeta;
        return this;
    }

    public AddDelayMessageBuilder setTags(String tag) {
        if (tag != null)
            this.tags = tag;
        return this;
    }

    public AddDelayMessageBuilder addProperty(String key, String value) {
        if (key != null && !key.equals(producerProxyConstants.PRESSURE_TRAFFIC_KEY)) {
            this.properties.put(key, value);
        }
        return this;
    }

    public AddDelayMessageBuilder addHeader(String key, String value) {
        if (key != null) {
            if (key.equalsIgnoreCase(DIDI_HEADER_RID)) {
                this.setTraceId(value);
            } else if (key.equalsIgnoreCase(DIDI_HEADER_SPANID)) {
                this.setSpanId(value);
            } else {
                if (!key.equalsIgnoreCase(CARRERA_HEADERS)) {
                    this.headers.put(key, value);
                }
            }
        }
        return this;
    }

    public AddDelayMessageBuilder setTraceId(String traceId) {
        this.properties.put(producerProxyConstants.TRACE_ID, traceId);
        return this;
    }

    public AddDelayMessageBuilder setSpanId(String spanId) {
        this.properties.put(producerProxyConstants.SPAN_ID, spanId);
        return this;
    }

    public AddDelayMessageBuilder setPressureTraffic(boolean isOpen) {
        if (isOpen) {
            this.properties.put(producerProxyConstants.PRESSURE_TRAFFIC_KEY, producerProxyConstants.PRESSURE_TRAFFIC_ENABLE);
        } else {
            this.properties.put(producerProxyConstants.PRESSURE_TRAFFIC_KEY, producerProxyConstants.PRESSURE_TRAFFIC_DISABLE);
        }
        return this;
    }


    public DelayResult send() {
        if (this.delayMeta != null) {
            if (this.headers != null && this.headers.size() > 0) {
                this.properties.put(CARRERA_HEADERS, FastJsonUtils.toJsonString(this.headers));
            }
            if (this.delayMeta.properties == null || this.delayMeta.properties.isEmpty()) {
                this.delayMeta.properties = this.properties;
            } else {
                this.delayMeta.properties.putAll(this.properties);
            }
        }
        if (this.tags == null)
            return this.producer.sendDelay(this.topic, this.body, this.delayMeta);

        return this.producer.sendDelay(this.topic, this.body, this.delayMeta, this.tags);
    }
}