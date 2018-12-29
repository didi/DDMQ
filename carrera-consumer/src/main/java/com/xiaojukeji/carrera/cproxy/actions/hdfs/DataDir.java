package com.xiaojukeji.carrera.cproxy.actions.hdfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DataDir {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataDir.class);

    private String path;

    private String topic;

    private Map<Long, DataFile> fileMap = new ConcurrentHashMap<>();

    private long lastModified = System.currentTimeMillis();

    private long releaseTime;

    public DataDir(String name, long releaseTime) {
        this.path = name;
        this.releaseTime = releaseTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Map<Long, DataFile> getFileMap() {
        return fileMap;
    }

    public int getFileSize() {
        return fileMap.size();
    }

    public boolean close() {
        // close all open files in directory.
        Collection<DataFile> files = fileMap.values();
        LOGGER.info("Close dir :" + path);
        boolean flag = true;
        for (DataFile file : files) {
            if (!file.close()) {
                flag = false;
            }
        }
        return flag;
    }

    public boolean isWrittenIdle() {
        long secondDiff = (System.currentTimeMillis() - lastModified) / 1000;
        if (secondDiff < releaseTime) {
            return false;
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public DataFile getDataFile(Long pid) {
        return fileMap.get(pid);
    }

    public void deleteDataFile(Long pid) {
        fileMap.remove(pid);
        LOGGER.info("Delete file from dir; dir:" + path + "; pid:" + pid);
    }

    public DataFile putDataFile(Long pid, DataFile file) {
        LOGGER.info("Put file to dir; dir:" + path + "; pid:" + pid + "; file:" + file.getFileName());
        return fileMap.put(pid, file);
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{name:").append(path).append(",lastModified:").append(lastModified);
        sb.append(",files:[");
        Collection<DataFile> files = fileMap.values();
        for (DataFile file : files) {
            sb.append(file.toString()).append(",");
        }
        sb.append("]}");

        return sb.toString();
    }
}
