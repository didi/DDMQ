package com.xiaojukeji.carrera.cproxy.actions.http;

import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;


public class PushService {
    public static final Logger LOGGER = getLogger(PushService.class);

    private AsyncHttpClient client;

    private PushService() {
        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().build();
        client = new DefaultAsyncHttpClient(config);
    }

    public AsyncHttpClient getClient() {
        return client;
    }

    public void shutdown() {
        LOGGER.info("PushService shutdown start");
        try {
            client.close();
        } catch (IOException e) {
            LogUtils.logErrorInfo("PushService_shutdown_error", "closing client", e);
        }
    }

    private static class Singleton {
        static PushService instance = new PushService();
    }

    public static PushService getInstance() {
        return Singleton.instance;
    }
}