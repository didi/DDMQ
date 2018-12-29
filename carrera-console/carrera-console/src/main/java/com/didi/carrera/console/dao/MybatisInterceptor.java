package com.didi.carrera.console.dao;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class})})
public class MybatisInterceptor implements Interceptor {


    protected static final String SHOW_DETAILSQL_KEY = "showDetailSql";
    protected static final String SHOW_COSTTIME_KEY = "showCostTime";
    protected static final String SLOW_SQL_MS_KEY = "slowSqlMs";

    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected Logger log = LoggerFactory.getLogger(MybatisInterceptor.class);

    private boolean showDetailSql = true;
    private boolean showCostTime = true;
    private long slowSqlMs = 0;

    public Object intercept(Invocation invocation) throws Throwable {
        Object returnValue;
        if (showDetailSql || showCostTime) {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = null;
            if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
            }
            String sqlId = mappedStatement.getId();
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            Configuration configuration = mappedStatement.getConfiguration();

            long start = System.currentTimeMillis();
            returnValue = invocation.proceed();
            long end = System.currentTimeMillis();
            long time = (end - start);

            String sql = getSql(configuration, boundSql, sqlId, time);
            if (slowSqlMs != 0 && time > slowSqlMs) {
                log.warn(sql);
            } else {
                log.info(sql);
            }
        } else {
            returnValue = invocation.proceed();
        }
        return returnValue;
    }

    public String getSql(Configuration configuration, BoundSql boundSql, String sqlId, long time) {
        StringBuilder str = new StringBuilder(100);
        str.append(sqlId);
        if (showDetailSql) {
            str.append("||");
            str.append(showSql(configuration, boundSql));
        }
        if (showCostTime) {
            str.append("||cost=");
            str.append(time);
            str.append("ms");
        }
        return str.toString();
    }

    private String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            value = "'" + DateFormatUtils.format((Date) obj, DATE_FORMAT) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return Matcher.quoteReplacement(value);
    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // Remove unnecessary empty characters
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");

        if(parameterMappings.size() > 200) {
            return sql;
        }
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
        if (properties != null) {
            String showDetailSql = properties.getProperty(SHOW_DETAILSQL_KEY, Boolean.TRUE.toString());
            String showCostTime = properties.getProperty(SHOW_COSTTIME_KEY, Boolean.TRUE.toString());
            String slowSqlMs = properties.getProperty(SLOW_SQL_MS_KEY, "0");
            this.showDetailSql = Boolean.parseBoolean(showDetailSql);
            this.showCostTime = Boolean.parseBoolean(showCostTime);
            this.slowSqlMs = Long.parseLong(slowSqlMs);
        }
    }

}