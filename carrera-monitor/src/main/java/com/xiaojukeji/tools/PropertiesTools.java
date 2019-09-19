package com.xiaojukeji.tools;

import java.io.*;
import java.util.Properties;

public class PropertiesTools {

    /**
     * 根据文件名获取classpath下的proterties配置文件
     *
     * @param fileName 文件名 xxx.properties
     * @return
     */
    public static Properties getProperties(String fileName) {
        Properties prop = new Properties();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("解析文件 " + fileName + " 错误,请检查文件是否存在。");
        }
        try {
            InputStreamReader reader = new InputStreamReader(stream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            prop.load(bufferedReader);
        } catch (IOException e) {
            throw new IllegalArgumentException("解析文件 " + fileName + " 错误,请检查文件格式是否正确。");
        }
        return prop;
    }

}
