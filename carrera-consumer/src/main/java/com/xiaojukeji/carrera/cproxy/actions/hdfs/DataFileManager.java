package com.xiaojukeji.carrera.cproxy.actions.hdfs;

import com.xiaojukeji.carrera.config.v4.cproxy.HdfsConfiguration;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



public class DataFileManager {

    private final Logger LOGGER = LoggerFactory.getLogger(DataFileManager.class);

    private final int MAX_FILE_COUNT = 10000;

    private FileSystem fileSystem;

    private Long releaseTime= 300L;

    private ConcurrentHashMap<String, DataDir> DIR_MAP = new ConcurrentHashMap<>();

    private String DATA_FILE_NAME_PREFIX = "text";

    private String PATH_FORMAT;

    private String ROOT_PATH;

    private Configuration conf;

    private String SEQUENCE_FILE = "SequenceFile";

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(7,
            new NamedThreadFactory("DataFileManagerScheduler"));

    private static ThreadPoolExecutor flushExecutor;

    private HdfsConfiguration config;

    private String group;

    private Map<String, DataFile> CLOSE_FAILED_FILE_MAP = new ConcurrentHashMap<>();

    public int getDirSize() {
        return DIR_MAP.size();
    }

    public int getFileSize() {
        int totalSize = 0;
        for (DataDir dir : DIR_MAP.values()) {
            totalSize = totalSize + dir.getFileSize();
        }

        return totalSize;
    }

    public DataFileManager(String group, HdfsConfiguration config) {
        this.config = config;
        this.group = group;
        try {
            init(config);
        } catch (Exception e) {
            LOGGER.error("init DataFileManager failed {}", e);
        }
    }

    public void init(HdfsConfiguration config) throws Exception {
        String rootPath = config.getRootPath();
        if (StringUtils.isBlank(rootPath)) {
            throw new Exception("The root path is empty");
        }
        ROOT_PATH = rootPath;

        PATH_FORMAT = config.getFilePath();
        if (PATH_FORMAT == null) {
            throw new IllegalArgumentException("no filepath, please check");
        }
        PATH_FORMAT = PATH_FORMAT.trim();

        conf = new Configuration();
        if (StringUtils.isBlank(config.getUserName())) {
            fileSystem = FileSystem.get(URI.create(rootPath), conf);
        } else {
            fileSystem = FileSystem.get(URI.create(rootPath), conf, config.getUserName());
        }

        String fileType = config.getFileType();
        if (!"text".equals(fileType)) {
            DATA_FILE_NAME_PREFIX = "sequence";
        }

        LOGGER.info("DataFileManager inited,root path:" + ROOT_PATH + ", fileNamePrefix:" + DATA_FILE_NAME_PREFIX
                + ", PATH_FORMAT " + PATH_FORMAT);

        flushExecutor = new ThreadPoolExecutor(50, 50, 30, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new NamedThreadFactory("DataFile-Flusher"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new FlushFileTask(), 1, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new CloseFailedStreamScannerTask(), 1, 10, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new FileStreamScannerTask(), 1, 2, TimeUnit.MINUTES);
    }

    private void flushDataFiles() {
        LOGGER.info("FileFlushScanner start, group: {}", group);
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future futureCall = executor.submit(() -> {
            List<Future> futures = new ArrayList<>();
            for (DataDir dataDir : DIR_MAP.values()) {
                for (DataFile dataFile : dataDir.getFileMap().values()) {

                    Future f = flushExecutor.submit(() -> {
                        try {
                            ConcurrentLinkedQueue<UpstreamJob> upstreamJobs = dataFile.getUpstreamJobs();
                            int jobSize = upstreamJobs.size();
                            dataFile.flush();

                            while (jobSize-- > 0) {
                                UpstreamJob job = upstreamJobs.poll();
                                MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.SUCCESS);
                                job.onFinished(true);
                            }
                        } catch (IOException e) {
                            LOGGER.error("group: {}, file: {}, flush failed.", group, dataFile.getFileName(), e);
                        }
                    });
                    futures.add(f);
                }
            }

            for (Future future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    LOGGER.error("Failed to await sync future ", e);
                }
            }
        });

