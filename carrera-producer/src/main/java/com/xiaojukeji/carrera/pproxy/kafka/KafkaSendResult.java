package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.pproxy.producer.ProxySendResult;
import org.apache.rocketmq.client.producer.SendResult;

public class KafkaSendResult {
    ProxySendResult proxySendResult;
    SendResult sendResult;

    public KafkaSendResult(ProxySendResult proxySendResult, SendResult sendResult) {
        this.proxySendResult = proxySendResult;
        this.sendResult = sendResult;
    }

    public ProxySendResult getProxySendResult() {
        return proxySendResult;
    }

    public void setProxySendResult(ProxySendResult proxySendResult) {
        this.proxySendResult = proxySendResult;
    }

    public SendResult getSendResult() {
        return sendResult;
    }

    public void setSendResult(SendResult sendResult) {
        this.sendResult = sendResult;
    }
}
