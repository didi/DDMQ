package com.xiaojukeji.carrera.cproxy.utils;


public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String newString(byte[] bytes) {
        return org.apache.commons.codec.binary.StringUtils.newStringUtf8(bytes);
    }
}