package com.xiaojukeji.carrera.utils;


import org.apache.commons.lang.RandomStringUtils;


public class RandomKeyUtils {
    public static String randomKey(int keySize) {
        return RandomStringUtils.randomAlphanumeric(keySize);
    }
}