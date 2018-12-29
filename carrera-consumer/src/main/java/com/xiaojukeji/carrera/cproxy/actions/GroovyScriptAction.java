package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.xiaojukeji.carrera.cproxy.actions.groovy.GroovyContext;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class GroovyScriptAction implements Action {
    private final static String CARRERA_GROOVY_CONTEXT = "carreraContext";

    @SuppressWarnings("rawtypes")
    private final static LoadingCache<String, Class> cache = CacheBuilder
        .newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build(new CacheLoader<String, Class>() {
            private final AtomicLong al = new AtomicLong(0);

            @Override
            public Class load(String key) throws Exception {
                try (GroovyClassLoader groovyLoader = new GroovyClassLoader()) {
                    GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () -> new GroovyCodeSource(key, "Script" + al.getAndIncrement() + ".groovy", "/groovy/shell"));
                    Class clazz = groovyLoader.parseClass(gcs, false);
                    return clazz;
                } catch (Throwable e) {
                    LogUtils.logErrorInfo("GroovyScript_error", "[GroovyErr]", e);
                    return null;
                }
            }

        });

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        String groovyText = job.getUpstreamTopic().getGroovyScript();
        if (StringUtils.isBlank(groovyText)) {
            return Status.FINISH;
        }

        try {
            @SuppressWarnings("rawtypes")
            Class groovyScript = cache.get(groovyText);
            if (groovyScript == null) {
                MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.INVALID);
                return Status.FINISH;
            }

            jsonObject.put(CARRERA_GROOVY_CONTEXT, new GroovyContext(job));
            Script script = InvokerHelper.createScript(groovyScript, new Binding(jsonObject));
            Object scriptRet = script.run();
            if (scriptRet instanceof Boolean) {
                if ((Boolean) scriptRet) {
                    jsonObject.remove(CARRERA_GROOVY_CONTEXT);
                    return Status.CONTINUE;
                }
            }
        } catch (MissingPropertyException e) {
            LogUtils.logErrorInfo("GroovyScript_error", "missing property exception, jsonObject:{}, job:{}, e.msg:{}",
                    JsonUtils.toJsonString(jsonObject), job.info(), e.getMessageWithoutLocationText());
        } catch (Throwable e) {
            LogUtils.logErrorInfo("GroovyScript_error", "error when running groovy script, job={}, e={}", job, e.getMessage());
        }

        MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.INVALID);
        return Status.FINISH;
    }
}