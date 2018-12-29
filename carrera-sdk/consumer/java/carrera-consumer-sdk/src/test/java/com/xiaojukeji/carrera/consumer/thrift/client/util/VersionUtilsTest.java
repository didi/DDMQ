package com.xiaojukeji.carrera.consumer.thrift.client.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class VersionUtilsTest {
    @Test
    public void getVersion() throws Exception {
        assertTrue(VersionUtils.getVersion().matches("java_([\\d.]+)([-]?)([\\w]*)"));
    }

}