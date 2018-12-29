package com.xiaojukeji.carrera.utils;

import groovy.lang.GroovyClassLoader;


public class GroovyUtils {

    private static GroovyClassLoader loader = new GroovyClassLoader();

    public static Class parseClass(String groovy) {
        return loader.parseClass(groovy);
    }

}