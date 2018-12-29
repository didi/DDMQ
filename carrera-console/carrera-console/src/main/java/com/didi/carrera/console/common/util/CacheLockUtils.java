package com.didi.carrera.console.common.util;

import java.util.concurrent.ConcurrentHashMap;


public class CacheLockUtils {
    private static final ConcurrentHashMap<String, Integer> CACHE_LOCK = new ConcurrentHashMap<>();

    public static boolean lock(String key) {
        return CACHE_LOCK.putIfAbsent(key, 1) == null;
    }

    public static void unlock(String key) {
        CACHE_LOCK.remove(key);
    }
}