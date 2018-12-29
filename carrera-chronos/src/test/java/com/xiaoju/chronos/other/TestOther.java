package com.xiaoju.chronos.other;

import com.xiaojukeji.chronos.utils.JsonUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class TestOther {

    @Test
    public void testJson() {
        Map<String, Long> map = new HashMap<>();
        map.put("a_1", 1000L);
        map.put("b_1", 2000L);

        System.out.println(JsonUtils.toJsonString(map));
    }
}