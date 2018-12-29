package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.apache.commons.lang3.StringUtils;


public class DelayConfiguration implements ConfigurationValidator {

    private int innerTopicNum;

    private String chronosInnerTopicPrefix;

    public int getInnerTopicNum() {
        return innerTopicNum;
    }

    public void setInnerTopicNum(int innerTopicNum) {
        this.innerTopicNum = innerTopicNum;
    }

    public String getChronosInnerTopicPrefix() {
        return chronosInnerTopicPrefix;
    }

    public void setChronosInnerTopicPrefix(String chronosInnerTopicPrefix) {
        this.chronosInnerTopicPrefix = chronosInnerTopicPrefix;
    }

    @Override
    public String toString() {
        return "DelayConfiguration{" +
                "innerTopicNum=" + innerTopicNum +
                ", chronosInnerTopicPrefix='" + chronosInnerTopicPrefix + '\'' +
                '}';
    }

    @Override
    public boolean validate() {
        return innerTopicNum > 0 && StringUtils.isNotEmpty(chronosInnerTopicPrefix);
    }
}