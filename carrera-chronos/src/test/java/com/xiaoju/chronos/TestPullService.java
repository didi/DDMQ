package com.xiaoju.chronos;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.db.RDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;


public class TestPullService {
    private static String dbPath = "/Users/didi/rocks_db";
    private static String configPath = "/Users/didi/work/carrera-chronos/src/main/resources/chronos.yaml";

    @BeforeClass
    public static void init() {
        ConfigManager.initConfig(configPath);
        RDB.init(dbPath);
    }

    @AfterClass
    public static void destructor() {
        RDB.close();
    }
}