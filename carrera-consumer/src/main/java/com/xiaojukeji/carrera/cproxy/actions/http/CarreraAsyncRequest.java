package com.xiaojukeji.carrera.cproxy.actions.http;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.actions.FormParamsExtractAction;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.utils.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpStatus;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xiaojukeji.carrera.cproxy.actions.FormParamsExtractAction.CARRERA_PROPERTIES;
import static com.xiaojukeji.carrera.thrift.consumer.consumerProxyConstants.CARRERA_HEADERS;
import static org.slf4j.LoggerFactory.getLogger;


public class CarreraAsyncRequest extends AsyncCompletionHandler<Response> {
    enum HttpErrNo {
        OK(0),
        SLOW(1000000),
        IN_PROCESSABLE(1000001),
        RETRY(1000002);
        private int code;

        HttpErrNo(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public static final Logger LOGGER = getLogger(CarreraAsyncRequest.class);
    private static final Logger DROP_LOGGER = LogUtils.DROP_LOGGER;
    private static final int SLOW_DOWN_PERMITS = 10; //处于slowDown状态的topic, 每次submit需要的令牌数, 也就是TPS放慢的倍数.

    private enum ProcessResult {OK, FAIL, DELAY_RETRY}

    private static final int MIN_TIMEOUT_TIME = 10000;
    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final int MAX_RETRY_DELAY_FACTOR = 7; //max delay is about 2.1 minutes

    private final UpstreamJob job;
    private volatile int startIdx;
    private volatile long startTime;
    private volatile int requestCnt = 0;
    private final BlockingQueue<CarreraAsyncRequest> retryRequestQueue;
    private final AtomicInteger permits;
    private final ScheduledExecutorService scheduler;
    private volatile String lastRequestErrno = null;
    private final Set<CarreraAsyncRequest> inflightRequests;
    private final String groupCluster;

    public CarreraAsyncRequest(UpstreamJob job, AtomicInteger permits, String groupCluster, BlockingQueue<CarreraAsyncRequest> retryQueue, Set<CarreraAsyncRequest> inflightRequests, ScheduledExecutorService scheduler) {
        this.job = job;
        this.permits = permits;
        this.groupCluster = groupCluster;
        this.retryRequestQueue = retryQueue;
        this.scheduler = scheduler;
        this.inflightRequests = inflightRequests;
        startIdx = RandomUtils.nextInt(0, job.getUrls().size());
    }

    /**
     * 标记某个topic为slowDown状态.
     */
    private void slowDown() {
        if (permits.get() <= 0) {
            permits.set((int) LimiterMgr.getInstance().getHttpRate(groupCluster, job.getTopic()));
            LOGGER.info("permits of GroupTopic {}-{} is set to {}", job.getGroupId(), job.getTopic(), permits.get());
        } else {
            permits.incrementAndGet();
        }
    }

    /**
     * 在slowDown状态, 每次从RateLimiter获取的令牌数为SLOW_DOWN_PERMITS个. 其他情况下为1.
     *
     * @return
     */
    private int getRequiredPermits() {
        int oldPermits;
        do {
            oldPermits = permits.get();
            if (oldPermits <= 0) {
                return 1;
            }
        } while (!permits.compareAndSet(oldPermits, oldPermits - 1));
        return SLOW_DOWN_PERMITS;
    }

    @Override
    public Response onCompleted(Response response) {
        inflightRequests.remove(this);
        job.setState("HTTP.onComplete");
        MetricUtils.httpRequestLatencyMetric(job, TimeUtils.getElapseTime(startTime));
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            MetricUtils.httpRequestFailureMetric(job, Integer.toString(response.getStatusCode()));
            LOGGER.info("Action Result: HttpAccess[result:exception,code:{},url:{},request:{},used:{}ms]",
                    response.getStatusCode(), getUrl(), this, TimeUtils.getElapseTime(startTime));
            delayRetryRequest(DEFAULT_RETRY_DELAY << Math.min(job.getErrorRetryCnt(), MAX_RETRY_DELAY_FACTOR));
            job.incErrorRetryCnt();
        } else {
            ProcessResult result = processResponseContent(response.getResponseBody(), job);
            long elapse = TimeUtils.getElapseTime(startTime);
            MetricUtils.httpRequestSuccessMetric(job, result == ProcessResult.OK, lastRequestErrno);
            if (result == ProcessResult.OK) {
                LOGGER.info("Action Result: HttpAccess[result:success,request:{},used:{}ms]", this, elapse);
                job.onFinished(true);
            } else if (result == ProcessResult.FAIL) {
                LOGGER.info("Action Result: HttpAccess[result:failure,request:{},used:{}ms,response:{}]",
                        this, elapse, response.getResponseBody());
                DROP_LOGGER.info("[request:{},url:{},response:{},used:{}ms,httpParams:{},msg:{}]",
                        this, getUrl(), response.getResponseBody(), elapse, job.getHttpParams(),
                        StringUtils.newString(job.getCommonMessage().getValue()));
                job.onFinished(true);
            } else { //DELAY_RETRY
                LOGGER.info("Action Result: HttpAccess[result:retry,url:{},request:{},response:{},used:{}ms]",
                        getUrl(), this, response.getResponseBody(), elapse);
            }
        }
        return response;
    }

