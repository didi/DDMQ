package com.didi.carrera.console.web.controller;

import com.didi.carrera.console.service.impl.TopicConfServiceImpl;
import com.didi.carrera.console.web.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: zanglei@didiglobal.com
 * Date: 2019-10-16
 * Time: 11:59
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("uri: {}, url: {}", request.getRequestURI(), request.getRequestURL());
        if (!request.getRequestURI().startsWith("/carrera/api")) {
            return true;
        }

        if (request.getRequestURI().contains("login") || request.getRequestURI().contains("logout")) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(401);
            return false;
        } else {
            for (Cookie cookie : cookies) {
                if (CookieUtil.cookies.contains(cookie.getValue())) {
                    return true;
                }
            }
        }
        response.setStatus(401);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
