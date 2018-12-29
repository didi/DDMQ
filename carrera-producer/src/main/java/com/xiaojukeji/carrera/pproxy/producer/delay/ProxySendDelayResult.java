package com.xiaojukeji.carrera.pproxy.producer.delay;

import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.pproxy.producer.ProxySendResult;


public class ProxySendDelayResult {

    private DelayResult result;

    private ProxySendResult proxySendResult;

    public ProxySendDelayResult(ProxySendResult proxySendResult) {
        this(proxySendResult, "");
    }

    public ProxySendDelayResult(ProxySendResult proxySendResult, String uniqDelayMsgId) {
        this.result = new DelayResult(
                proxySendResult.getResult().getCode(),
                proxySendResult.getResult().getMsg(),
                uniqDelayMsgId);
        this.proxySendResult = proxySendResult;
    }

    public DelayResult getResult() {
        return result;
    }

    public ProxySendResult getProxySendResult() {
        return proxySendResult;
    }

    @Override
    public String toString() {
        return "ProxySendDelayResult{" +
                "result=" + result +
                ", proxySendResult=" + proxySendResult +
                '}';
    }
}