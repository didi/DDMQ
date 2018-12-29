package com.didi.carrera.console.common.util;

import java.util.Random;


public class LogUtils {

    private static final Random random = new Random();

    public static String genLogid() {
        Long logid = (System.currentTimeMillis() << 22) + random.nextInt(1 << 23);
        return logid.toString();
    }
}