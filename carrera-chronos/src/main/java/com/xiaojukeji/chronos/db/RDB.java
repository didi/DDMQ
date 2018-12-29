package com.xiaojukeji.chronos.db;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.autobatcher.Batcher;
import com.xiaojukeji.chronos.utils.FileIOUtils;
import org.rocksdb.AbstractImmutableNativeReference;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.xiaojukeji.chronos.db.BackupDB.DB_PATH_BACKUP;
import static com.xiaojukeji.chronos.db.BackupDB.DB_PATH_RESTORE;
import static com.xiaojukeji.chronos.db.CFManager.CF_DESCRIPTORS;
import static com.xiaojukeji.chronos.db.CFManager.CF_HANDLES;
import static com.xiaojukeji.chronos.db.CFManager.initCFManger;


public class RDB {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RDB.class);

    static RocksDB DB;

    public static void init(final String dbPath) {
        try {
            final long start = System.currentTimeMillis();
            boolean result = FileIOUtils.createOrExistsDir(new File(dbPath));
            assert(result != false);

            result = FileIOUtils.createOrExistsDir(new File(DB_PATH_BACKUP));
            assert(result != false);

            result = FileIOUtils.createOrExistsDir(new File(DB_PATH_RESTORE));
            assert(result != false);

            DB = RocksDB.open(OptionsConfig.DB_OPTIONS, dbPath, CF_DESCRIPTORS, CF_HANDLES);
            assert (DB != null);

            initCFManger(CF_HANDLES);

            final long cost = System.currentTimeMillis() - start;
            LOGGER.info("succ open rocksdb, path:{}, cost:{}ms", dbPath, cost);
        } catch (RocksDBException e) {
            LOGGER.error("error while open rocksdb, path:{}, err:{}", dbPath, e.getMessage(), e);
        }
    }

    public static boolean writeSync(final WriteBatch writeBatch) {
        return write(OptionsConfig.WRITE_OPTIONS_SYNC, writeBatch);
    }

    public static boolean writeAsync(final WriteBatch writeBatch) {
        return write(OptionsConfig.WRITE_OPTIONS_ASYNC, writeBatch);
    }

    private static boolean write(final WriteOptions writeOptions, final WriteBatch writeBatch) {
        try {
            DB.write(writeOptions, writeBatch);
            LOGGER.debug("succ write writeBatch, size:{}", writeBatch.count());
        } catch (RocksDBException e) {
            // TODO: 2017/11/8 上报写入失败
            LOGGER.error("error while write batch, err:{}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static RocksIterator newIterator(final ColumnFamilyHandle cfHandle) {
        return DB.newIterator(cfHandle, OptionsConfig.READ_OPTIONS);
    }

    public static boolean deleteFilesInRange(final ColumnFamilyHandle cfh, final byte[] beginKey,
                                             final byte[] endKey) {
        try {
            DB.deleteRange(cfh, beginKey, endKey);
            LOGGER.debug("succ delete range, columnFamilyHandle:{}, beginKey:{}, endKey:{}",
                    cfh.toString(), new String(beginKey), new String(endKey));
        } catch (RocksDBException e) {
            LOGGER.error("error while delete range, columnFamilyHandle:{}, beginKey:{}, endKey:{}, err:{}",
                    cfh.toString(), new String(beginKey), new String(endKey), e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static boolean delete(final ColumnFamilyHandle cfh, final byte[] key) {
        try {
            DB.delete(cfh, key);
            LOGGER.debug("succ delete key, columnFamilyHandle:{}, key:{}", cfh.toString(), new String(key));
        } catch (RocksDBException e) {
            LOGGER.error("error while delete key, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.toString(), new String(key), e.getMessage(), e);
        }
        return true;
    }

    public static byte[] get(final ColumnFamilyHandle cfh, final byte[] key) {
        try {
            return DB.get(cfh, key);
        } catch (RocksDBException e) {
            LOGGER.error("error while get, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.toString(), new String(key), e.getMessage(), e);
            return null;
        }
    }

    public static boolean put(final ColumnFamilyHandle cfh, final byte[] key, final byte[] value) {
        try {
            DB.put(cfh, key, value);
        } catch (RocksDBException e) {
            LOGGER.error("error while put, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.isOwningHandle(), new String(key), e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static boolean put(final ColumnFamilyHandle cfh, WriteOptions writeOptions, final byte[] key, final byte[] value) {
        try {
            DB.put(cfh, writeOptions, key, value);
        } catch (RocksDBException e) {
            LOGGER.error("error while put, columnFamilyHandle:{}, key:{}, err:{}",
                    cfh.isOwningHandle(), new String(key), e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static boolean putSync(final ColumnFamilyHandle cfh, final byte[] key, final byte[] value) {
         return put(cfh, OptionsConfig.WRITE_OPTIONS_SYNC, key, value);
    }

    public static ColumnFamilyHandle createCF(final String name) {
        try {
            ColumnFamilyHandle cfh = DB.createColumnFamily(new ColumnFamilyDescriptor(name.getBytes(Charsets.UTF_8)));
            return cfh;
        } catch (RocksDBException e) {
            LOGGER.error("error while createCF, msg:{}", e.getMessage(), e);
            return null;
        }
    }

    public static boolean dropCF(final ColumnFamilyHandle cfh) {
        try {
            DB.dropColumnFamily(cfh);
            return true;
        } catch (RocksDBException e) {
            LOGGER.error("error while dropCF, msg:{}", e.getMessage(), e);
            return false;
        }
    }

    public static void close() {
        Batcher.getInstance().close();
        OptionsConfig.DB_OPTIONS.close();
        OptionsConfig.WRITE_OPTIONS_SYNC.close();
        OptionsConfig.WRITE_OPTIONS_ASYNC.close();
        OptionsConfig.READ_OPTIONS.close();
        OptionsConfig.COLUMN_FAMILY_OPTIONS_DEFAULT.close();
        CF_HANDLES.forEach(AbstractImmutableNativeReference::close);
        if (DB != null) {
            DB.close();
        }
    }
}