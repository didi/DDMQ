package com.xiaojukeji.carrera.pproxy.kafka;

import org.slf4j.event.Level;

import java.io.Closeable;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;

public class CoreUtils {

    public static void close (Closeable closeable) {
        close(closeable, Level.ERROR);
    }

    public static void close(Closeable closeable, Level level) {
        try {
            closeable.close();
        } catch (Throwable e) {
            switch (level) {
                case INFO: KafkaAdapterLog.info(e.getMessage(), e);
                case WARN: KafkaAdapterLog.warn(e.getMessage(), e);
                case ERROR: KafkaAdapterLog.error(e.getMessage(), e);
                case DEBUG: KafkaAdapterLog.debug(e.getMessage(), e);
                case TRACE: KafkaAdapterLog.trace(e.getMessage(), e);
            }
        }
    }
}
