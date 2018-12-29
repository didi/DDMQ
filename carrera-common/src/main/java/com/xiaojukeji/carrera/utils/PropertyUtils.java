package com.xiaojukeji.carrera.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropertyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    private static class NullAwareBeanUtilsBean extends BeanUtilsBean {
        static NullAwareBeanUtilsBean INSTANCE = new NullAwareBeanUtilsBean();

        @Override
        public void copyProperty(Object dest, String name, Object value)
                throws IllegalAccessException, InvocationTargetException {
            if (value == null) return;
            super.copyProperty(dest, name, value);
        }
    }

    public static void copyNonNullProperties(Object dest, Object src) {
        try {
            NullAwareBeanUtilsBean.INSTANCE.copyProperties(dest, src);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("copy properties error!", e);
        }
    }
}