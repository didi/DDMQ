package com.xiaojukeji.carrera.consumer.thrift.client;

import org.junit.Test;

import java.util.Random;

import static com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig.GROUP_PATTERN;
import static org.junit.Assert.*;


public class CarreraConfigTest {
    @Test
    public void test() throws Exception {
        CarreraConfig config = new CarreraConfig("some-group", "111.111.111.1:123;111.111.111.1:123;");
        config.validate(false);
    }

    @Test
    public void testGroupPattern() throws Exception {
        //assertFalse(GROUP_PATTERN.matcher(null).matches()); exception!
        assertFalse(GROUP_PATTERN.matcher("").matches());
        assertFalse(GROUP_PATTERN.matcher(" ").matches());
        assertFalse(GROUP_PATTERN.matcher("test group").matches());
        assertTrue(GROUP_PATTERN.matcher("test-group").matches());
    }
}