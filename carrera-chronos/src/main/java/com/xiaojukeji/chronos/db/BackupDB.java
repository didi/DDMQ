package com.xiaojukeji.chronos.db;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.enums.BackupState;
import com.xiaojukeji.chronos.enums.RestoreState;
import com.xiaojukeji.chronos.utils.LogUtils;
import org.rocksdb.BackupEngine;
import org.rocksdb.BackupInfo;
import org.rocksdb.BackupableDBOptions;
import org.rocksdb.Env;
import org.rocksdb.RestoreOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;

import java.util.List;


public class BackupDB {
    private static final Logger LOGGER = LogUtils.BACKUP_RESTORE_LOGGER;

    public static final String DB_PATH_BACKUP = ConfigManager.getConfig().getDbConfig().getDbPathBackup();
    public static final String DB_PATH_RESTORE = ConfigManager.getConfig().getDbConfig().getDbPathRestore();
    private static volatile boolean backuping = false;
    private static volatile boolean restoring = false;

    public static BackupState backup() {
        if (backuping) {
            LOGGER.info("is backuping, return");
            return BackupState.BEING_BACKUP;
        }

        LOGGER.info("start backup");
        backuping = true;
        try (final BackupableDBOptions bopt = new BackupableDBOptions(DB_PATH_BACKUP);
             final BackupEngine be = BackupEngine.open(Env.getDefault(), bopt)) {

            /**
             * Captures the state of the database in the latest backup
             *
             * @param db The database to backup
             * @param flushBeforeBackup When true, the Backup Engine will first issue a
             *                          memtable flush and only then copy the DB files to
             *                          the backup directory. Doing so will prevent log
             *                          files from being copied to the backup directory
             *                          (since flush will delete them).
             *                          When false, the Backup Engine will not issue a
             *                          flush before starting the backup. In that case,
             *                          the backup will also include log files
             *                          corresponding to live memtables. The backup will
             *                          always be consistent with the current state of the
             *                          database regardless of the flushBeforeBackup
             *                          parameter.
             *
             * Note - This method is not thread safe
             *
             * @throws RocksDBException thrown if a new backup could not be created
             */
            boolean flushBeforeBackup = false;
            be.createNewBackup(RDB.DB, flushBeforeBackup);

            List<BackupInfo> backupInfos = be.getBackupInfo();
            for (int i = 0; i < backupInfos.size(); i++) {
                LOGGER.info("backupInfo[{}}, backupId:{}, timestamp:{}, size:{}, numberFiles:{}", i, backupInfos.get(i).backupId(),
                        backupInfos.get(i).timestamp(), backupInfos.get(i).size(), backupInfos.get(i).numberFiles());
            }

            return BackupState.SUCCESS;
        } catch (RocksDBException e) {
            LOGGER.error("error while backup, path:{}, err:{}", DB_PATH_BACKUP, e.getMessage(), e);
            return BackupState.FAIL;
        } finally {
            backuping = false;
            LOGGER.info("end backup");
        }
    }

    public static RestoreState restore() throws RocksDBException {
        if (restoring) {
            LOGGER.info("is restoring, return");
            return RestoreState.BEING_RESTORE;
        }

        LOGGER.info("start restore");
        restoring = true;
        RocksDB restoreDB = null;
        try (final BackupableDBOptions bopt = new BackupableDBOptions(DB_PATH_BACKUP);
             final BackupEngine be = BackupEngine.open(Env.getDefault(), bopt)) {
            // restore db from first backup

            /**
             * @param keepLogFiles If true, restore won't overwrite the existing log files
             *   in wal_dir. It will also move all log files from archive directory to
             *   wal_dir. Use this option in combination with
             *   BackupableDBOptions::backup_log_files = false for persisting in-memory
             *   databases.
             *   Default: false
             */
            boolean keepLogFiles = false;
            be.restoreDbFromLatestBackup(DB_PATH_RESTORE, DB_PATH_RESTORE, new RestoreOptions(keepLogFiles));
            // open database again.
            restoreDB = RocksDB.open(OptionsConfig.DB_OPTIONS, DB_PATH_RESTORE, CFManager.CF_DESCRIPTORS, CFManager.CF_HANDLES);

            int i = 0;
            try (RocksIterator it = restoreDB.newIterator()) {
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    LOGGER.info("i:{}, key:{}, value:{}", i++, new String(it.key()), new String(it.value()));
                    if (i == 10) {
                        break;
                    }
                }
            }

            return RestoreState.SUCCESS;
        } catch (RocksDBException e) {
            LOGGER.error("error while restore, path:{}, err:{}", DB_PATH_RESTORE, e.getMessage(), e);
            return RestoreState.FAIL;
        } finally {
            if (restoreDB != null) {
                restoreDB.close();
            }

            restoring = false;
            LOGGER.info("end restore");
        }
    }
}