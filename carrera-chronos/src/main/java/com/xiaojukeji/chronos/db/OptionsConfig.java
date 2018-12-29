package com.xiaojukeji.chronos.db;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.DbConfig;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.Filter;
import org.rocksdb.LRUCache;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;
import org.rocksdb.util.SizeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class OptionsConfig {
    static final DBOptions DB_OPTIONS = new DBOptions();

    static final ReadOptions READ_OPTIONS = new ReadOptions();

    static final WriteOptions WRITE_OPTIONS_SYNC = new WriteOptions();

    static final WriteOptions WRITE_OPTIONS_ASYNC = new WriteOptions();

    private static final Filter BLOOM_FILTER = new BloomFilter();

    private static final BlockBasedTableConfig BLOCK_BASED_TABLE_CONFIG = new BlockBasedTableConfig();

    static final ColumnFamilyOptions COLUMN_FAMILY_OPTIONS_DEFAULT = new ColumnFamilyOptions();

    private static final List<CompressionType> COMPRESSION_TYPES = new ArrayList<>();

    static {
        DbConfig dbConfig = ConfigManager.getConfig().getDbConfig();
        DB_OPTIONS
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                // 后台flush线程数
                .setMaxBackgroundFlushes(dbConfig.getMaxBackgroundFlushes())
                // 后台compact的线程数
                .setMaxBackgroundCompactions(dbConfig.getMaxBackgroundCompactions())
//                .setDelayedWriteRate(dbConfig.getDelayedWriteRate() * SizeUnit.MB)
                .setMaxOpenFiles(2048)
                .setRowCache(new LRUCache(1024 * SizeUnit.MB, 16, true, 5))
                .setMaxSubcompactions(dbConfig.getMaxSubcompactions());

        DB_OPTIONS.setBaseBackgroundCompactions(dbConfig.getBaseBackgroundCompactions());

        READ_OPTIONS
                .setPrefixSameAsStart(true);

        WRITE_OPTIONS_SYNC
                .setSync(true);

        WRITE_OPTIONS_ASYNC
                .setSync(false);

        // https://github.com/facebook/rocksdb/wiki/RocksDB-Tuning-Guide
        // Bloom filters are always kept in memory for open files,
        // unless BlockBasedTableOptions::cache_index_and_filter_blocks is set to true.
        // Number of open files is controlled by max_open_files option.
        BLOCK_BASED_TABLE_CONFIG
                .setFilter(BLOOM_FILTER)
                .setCacheIndexAndFilterBlocks(true)
                .setPinL0FilterAndIndexBlocksInCache(true);

        // RocksDB 每一层数据的压缩方式, 可选的值为: no,snappy,zlib,bzip2,lz4,lz4hc,zstd.
        // no:no:lz4:lz4:lz4:zstd:zstd 表示 level0 和 level1 不压缩, level2 到 level4 采用 lz4 压缩算法,
        // level5 和 level6 采用 zstd 压缩算法.
        // no 表示没有压缩, lz4 是速度和压缩比较为中庸的压缩算法, zlib 的压缩比很高, 对存储空间比较友好,
        // 但是压缩速度比较慢, 压缩的时候需要占用较多的 CPU 资源. 不同的机器需要根据 CPU 以及 IO 资
        // 源情况来配置怎样的压缩方式. 例如: 如果采用的压缩方式为"no:no:lz4:lz4:lz4:zstd:zstd", 在大量
        // 写入数据的情况下（导数据）, 发现系统的 IO 压力很大（使用 iostat 发现 %util 持续 100% 或者使
        // 用 top 命令发现 iowait 特别多）, 而 CPU 的资源还比较充裕, 这个时候可以考虑将 level0 和
        // level1 开启压缩, 用 CPU 资源换取 IO 资源. 如果采用的压缩方式
        // 为"no:no:lz4:lz4:lz4:zstd:zstd", 在大量写入数据的情况下, 发现系统的 IO 压力不大, 但是 CPU
        // 资源已经吃光了, top -H 发现有大量的 bg 开头的线程（RocksDB 的 compaction 线程）在运行, 这
        // 个时候可以考虑用 IO 资源换取 CPU 资源，将压缩方式改成"no:no:no:lz4:lz4:zstd:zstd". 总之, 目
        // 的是为了最大限度地利用系统的现有资源, 使性能在现有的资源情况下充分发挥.
        COMPRESSION_TYPES.addAll(Arrays.asList(
                CompressionType.NO_COMPRESSION, CompressionType.NO_COMPRESSION,
                CompressionType.LZ4_COMPRESSION, CompressionType.LZ4_COMPRESSION, CompressionType.LZ4_COMPRESSION,
                CompressionType.ZSTD_COMPRESSION, CompressionType.ZSTD_COMPRESSION)
        );

        COLUMN_FAMILY_OPTIONS_DEFAULT
                .setTableFormatConfig(BLOCK_BASED_TABLE_CONFIG)
                // 使用prefix filter
                .useFixedLengthPrefixExtractor(10)
                // 设置每个memtable大小
                .setWriteBufferSize(dbConfig.getWriteBufferSize() * SizeUnit.MB)
                .setMaxWriteBufferNumber(dbConfig.getMaxWriteBufferNumber())
                .setLevel0SlowdownWritesTrigger(dbConfig.getLevel0SlowdownWritesTrigger())
                .setLevel0StopWritesTrigger(dbConfig.getLevel0StopWritesTrigger())
                .setCompressionPerLevel(COMPRESSION_TYPES)
                .setTargetFileSizeBase(dbConfig.getTargetFileSizeBase() * SizeUnit.MB)
                .setMaxBytesForLevelBase(dbConfig.getMaxBytesForLevelBase() * SizeUnit.MB)
                .setOptimizeFiltersForHits(true);
    }
}