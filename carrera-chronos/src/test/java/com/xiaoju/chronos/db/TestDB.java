package com.xiaoju.chronos.db;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.db.CFManager;
import com.xiaojukeji.chronos.db.RDB;
import org.junit.Test;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;

import java.util.concurrent.TimeUnit;


public class TestDB {
    private static String dbPath = "/Users/didi/rocks_db";
    private static String configPath = "/Users/didi/work/carrera-chronos/src/main/resources/chronos.yaml";

    public static void init() {
        ConfigManager.initConfig(configPath);
        RDB.init(dbPath);
    }

    private static final long MIN_TIMESTAMP = 0000000000;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000; //ms

    @Test
    public void testDeleteRange() {
        init();
        WriteBatch wb = new WriteBatch();
        ColumnFamilyHandle cfHandle = CFManager.CFH_DEFAULT;

        long st = System.currentTimeMillis();
        for(int i=100000; i<200000; i++) {
            wb.put(cfHandle, ("1324356527-" + i + "-5-5-345-356-234-232").getBytes(), "tasdfasdgasdfestfordb".getBytes());

            if(i % 30 == 0) {
                RDB.writeAsync(wb);
                wb.clear();
            }
        }
        for(int i=100000; i<200000; i++) {
            wb.put(cfHandle, ("1324356525-" + i + "-5-5-345-356-234-232").getBytes(), "tasdfasdgasdfestfordb".getBytes());

            if(i % 30 == 0) {
                RDB.writeAsync(wb);
                wb.clear();
            }
        }
        for(int i=100000; i<200000; i++) {
            wb.put(cfHandle, ("1324356529-" + i + "-5-5-345-356-234-232").getBytes(), "tasdfasdgasdfestfordb".getBytes());

            if(i % 30 == 0) {
                RDB.writeAsync(wb);
                wb.clear();
            }
        }
        RDB.writeAsync(wb);

        long ed = System.currentTimeMillis();
        System.out.println("write cost :" + (ed - st));

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        RocksIterator it = RDB.newIterator(cfHandle);
        byte[] now = "1324356527".getBytes();
        long count = 0;
        for(it.seek(now); it.isValid(); it.next()) {
//            System.out.println(new String(it.key()) + " " + new String(it.value()));
            count++;
            if(count == 100000)
                break;
        }
        it.close();
        long end = System.currentTimeMillis();
        System.out.println("cost : " + (end - start) + " count:" +count);
        RDB.deleteFilesInRange(CFManager.CFH_DEFAULT, "132435653".getBytes(), "1324356529".getBytes());

        count = 0;
        it = RDB.newIterator(cfHandle);
        now = "1324356525".getBytes();
        for(it.seek(now); it.isValid(); it.next()) {
//            System.out.println(new String(it.key()) + " " + new String(it.value()));
            count++;
            if(count == 100000)
                break;
        }
        it.close();
        end = System.currentTimeMillis();
        System.out.println("cost : " + (end - start) + " count:" +count);

        destructor();
    }

    public static void destructor() {
        RDB.close();
    }
}