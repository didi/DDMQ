package com.didi.carrera.console.web.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.support.spring.FastJsonContainer;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import com.alibaba.fastjson.support.spring.MappingFastJsonValue;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class JsonMessageConverter extends FastJsonHttpMessageConverter4 {

    private Logger accessLogger = LoggerFactory.getLogger("carrera.console.framework.request");

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        byte[] bodyBytes = getBody(inputMessage);
        if (bodyBytes == null || bodyBytes.length == 0) {
            return null;
        }

        return JSON.parseObject(bodyBytes, 0, bodyBytes.length, getFastJsonConfig().getCharset(), getType(clazz, null), getFastJsonConfig().getFeatures());
    }

    private byte[] getBody(HttpInputMessage inputMessage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream in = inputMessage.getBody();

        byte[] buf = new byte[1024];
        for (; ; ) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }

            if (len > 0) {
                baos.write(buf, 0, len);
            }
        }
        if (baos.size() == 0) {
            accessLogger.info("Request Body={}", RequestContext.getBody());
            String body = RequestContext.getBody();
            if (StringUtils.isNotBlank(body)) {
                return body.getBytes("UTF-8");
            }
            return null;
        } else {
            // 记录请求的body内容
            RequestContext.setBody(baos.toString());
            accessLogger.info("Request Body={}", RequestContext.getBody());

            return baos.toByteArray();
        }
    }

    @Override
    public Object read(Type type, //
                       Class<?> contextClass, //
                       HttpInputMessage inputMessage //
    ) throws IOException, HttpMessageNotReadableException {
        byte[] bodyBytes = getBody(inputMessage);
        if (bodyBytes == null || bodyBytes.length == 0) {
            return null;
        }

        return JSON.parseObject(bodyBytes, 0, bodyBytes.length, getFastJsonConfig().getCharset(), getType(type, contextClass), getFastJsonConfig().getFeatures());
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (ByteArrayOutputStream outnew = new ByteArrayOutputStream()) {
            HttpHeaders headers = outputMessage.getHeaders();

            //获取全局配置的filter
            SerializeFilter[] globalFilters = getFastJsonConfig().getSerializeFilters();
            List<SerializeFilter> allFilters = new ArrayList<>(Arrays.asList(globalFilters));

            boolean isJsonp = false;
            Object value = strangeCodeForJackson(object);

            if (value instanceof FastJsonContainer) {
                FastJsonContainer fastJsonContainer = (FastJsonContainer) value;
                PropertyPreFilters filters = fastJsonContainer.getFilters();
                allFilters.addAll(filters.getFilters());
                value = fastJsonContainer.getValue();
            }

            if (value instanceof MappingFastJsonValue) {
                isJsonp = true;
                value = ((MappingFastJsonValue) value).getValue();
            } else if (value instanceof JSONPObject) {
                isJsonp = true;
            }


            int len = writePrefix(outnew, object);
            len += JSON.writeJSONString(outnew, //
                    getFastJsonConfig().getCharset(), //
                    value, //
                    getFastJsonConfig().getSerializeConfig(), //
                    allFilters.toArray(new SerializeFilter[allFilters.size()]),
                    getFastJsonConfig().getDateFormat(), //
                    JSON.DEFAULT_GENERATE_FEATURE, //
                    getFastJsonConfig().getSerializerFeatures());
            len += writeSuffix(outnew, object);

            if (isJsonp) {
                headers.setContentType(APPLICATION_JAVASCRIPT);
            }
            if (getFastJsonConfig().isWriteContentLength()) {
                headers.setContentLength(len);
            }

            headers.set("carrera_logid", RequestContext.getLogId());
            RequestContext.sendJsonResponse(outnew.toString());

            outnew.writeTo(outputMessage.getBody());

        } catch (JSONException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    private Object strangeCodeForJackson(Object obj) {
        if (obj != null) {
            String className = obj.getClass().getName();
            if ("com.fasterxml.jackson.databind.node.ObjectNode".equals(className)) {
                return obj.toString();
            }
        }
        return obj;
    }

}