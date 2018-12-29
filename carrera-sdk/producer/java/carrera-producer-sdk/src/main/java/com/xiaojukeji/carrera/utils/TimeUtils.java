package com.xiaojukeji.carrera.utils;


public class TimeUtils {
    public static long getCurTime() {
        return System.currentTimeMillis();
    }

    public static long getElapseTime(long preTime) {
        return System.currentTimeMillis() - preTime;
    }
}