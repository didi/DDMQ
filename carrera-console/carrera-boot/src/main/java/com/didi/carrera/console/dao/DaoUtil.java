package com.didi.carrera.console.dao;

import org.apache.commons.lang3.StringUtils;


public class DaoUtil {

    public static String getLikeField(String field) {
        if(StringUtils.isEmpty(field)) {
            return null;
        }
        return "%" + field + "%";
    }
}