    ProcessResult processResponseContent(String content, UpstreamJob job) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("HttpAccess; job={},url:{},content:{}", job.info(), getUrl(), content);
        }
        if (StringUtils.isBlank(content)) {
            lastRequestErrno = null;
            return ProcessResult.OK;
        }
        try {
            JSONObject ret = JSONObject.parseObject(content);
            Integer error = ret.getInteger("errno");
            if (error != null) {
                lastRequestErrno = Integer.toString(error);
                return proceedErrCode(error, job);
            } else {
                lastRequestErrno = null;
                return ProcessResult.OK;
            }
        } catch (NumberFormatException e) { // errno exists but not int.
            LogUtils.logErrorInfo("CarreraAsyncRequest_error", "errno format exception. job=" + job.info(), e);
            lastRequestErrno = "NumberFormatException";
            return ProcessResult.FAIL;
        } catch (JSONException e) {
            lastRequestErrno = "JSONException";
            return ProcessResult.OK;
        } catch (Throwable t) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("exception when handle result." + job.info(), t);
            }
            lastRequestErrno = null;
            return ProcessResult.OK;
        }
    }

    ProcessResult proceedErrCode(int code, UpstreamJob job) {

        if (code == HttpErrNo.OK.code()) {
            return ProcessResult.OK;
        } else if (code == HttpErrNo.SLOW.code()) {
            slowDown();
            return ProcessResult.OK;
        } else if (code == HttpErrNo.IN_PROCESSABLE.code()) {
            return ProcessResult.OK;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("proceedErrCode failed, errno={}, request:{}", code, this);
        }

        if (code == HttpErrNo.RETRY.code()) {
            delayRetryRequest(DEFAULT_RETRY_DELAY);
            return ProcessResult.DELAY_RETRY;
        }

        int interval = job.nextRetryDelay();
        if (interval < 0) {
            LOGGER.info("request failed. beyond retry. request", this);
            return ProcessResult.FAIL;
        }
        delayRetryRequest(interval);
        return ProcessResult.DELAY_RETRY;
    }

    private void delayRetryRequest(int delay) {
        if (job.canDoErrorRetry()) {
            LOGGER.info("retry in {}ms, request={}", delay, this);
            try {
                job.setState("HTTP.DelayRetryRequest#" + requestCnt);
                scheduler.schedule(this::retryRequest, delay, TimeUnit.MILLISECONDS);
                return;
            } catch (RejectedExecutionException e) {
                LOGGER.info("delayRetryRequest failed.job=" + job, e);
            }
        }
        LOGGER.info("request is failed! beyond retry! job:{}", job);
        LOGGER.info("Action Result: HttpAccess[result:failure,url:{},request:{},used:{}ms]",
                getUrl(), this, TimeUtils.getElapseTime(startTime));
        DROP_LOGGER.info("[request:{},url:{},used:{}ms,httpParams:{},msg:{}]",
                this, getUrl(), TimeUtils.getElapseTime(startTime), job.getHttpParams(),
                StringUtils.newString(job.getCommonMessage().getValue()));
        job.onFinished(true);
    }

    public void tryRequest() {
        job.setState("HTTP.RateLimit");
        if (LimiterMgr.getInstance().httpAcquire(groupCluster, job.getTopic(), getRequiredPermits()) > 0.0) {
            MetricUtils.incRateLimiterCount(job.getGroupId(), job.getTopic());
        }
        doRequest();

        while (true) {
            CarreraAsyncRequest request = retryRequestQueue.poll();
            if (request == null) break;
            if (LimiterMgr.getInstance().httpAcquire(groupCluster, job.getTopic(), getRequiredPermits()) > 0.0) {
                MetricUtils.incRateLimiterCount(request.job.getGroupId(), request.job.getTopic());
            }
            request.doRequest();
        }
    }

    void retryRequest() {
        if (job.isTerminated()) {
            job.terminate();
            return;
        }
        if (LimiterMgr.getInstance().httpTryAcquire(groupCluster, job.getTopic(), getRequiredPermits())) {
            doRequest();
        } else {
            MetricUtils.incRateLimiterCount(job.getGroupId(), job.getTopic());
            LOGGER.info("job is limited on retry, send in retryRequestQueue, job={}", job.info());
            job.setState("HTTP.InRetryRequestQueue#" + requestCnt);
            retryRequestQueue.offer(this);
        }
    }

    private void doRequest() {
        if (job.isTerminated()) {
            LOGGER.info("job is terminated! request:{}", this);
            job.terminate();
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("doRequest...request:{},httpForm={}", this, job.getHttpParams());
        }
        MetricUtils.maxRetryCount(job.getGroupId(), job.getTopic(), job.getQid(), requestCnt);

        BoundRequestBuilder builder;
        if (StringUtils.equalsIgnoreCase(job.getUpstreamTopic().getHttpMethod(), UpstreamTopic.HTTP_POST)) {
            builder = PushService.getInstance().getClient()
                    .preparePost(getUrl())
                    .setRequestTimeout(getTimeout());
        } else { //GET
            builder = PushService.getInstance().getClient()
                    .prepareGet(getUrl())
                    .setRequestTimeout(getTimeout());
        }

        if (job.getHttpParams() != null) {
            for (HttpParam httpParam : job.getHttpParams()) {
                addPropertiesToHeader(builder, httpParam);

                switch (httpParam.type) {
                    case FORM:
                        builder.addFormParam(httpParam.key, httpParam.value);
                        break;
                    case QUERY:
                        builder.addQueryParam(httpParam.key, httpParam.value);
                        break;
                    case HEADER:
                        builder.addHeader(httpParam.key, httpParam.value);
                        break;
                    default:
                        LogUtils.logErrorInfo("CarreraAsyncRequest_error", "unknown httpType, httpParam:{},job:{}", httpParam, job);
                }
            }
        }

        // http请求次数
        String carreraReqCnt = String.valueOf(requestCnt);
        if (job.isFromChronos()) {
            InternalKey internalKey = new InternalKey(job.getCommonMessage().getKey());
            if (internalKey != null) {
                if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()
                        || internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                    carreraReqCnt = String.valueOf(internalKey.getTimed());
                }
            }
        }
        builder.addFormParam(FormParamsExtractAction.CARRERA_REQ_CNT, carreraReqCnt);

        startTime = TimeUtils.getCurTime();
        job.setState(requestCnt == 0 ? "HTTP.RequestOnFly" : "HTTP.RetryOnFly#" + requestCnt);
        inflightRequests.add(this);
        builder.execute(this);
        requestCnt++;
    }

    @Override
    public void onThrowable(Throwable t) {
        inflightRequests.remove(this);
        job.setState("HTTP.onThrowable");
        MetricUtils.httpRequestLatencyMetric(job, TimeUtils.getElapseTime(startTime));
        MetricUtils.httpRequestFailureMetric(job, null);
        String errorLog = String.format("Action Result: HttpAccess[result:exception,url:%s,request:%s,used:%d],Exception:%s|%s",
                getUrl(), this, TimeUtils.getElapseTime(startTime), t.getClass().getSimpleName(), t.getMessage());
        if (t instanceof ConnectException || t instanceof TimeoutException) {
            LOGGER.info(errorLog);
        } else {
            LogUtils.logErrorInfo("CarreraAsyncRequest_error", errorLog, t);
        }
        delayRetryRequest(DEFAULT_RETRY_DELAY << Math.min(job.getErrorRetryCnt(), MAX_RETRY_DELAY_FACTOR));
        job.incErrorRetryCnt();
    }

    public String getUrl() {
        List<String> urls = job.getUrls();
        return urls.get((startIdx + job.getErrorRetryCnt()) % urls.size());
    }

    public int getTimeout() {
        return job.getUpstreamTopic().getTimeout();
    }

    @Override
    public String toString() {
        return String.format("[job=%s,errRty=%d,rIdx=%d,req=%d]", job, job.getErrorRetryCnt(), job.getRetryIdx(), requestCnt);
    }

    /**
     * 兜底的超时检查！
     */
    public void checkTimeout() {
        if (TimeUtils.getElapseTime(startTime) > Math.max(getTimeout() * 2, MIN_TIMEOUT_TIME)) {
            LOGGER.warn("ASYNC_HTTP_CLIENT_BUG, request callback missing! request={}", this);
            onThrowable(new TimeoutException());
        }
    }

    private void addPropertiesToHeader(final BoundRequestBuilder builder, final HttpParam httpParam) {
        if (builder == null || httpParam == null || httpParam.key == null) {
            return;
        }

        if (httpParam.key.equals(CARRERA_PROPERTIES)) {
            Map<String, String> properties = JsonUtils.fromJsonString(httpParam.value, Map.class);
            if (properties != null && properties.size() > 0) {
                if (properties.containsKey(CARRERA_HEADERS)) {
                    Map<String, String> headers = JsonUtils.fromJsonString(properties.get(CARRERA_HEADERS), Map.class);
                    if (headers != null && headers.size() > 0) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            builder.addHeader(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }
    }
}