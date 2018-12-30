package com.didi.carrera.console.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.alibaba.fastjson.JSONException;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.service.exception.ConvertDataException;
import com.didi.carrera.console.service.exception.MqException;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.util.NestedServletException;


public class ConsoleExceptionFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Logger accessLogger = LoggerFactory.getLogger("carrera.console.framework.request");

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        long start = System.currentTimeMillis();

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        RequestContext.start(req, resp);

        if (accessLogger.isInfoEnabled()) {
            accessLogger.info("HttpStart: [{}]{}||{}", RequestContext.getUri(), RequestContext.getRequestInfo(),
                    RequestContext.getRemoteSource());
        }

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Filter exception: ", e);
            String msg = "数据提交出错，请稍后重试";
            ConsoleBaseResponse.Status status = ConsoleBaseResponse.Status.INVALID_PARAM;
            if (NestedServletException.class.isInstance(e)) {
                //参数绑定错误异常
                NestedServletException ex = (NestedServletException) e;
                Throwable rootCause = ex.getRootCause();

                if (BindException.class.isInstance(rootCause)) {
                    msg = "参数校验失败";
                } else if (IllegalArgumentException.class.isInstance(rootCause)) {
                    msg = rootCause.getMessage();
                } else if (JSONException.class.isInstance(rootCause)) {
                    msg = "Json 格式错误";
                } else if (MqException.class.isInstance(rootCause)) {
                    msg = rootCause.getMessage();
                } else if (ConvertDataException.class.isInstance(rootCause)) {
                    msg = rootCause.getMessage();
                } else {
                    status = ConsoleBaseResponse.Status.INTERNAL_ERROR;
                    msg = rootCause.getMessage();
                }
            }
            String json = FastJsonUtils.toJson(ConsoleBaseResponse.error(status, msg));
            RequestContext.sendJsonResponse(json);
            // 返回错误提示信息
            resp.getWriter().write(json);
        } finally {
            long past = System.currentTimeMillis() - start;
            if (accessLogger.isInfoEnabled()) {
                accessLogger.info("HttpEnd: [{}]||{}||{}||cost={}ms", RequestContext.getUri(),
                        RequestContext.getResponseInfo(), RequestContext.getRemoteSource(), past);
            }

            RequestContext.clear();
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}