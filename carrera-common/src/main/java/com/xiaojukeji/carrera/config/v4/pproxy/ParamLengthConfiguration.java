package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;


public class ParamLengthConfiguration implements ConfigurationValidator {
    private boolean failWhenIllegal = true;
    private int keyLenMax = 255;
    private int tagLenMax = 255;

    public boolean isFailWhenIllegal() {
        return failWhenIllegal;
    }

    public void setFailWhenIllegal(boolean failWhenIllegal) {
        this.failWhenIllegal = failWhenIllegal;
    }

    public int getKeyLenMax() {
        return keyLenMax;
    }

    public void setKeyLenMax(int keyLenMax) {
        this.keyLenMax = keyLenMax;
    }

    public int getTagLenMax() {
        return tagLenMax;
    }

    public void setTagLenMax(int tagLenMax) {
        this.tagLenMax = tagLenMax;
    }

    @Override
    public String toString() {
        return "ParamLengthConfiguration{" +
                "failWhenIllegal=" + failWhenIllegal +
                ", keyLenMax=" + keyLenMax +
                ", tagLenMax=" + tagLenMax +
                '}';
    }

    @Override
    public boolean validate() {
        return keyLenMax > 0 && tagLenMax > 0;
    }
}