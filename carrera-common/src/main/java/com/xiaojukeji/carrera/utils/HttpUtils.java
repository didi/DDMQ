package com.xiaojukeji.carrera.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private static final int MAX_TIMEOUT = 7000;

    static class InnerStaticClass {
        private static PoolingHttpClientConnectionManager connMgr;
        private static RequestConfig requestConfig;

        static {
            // 设置连接池
            connMgr = new PoolingHttpClientConnectionManager();
            // 设置连接池大小
            connMgr.setMaxTotal(100);
            connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

            RequestConfig.Builder configBuilder = RequestConfig.custom();
            // 设置连接超时
            configBuilder.setConnectTimeout(MAX_TIMEOUT);
            // 设置读取超时
            configBuilder.setSocketTimeout(MAX_TIMEOUT);
            // 设置从连接池获取连接实例的超时
            configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
            // 在提交请求之前 测试连接是否可用
            configBuilder.setStaleConnectionCheckEnabled(true);
            requestConfig = configBuilder.build();
        }
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     *
     * @param apiUrl
     * @param json   json对象
     * @return
     */
    public static CloseableHttpResponse doPost(String apiUrl, Object json) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(InnerStaticClass.requestConfig);
            StringEntity stringEntity = new StringEntity(json.toString(), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            LOGGER.error("do post meet exception", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    LOGGER.error("do post meet exception", e);
                }
            }
        }
        return response;
    }

}
