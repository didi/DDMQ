package com.didi.carrera.console.dao.model.custom;

import com.didi.carrera.console.dao.dict.TopicCompressionType;

import java.io.Serializable;


public class TopicConfig implements Serializable {
    public static final String  key_useCache = "useCache";
    public static final String  key_autoBatch = "autoBatch";
    public static final String  key_compressionType = "compressionType";

    private boolean useCache = true;

    private boolean autoBatch = false;

    private Byte compressionType = TopicCompressionType.RMQ_COMPRESSION.getIndex();

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isAutoBatch() {
        return autoBatch;
    }

    public void setAutoBatch(boolean autoBatch) {
        this.autoBatch = autoBatch;
    }

    public Byte getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(Byte compressionType) {
        this.compressionType = compressionType;
    }

    @Override
    public String toString() {
        return "TopicConfig{" +
                "useCache=" + useCache +
                ", autoBatch=" + autoBatch +
                ", compressionType=" + compressionType +
                '}';
    }
}