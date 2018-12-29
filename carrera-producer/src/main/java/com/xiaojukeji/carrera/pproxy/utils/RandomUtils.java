package com.xiaojukeji.carrera.pproxy.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Random;


public class RandomUtils {
    private static final Random rand = new Random();

    public static int nextInt(int bound) {
        return rand.nextInt(bound);
    }

    public static <T> T pick(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(rand.nextInt(list.size()));
        }
    }
}