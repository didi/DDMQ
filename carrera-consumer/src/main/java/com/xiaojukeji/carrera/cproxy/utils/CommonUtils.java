package com.xiaojukeji.carrera.cproxy.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CommonUtils {
    public static <K,V> Map<K, V> mapRemoveKeys(Map<K, V> map, Set<K> deleteKeys) {
        if (map == null) {
            return null;
        }

        Map<K,V> mapNew = new HashMap<>(map);
        deleteKeys.forEach(mapNew::remove);

        if (mapNew.isEmpty()) {
            return null;
        }
        return mapNew;
    }
}