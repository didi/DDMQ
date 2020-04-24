/**
 * Kuaidadi.com Inc.
 * Copyright (c) 2012-2015 All Rights Reserved.
 */
package com.didi.carrera.console.web.util;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;


public class StringPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isBlank(text)) {
            setValue(null);
        } else {
            setValue(text.trim());
        }
    }

}