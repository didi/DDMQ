package com.didi.carrera.console.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;


public class RequestUtil {

    /**
     * 获取客户端真实ip
     */
    public static RemoteSource getRemoteSource(HttpServletRequest request) {

        String ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return new RemoteSource(ip, "X-Real-IP");
        }

        ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return new RemoteSource(ip, "X-Forwarded-For");
        }

        return new RemoteSource(request.getRemoteAddr(), "Remote-Addr");
    }
}