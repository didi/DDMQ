package com.xiaojukeji.chronos.utils;

import java.util.Calendar;


public class TsUtils {

    public static long genTS() {
        return System.currentTimeMillis() / 1000;
    }

    public static long computeTomorrowDeleteTimeMillis(int deleteWhen) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, deleteWhen);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static void main(String[] args) {
        System.out.println(computeTomorrowDeleteTimeMillis(13) - System.currentTimeMillis());
    }
}