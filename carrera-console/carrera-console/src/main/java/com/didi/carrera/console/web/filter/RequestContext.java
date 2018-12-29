package com.didi.carrera.console.web.filter;

import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.common.util.LogUtils;
import com.didi.carrera.console.web.controller.bo.BaseBo;
import com.didi.carrera.console.web.util.RemoteSource;
import com.didi.carrera.console.web.util.RequestUtil;
import com.sun.tools.internal.ws.processor.model.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RequestContext {
    private static final String BODY_KEY = "body";
    public static final String LOGID_KEY = "logId";

    /**
     * 客户端请求的信息
     */
    private static final InheritableThreadLocal<Context> context = new InheritableThreadLocal<>();

    /**
     * 服务器处理后的返回信息
     */
    private static final InheritableThreadLocal<Response> response = new InheritableThreadLocal<>();
    /**
     * 用户请求信息
     */
    private static final InheritableThreadLocal<Request> request = new InheritableThreadLocal<>();

    public static HttpServletRequest getRequest() {
        return context.get() == null ? null : context.get().request;
    }

    public static HttpServletResponse getResponse() {
        return context.get() == null ? null : context.get().response;
    }

    public static String getUri() {
        return context.get() == null ? null : context.get().uri;
    }

    public static String getFullUri() {
        return context.get() == null ? null : context.get().fullUri;
    }

    public static String getLogId() {
        return context.get() == null ? null : context.get().logId;
    }

    public static String getRequestedWith() {
        return context.get() == null ? null : context.get().requestedWith;
    }

    public static RemoteSource getRemoteSource() {
        return context.get() == null ? null : context.get().remoteSource;
    }

    public static Map<String, Object> getDataMap() {
        return context.get() == null ? null : context.get().dataMap;
    }

    public static Object getValue(String key) {
        return context.get() == null ? null : context.get().dataMap.get(key);
    }

    public static String getString(String key) {
        Object value = getValue(key);
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static String getBody() {
        return getString(BODY_KEY);
    }

    protected static void setBody(String body) {
        BaseBo bo = FastJsonUtils.toObject(body, BaseBo.class);
        if(bo != null && StringUtils.isNotEmpty(bo.getUser())) {
            context.get().dataMap.put("user", bo.getUser());
        }
        context.get().dataMap.put(BODY_KEY, body);
    }

    static void start(HttpServletRequest request, HttpServletResponse response) {
        RequestContext.context.set(new Context(request, response));
        RequestContext.response.set(new Response());
        MDC.put(LOGID_KEY, getLogId());
    }

    static void clear() {
        context.remove();
        response.remove();
        MDC.remove(LOGID_KEY);
    }

    static String getRequestInfo() {
        Map<String, Object> params = RequestContext.getDataMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry: params.entrySet()) {
            sb.append("||").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    static String getResponseInfo() {
        StringBuilder sb = new StringBuilder("code=").append(response.get().code);
        if (StringUtils.isNotBlank(response.get().contentType)) {
            sb.append("||contentType=[").append(response.get().contentType).append("]");
        }
        if (StringUtils.isNotBlank(response.get().content)) {
            sb.append("||content=[").append(response.get().content).append("]");
        }
        if (StringUtils.isNotBlank(response.get().path)) {
            sb.append("||path=[").append(response.get().path).append("]");
        }
        return sb.toString();
    }

    public static void sendResponse(int code) {
        response.get().code = code;
    }

    public static void sendResponse(int code, String message) {
        response.get().code = code;
        response.get().content = message;
    }

    public static void sendResponse(String type, String content) {
        response.get().contentType = type;
        response.get().content = content;
    }

    static void sendJsonResponse(String json) {
        context.get().response.setContentType("application/json;charset=UTF-8");
        response.get().contentType = "application/json;charset=UTF-8";
        response.get().content = json;

    }

    static void sendHtmlResponse(String path) {
        response.get().contentType = "text/html;charset=UTF-8";
        response.get().path = path;
    }

    static void sendStaticResponse(String type, String path) {
        response.get().contentType = type;
        response.get().path = path;
    }

    private static class Context {
        public final HttpServletRequest request;
        public final HttpServletResponse response;
        public final Map<String, Object> dataMap = new ConcurrentHashMap<>();
        public final String uri;
        public final String fullUri;
        public final String logId;
        public final String requestedWith;
        public final RemoteSource remoteSource;

        public Context(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
            String logId = LogUtils.genLogid();
            Enumeration names = request.getParameterNames();
            if (names != null) {
                while (names.hasMoreElements()) {
                    String name = String.valueOf(names.nextElement());
                    String value = request.getParameter(name);
                    if (StringUtils.isNotBlank(value)) {
                        if (LOGID_KEY.equalsIgnoreCase(name)) {
                            logId = value;
                        }
                        dataMap.put(name, value);
                    }
                }
            }
            this.logId = logId;
            String uri = request.getRequestURI();
            // 从uri上去掉context_path
            uri = uri.replace(request.getContextPath(), "");
            this.uri = uri;

            String query = StringUtils.isBlank(request.getQueryString()) ? "" : "?" + request.getQueryString();
            this.fullUri = uri + query;

            String requested = request.getHeader("X-Requested-With");
            if (org.apache.commons.lang3.StringUtils.isNotBlank(requested)) {
                requestedWith = requested;
            } else {
                requestedWith = "";
            }

            remoteSource = RequestUtil.getRemoteSource(request);
        }
    }

    /**
     * 目前只做记录，方便日志打印
     */
    private static class Response {
        public int code = HttpServletResponse.SC_OK;
        public String contentType;
        public String content;
        public String path;
    }


}