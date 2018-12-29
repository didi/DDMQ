package com.didi.carrera.console.web.util;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class DateTimePropertyEditor extends PropertyEditorSupport {

    /**
     * Parse the Date from the given text, using the specified DateFormat.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isBlank(text)) {
            setValue(null);
            return;
        }
        SimpleDateFormat dateFormat;

        if (text.contains(":")) { // 时分秒
            if (text.split(":").length == 2) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            } else {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        try {
            setValue(dateFormat.parse(text));
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
        }

    }
    
}