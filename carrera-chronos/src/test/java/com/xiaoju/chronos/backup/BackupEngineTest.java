package com.xiaoju.chronos.backup;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.BackupEngine;
import org.rocksdb.BackupInfo;
import org.rocksdb.BackupableDBOptions;
import org.rocksdb.Options;
import org.rocksdb.RestoreOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class BackupEngineTest {

    @Rule
    public TemporaryFolder dbFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder backupFolder = new TemporaryFolder();

    @Test
    public void backupDb() throws RocksDBException {
//        String originPath = dbFolder.getRoot().getAbsolutePath();
//        String backupPath = backupFolder.getRoot().getAbsolutePath();
        String originPath = "/tmp/rocksdb";
        String backupPath = "/tmp/rocksdb_backup";
        System.out.println("originPath=" + originPath);
        System.out.println("backupPath=" + backupPath);
        // Open empty database.
        try (final Options opt = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(opt, originPath)) {

            // Fill database with some test values
            prepareDatabase(db);

            try (RocksIterator it = db.newIterator()) {
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    System.out.println(originPath + ":" + new String(it.key()) + ":" + new String(it.value()));
                }
            }

            // Create two backups
            try (final BackupableDBOptions bopt = new BackupableDBOptions(backupPath);
                 final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                be.createNewBackup(db, false);
                be.createNewBackup(db, true);
                verifyNumberOfValidBackups(be, 2);
            }
        }
    }

    @Test
    public void backupDb2() throws RocksDBException {
//        String originPath = dbFolder.getRoot().getAbsolutePath();
//        String backupPath = backupFolder.getRoot().getAbsolutePath();
        String originPath = "/tmp/rocksdb";
        String originPath2 = "/tmp/rocksdb2";
        String backupPath = "/tmp/rocksdb_backup";
        System.out.println("originPath=" + originPath);
        System.out.println("backupPath=" + backupPath);
        // Open empty database.
        try (final Options opt = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(opt, originPath)) {

            // Fill database with some test values
            prepareDatabase(db);

            try (RocksIterator it = db.newIterator()) {
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    System.out.println(originPath + ":" + new String(it.key()) + ":" + new String(it.value()));
                }
            }

            // Create two backups
            try (final BackupableDBOptions bopt = new BackupableDBOptions(backupPath);
                 final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                be.createNewBackup(db, true);
                be.createNewBackup(db, true);

                //restore the backup
                final List<BackupInfo> backupInfo = verifyNumberOfValidBackups(be, 2);
                // restore db from first backup
                be.restoreDbFromBackup(backupInfo.get(0).backupId(), originPath2, originPath2, new RestoreOptions(true));
                // Open database again.
                RocksDB db2 = RocksDB.open(opt, originPath2);

                try (RocksIterator it = db2.newIterator()) {
                    for (it.seekToFirst(); it.isValid(); it.next()) {
                        System.out.println(originPath2 + ":" + new String(it.key()) + ":" + new String(it.value()));
                    }
                }

                db2.close();
            }
        }


    }

    @Test
    public void deleteBackup() throws RocksDBException {
        // Open empty database.
        try (final Options opt = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(opt,
                     dbFolder.getRoot().getAbsolutePath())) {
            // Fill database with some test values
            prepareDatabase(db);
            // Create two backups
            try (final BackupableDBOptions bopt = new BackupableDBOptions(
                    backupFolder.getRoot().getAbsolutePath());
                 final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                be.createNewBackup(db, false);
                be.createNewBackup(db, true);
                final List<BackupInfo> backupInfo =
                        verifyNumberOfValidBackups(be, 2);
                // Delete the first backup
                be.deleteBackup(backupInfo.get(0).backupId());
                final List<BackupInfo> newBackupInfo =
                        verifyNumberOfValidBackups(be, 1);

                // The second backup must remain.
                assertThat(newBackupInfo.get(0).backupId()).
                        isEqualTo(backupInfo.get(1).backupId());
            }
        }
    }

    @Test
    public void purgeOldBackups() throws RocksDBException {
        // Open empty database.
        try (final Options opt = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(opt,
                     dbFolder.getRoot().getAbsolutePath())) {
            // Fill database with some test values
            prepareDatabase(db);
            // Create four backups
            try (final BackupableDBOptions bopt = new BackupableDBOptions(
                    backupFolder.getRoot().getAbsolutePath());
                 final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                be.createNewBackup(db, false);
                be.createNewBackup(db, true);
                be.createNewBackup(db, true);
                be.createNewBackup(db, true);
                final List<BackupInfo> backupInfo =
                        verifyNumberOfValidBackups(be, 4);
                // Delete everything except the latest backup
                be.purgeOldBackups(1);
                final List<BackupInfo> newBackupInfo =
                        verifyNumberOfValidBackups(be, 1);
                // The latest backup must remain.
                assertThat(newBackupInfo.get(0).backupId()).
                        isEqualTo(backupInfo.get(3).backupId());
            }
        }
    }

    @Test
    public void restoreLatestBackup() throws RocksDBException {
        try (final Options opt = new Options().setCreateIfMissing(true)) {
            // Open empty database.
            RocksDB db = null;
            try {
                db = RocksDB.open(opt,
                        dbFolder.getRoot().getAbsolutePath());
                // Fill database with some test values
                prepareDatabase(db);

                try (final BackupableDBOptions bopt = new BackupableDBOptions(
                        backupFolder.getRoot().getAbsolutePath());
                     final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                    be.createNewBackup(db, true);
                    verifyNumberOfValidBackups(be, 1);
                    db.put("key1".getBytes(), "valueV2".getBytes());
                    db.put("key2".getBytes(), "valueV2".getBytes());
                    be.createNewBackup(db, true);
                    verifyNumberOfValidBackups(be, 2);
                    db.put("key1".getBytes(), "valueV3".getBytes());
                    db.put("key2".getBytes(), "valueV3".getBytes());
                    assertThat(new String(db.get("key1".getBytes()))).endsWith("V3");
                    assertThat(new String(db.get("key2".getBytes()))).endsWith("V3");

                    db.close();
                    db = null;

                    verifyNumberOfValidBackups(be, 2);
                    // restore db from latest backup
                    try (final RestoreOptions ropts = new RestoreOptions(false)) {
                        be.restoreDbFromLatestBackup(dbFolder.getRoot().getAbsolutePath(),
                                dbFolder.getRoot().getAbsolutePath(), ropts);
                    }

                    // Open database again.
                    db = RocksDB.open(opt, dbFolder.getRoot().getAbsolutePath());

                    // Values must have suffix V2 because of restoring latest backup.
                    assertThat(new String(db.get("key1".getBytes()))).endsWith("V2");
                    assertThat(new String(db.get("key2".getBytes()))).endsWith("V2");
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }
    }

    @Test
    public void restoreFromBackup()
            throws RocksDBException {
        try (final Options opt = new Options().setCreateIfMissing(true)) {
            RocksDB db = null;
            try {
                // Open empty database.
                db = RocksDB.open(opt,
                        dbFolder.getRoot().getAbsolutePath());
                // Fill database with some test values
                prepareDatabase(db);
                try (final BackupableDBOptions bopt = new BackupableDBOptions(
                        backupFolder.getRoot().getAbsolutePath());
                     final BackupEngine be = BackupEngine.open(opt.getEnv(), bopt)) {
                    be.createNewBackup(db, true);
                    verifyNumberOfValidBackups(be, 1);
                    db.put("key1".getBytes(), "valueV2".getBytes());
                    db.put("key2".getBytes(), "valueV2".getBytes());
                    be.createNewBackup(db, true);
                    verifyNumberOfValidBackups(be, 2);
                    db.put("key1".getBytes(), "valueV3".getBytes());
                    db.put("key2".getBytes(), "valueV3".getBytes());
                    assertThat(new String(db.get("key1".getBytes()))).endsWith("V3");
                    assertThat(new String(db.get("key2".getBytes()))).endsWith("V3");

                    //close the database
                    db.close();
                    db = null;

                    //restore the backup
                    final List<BackupInfo> backupInfo = verifyNumberOfValidBackups(be, 2);
                    // restore db from first backup
                    be.restoreDbFromBackup(backupInfo.get(0).backupId(),
                            dbFolder.getRoot().getAbsolutePath(),
                            dbFolder.getRoot().getAbsolutePath(),
                            new RestoreOptions(false));
                    // Open database again.
                    db = RocksDB.open(opt,
                            dbFolder.getRoot().getAbsolutePath());
                    // Values must have suffix V2 because of restoring latest backup.
                    assertThat(new String(db.get("key1".getBytes()))).endsWith("V1");
                    assertThat(new String(db.get("key2".getBytes()))).endsWith("V1");
                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }
    }

    /**
     * Verify backups.
     *
     * @param be                      {@link BackupEngine} instance.
     * @param expectedNumberOfBackups numerical value
     * @throws RocksDBException thrown if an error occurs within the native
     *                          part of the library.
     */
    private List<BackupInfo> verifyNumberOfValidBackups(final BackupEngine be,
                                                        final int expectedNumberOfBackups) throws RocksDBException {
        // Verify that backups exist
        assertThat(be.getCorruptedBackups().length).
                isEqualTo(0);
        be.garbageCollect();
        final List<BackupInfo> backupInfo = be.getBackupInfo();
        assertThat(backupInfo.size()).
                isEqualTo(expectedNumberOfBackups);
        return backupInfo;
    }

    /**
     * Fill database with some test values.
     *
     * @param db {@link RocksDB} instance.
     * @throws RocksDBException thrown if an error occurs within the native
     *                          part of the library.
     */
    private void prepareDatabase(final RocksDB db)
            throws RocksDBException {
        db.put("key1".getBytes(), "valueV1".getBytes());
        db.put("key2".getBytes(), "valueV1".getBytes());
        db.put("key3".getBytes(), "valueV1".getBytes());
        db.put("key4".getBytes(), "valueV1".getBytes());
    }
}