        try {
            futureCall.get();
        } catch (Exception e) {
            LOGGER.error("Failed to get result ", e);
        }
        executor.shutdown();
    }

    public void flush(boolean stop) {
        flushDataFiles();
        if (stop) {
            close();
        }
    }

    private void close() {
        LOGGER.info("Begin to stop dataFileManager!");
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to terminate task scheduledExecutorService1", e);
            }
        }

        Collection<DataDir> dirs = DIR_MAP.values();

        LOGGER.info("Close all dir......");
        for (DataDir dir : dirs) {
            dir.close();
        }
        LOGGER.info("All dir closed.");

        try {
            LOGGER.info("Close HDFS filesystem......");
            fileSystem.close();
            LOGGER.info("HDFS filesystem closed.");
        } catch (IOException e) {
            LOGGER.error("HDFS filesystem close error.", e);
        }

        LOGGER.info("DataFileManager closed");
    }


    public DataFile loadDataFile(String group, String topic, long time, Long pid) {
        String path = generateDirPath(group, topic, time);

        DataDir dir = DIR_MAP.get(path);
        if (dir == null) {
            synchronized (DIR_MAP) {
                dir = DIR_MAP.get(path);
                if (dir == null) {
                    dir = new DataDir(path, releaseTime);
                    dir.setTopic(topic);
                    DIR_MAP.put(path, dir);
                }
            }
        }

        DataFile file = dir.getDataFile(pid);
        if (file == null) {
            synchronized (dir) {
                file = dir.getDataFile(pid);
                if (file == null) {
                    file = createDataFile(dir, pid);
                    DataFile old = dir.putDataFile(pid, file);
                    if (old != null) {
                        LOGGER.error("Old file exist; file:" + old.getPath());
                    }
                }
            }
        }

        return file;
    }

    private String paddingZero(int number) {
        return (number < 10) ? "0" + number : "" + number;
    }

    private DataFile createDataFile(DataDir dir, long pid) {
        // rate limit
        int fileNum = getFileSize();
        int alarmCount = 0;
        while (fileNum > MAX_FILE_COUNT) {
            if (alarmCount++ > 30) {
                alarmCount = 0;
            }
            LOGGER.error("Too many open files; dir:" + dir.getPath() + " ; pid:" + pid);
            try {
                Thread.sleep(2000L);
            } catch (Exception e) {
            }
            fileNum = getFileSize();
        }

        DataFile file;
        if (SEQUENCE_FILE.equals(config.getFileType())) {
            file = new SequenceDataFile(fileSystem, dir, DATA_FILE_NAME_PREFIX, pid, this);
            return file;
        } else {
            file = new DataFile(dir, DATA_FILE_NAME_PREFIX, pid, this);
        }


        FSDataOutputStream stream = null;
        while (stream == null) {
            try {
                stream = fileSystem.create(new Path(file.getPath()), false, 4096);
                file.setStream(stream);
                LOGGER.info("Create data file ok, path:" + file.getPath());
            } catch (Exception e) {
                String error = "Create data file error, path:" + file.getPath() + ", will sleep 1s";
                LOGGER.error(error, e);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e1) {
                }
            }
        }

        return file;
    }

    public String generateDirPath(String group, String topic, long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String dataDir = PATH_FORMAT
                .replace("${group}", group)
                .replace("${topic}", topic);
        dataDir = dataDir
                .replace("{group}", group)
                .replace("{topic}", topic);

        dataDir = dataDir.replace("${yyyy}", String.valueOf(c.get(Calendar.YEAR)));
        dataDir = dataDir.replace("${MM}", paddingZero(c.get(Calendar.MONTH) + 1));
        dataDir = dataDir.replace("${dd}", paddingZero(c.get(Calendar.DAY_OF_MONTH)));
        dataDir = dataDir.replace("${HH}", paddingZero(c.get(Calendar.HOUR_OF_DAY)));
        dataDir = dataDir.replace("${hh}", paddingZero(c.get(Calendar.HOUR_OF_DAY)));
        dataDir = dataDir.replace("${mm}", paddingZero(c.get(Calendar.MINUTE)));

        return new Path(ROOT_PATH + "/" + dataDir).toString();
    }

    public void addCloseFailedStream(DataFile dataFile) {
        CLOSE_FAILED_FILE_MAP.put(dataFile.getPath(), dataFile);
    }

    private class FlushFileTask implements Runnable {

        @Override
        public void run() {
            flushDataFiles();
        }
    }

    private class CloseFailedStreamScannerTask implements Runnable {

        @Override
        public void run() {
            try {
                LOGGER.info("------------CloseFailedStreamScanner begin----------------------------------");

                int beginSize = CLOSE_FAILED_FILE_MAP.size();
                Iterator<Map.Entry<String, DataFile>> iterator = CLOSE_FAILED_FILE_MAP.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, DataFile> next = iterator.next();
                    DataFile stream = next.getValue();
                    try {
                        if (stream.directClose()) {
                            iterator.remove();
                            LOGGER.info("Remove1 form closeFailedStreams,path:" + stream.getPath());
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to flush and close file ", e);
                    }
                    int closeInvokedCount = stream.getCloseInvokedCount();
                    LOGGER.error("CLOSE_FILE_FAILED count " + closeInvokedCount + ", Failed to close DataFile "
                            + next.getKey() + ", try times " + closeInvokedCount);
                }

                LOGGER.info("closeFailedStreams size:" + beginSize + "," + CLOSE_FAILED_FILE_MAP.size());
                LOGGER.info("------------CloseFailedStreamScanner end---------------------------------------");
            } catch (Throwable e) {
                LOGGER.error("CloseFailedStreamScanner run error", e);
            }
        }
    }

    private class FileStreamScannerTask implements Runnable {

        @Override
        public void run() {
            try {
                LOGGER.info("----------------------------FileStreamScanner begin----------------------------");

                Collection<DataDir> dirs = DIR_MAP.values();
                LOGGER.info("Scan dirs begin, size: {}, group: {}", DIR_MAP.size(), group);

                for (DataDir dir : dirs) {
                    if (!dir.isWrittenIdle()) {
                        LOGGER.info("Write Go on! dir:" + dir);
                        continue;
                    }

                    LOGGER.info("Write idle! dir:" + dir.getPath());
                    DIR_MAP.remove(dir.getPath());
                    boolean flag = dir.close();
                    LOGGER.info("Close over! dir:" + dir.getPath());

                    if (!flag) {
                        dir.setLastModified(System.currentTimeMillis());
                        LOGGER.error("Close DataDir failed, dir path = {}", dir.getPath());
                    }
                }

                LOGGER.info("Scan dirs end, size:" + DIR_MAP.size());
                LOGGER.info("----------------------------FileStreamScanner end------------------------------");
            } catch (Throwable e) {
                LOGGER.error("FileStreamScanner run error", e);
            }
        }
    }

    private class NamedThreadFactory implements ThreadFactory {
        private AtomicInteger seq = new AtomicInteger(0);
        private String        name;

        public NamedThreadFactory(String name){
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name + "--" + seq.getAndIncrement());
        }
    }
}
