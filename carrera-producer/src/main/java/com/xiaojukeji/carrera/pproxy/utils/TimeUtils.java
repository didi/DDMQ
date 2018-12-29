package com.xiaojukeji.carrera.pproxy.utils;


public class TimeUtils {

    public static long getCurTime() {
        return System.nanoTime();
    }

    public static double getElapseTime(long preTime) {
        return (System.nanoTime() - preTime) / 1000 / 1000.0;
    }

    public static long getElapseMicros(long preTime) {
        return (System.nanoTime() - preTime) / 1000;
    }

    public static long getElapseMills(long preTime) {
        return (System.nanoTime() - preTime) / 1000000;
    }
}