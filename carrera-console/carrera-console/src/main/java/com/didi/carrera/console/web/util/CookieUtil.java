package com.didi.carrera.console.web.util;

import com.didi.carrera.console.common.util.LogUtils;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: zanglei@didiglobal.com
 * Date: 2019-10-16
 * Time: 12:06
 */
public class CookieUtil {

    public static final String COOKIE_KEY="JSESSION_ID";

    public static final Set<String> cookies = new HashSet<>();

    public static Cookie newCookie() {
        String value = LogUtils.genLogid();
        Cookie cookie = new Cookie(COOKIE_KEY, value);
        cookie.setPath("/");
        cookies.add(value);
        return cookie;
    }
}
