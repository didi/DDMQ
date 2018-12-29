package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;

import com.xiaojukeji.carrera.config.v4.TopicConfig;


public enum TopicProduceMode {
    SAME_IDC((byte) 0, "同机房生产"),
    OTHER((byte) 1, "自定义");

    private byte index;

    private String name;

    TopicProduceMode(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return this.index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TopicProduceMode getByIndex(byte index) {
        List<TopicProduceMode> all = Arrays.asList(values());
        for (TopicProduceMode level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

    public static TopicConfig.ProduceMode getZkMode(Byte index) {
        if(index == null) {
            return null;
        }

        if(index == SAME_IDC.index) {
            return TopicConfig.ProduceMode.SAME_IDC;
        } else if (index == OTHER.index) {
            return TopicConfig.ProduceMode.OTHER;
        }

        return null;
    }

}