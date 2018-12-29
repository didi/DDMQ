package com.xiaojukeji.carrera.pproxy.producer;


import com.xiaojukeji.carrera.thrift.Result;


public enum ProxySendResult {
    ASYNC(-1, "ASYNC"),
    OK(0, "OK"),

    FAIL_UNKNOWN(10, "FAILED: unknown reason."),
    FAIL_ILLEGAL_MSG(11, "FAILED: Message is too large or illegal or the length is 0"),
    FAIL_TIMEOUT(12, "FAILED: Send timeout"),
    FAIL_TOPIC_NOT_ALLOWED(13, "FAILED: Topic is not in the proxy white list"),
    FAIL_TOPIC_NOT_EXIST(14, "FAILED: Topic do not exist!"),
    FAIL_REFUSED_BY_RATE_LIMITER(15, "FAILED: Refused by the rate limiter."),
    FAIL_TOPIC_IS_NOT_DELAY(18, "FAILED: Topic is not delay"),
    FAIL_TOPIC_IS_DELAY(19, "FAILED: Topic is delay"),

    FAIL_SERVER_SHUTDOWN(20, "FAILED: Server is shutting down."),
    FAIL_NO_PRODUCER_FOR_CLUSTER(21, "FAILED: no producer for cluster."),

    FAIL_ILLEGAL_UNIQ_DELAY_MSG_ID(30, "FAILED: Illegal uniqDelayMsgId."),
    FAIL_ILLEGAL_DELAY_MSG_TYPE(31, "FAILED: Illegal dMsgType."),
    FAIL_ILLEGAL_DELAY_ACTION(32, "FAILED: Illegal delay action."),
    FAIL_ILLEGAL_DELAY_TIMESTAMP(33, "FAILED: Illegal delay timestamp."),
    FAIL_ILLEGAL_DELAY_INNER_TOPIC_SEQ(34, "FAILED: Illegal delay innerTopicSeq."),
    FAIL_ILLEGAL_DELAY_LOOP_TIMES(35, "FAILED: Illegal delay loopTimes."),
    FAIL_ILLEGAL_DELAY_LOOP_EXPIRE(36, "FAILED: Illegal delay expire, expire must be greater than timestamp."),
    FAIL_ILLEGAL_DELAY_LOOP_INTERVAL(37, "FAILED: Illegal delay interval, interval can not be less than 3."),
    FAIL_ILLEGAL_DELAY_LOOP_EXPONENT_BASE(38, "FAILED: Illegal delay exponent base, base can not be less than 2."),
    ;
    private Result result;

    ProxySendResult(int code, String message) {
        result = new Result(code, message);
    }

    public Result getResult() {
        return result;
    }
}