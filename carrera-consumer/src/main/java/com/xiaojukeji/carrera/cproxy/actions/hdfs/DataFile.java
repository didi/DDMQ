package com.xiaojukeji.carrera.cproxy.actions.hdfs;

import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DataFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFile.class);

    protected DataDir dir;

    protected String fileName;

    protected long lastModified = 0L;

    protected long byteSize;

    protected String prefix;

    protected long pid;

    protected ConcurrentLinkedQueue<UpstreamJob> upstreamJobs = new ConcurrentLinkedQueue<>();
    protected volatile boolean closed = false;
    protected FSDataOutputStream stream;
    protected DataFileManager dataFileManager;
    protected volatile int closeInvokedCount = 0;

    public DataFile(DataDir dir, String prefix, long pid, DataFileManager dataFileManager) {
        this.dir = dir;
        this.prefix = prefix;
        this.pid = pid;
        this.fileName = prefix + "-" + getNodeIDFromHostname() + "-" + pid + "." + System.currentTimeMillis();
        this.dataFileManager = dataFileManager;
    }

    public boolean close() {
        boolean flag = false;
        closeInvokedCount++;
        try {
            flush();
            stream.close();
            flag = true;
            closed = true;
        } catch (Exception e) {
            LOGGER.error("Close file error, dir:" + dir + ", file:" + fileName, e);
        }

        if (flag) {
            dir.deleteDataFile(pid);
            LOGGER.info("Close file success,file:" + getPath());
        } else {
            LOGGER.info("Close file failed, then add CloseFailStream,file:" + getPath());
            dataFileManager.addCloseFailedStream(this);
        }

        return flag;
    }

    public boolean directClose() {
        boolean flag = false;
        closeInvokedCount++;
        try {
            flush();
            stream.close();
            flag = true;
            closed = true;
        } catch (Exception e) {
            LOGGER.error("Close file error, dir:" + dir + ", file:" + fileName, e);
        }

        if (flag) {
            LOGGER.info("Close file success,file:" + getPath());
        } else {
            LOGGER.info("Close file failed, then add CloseFailStream,file:" + getPath());
            dataFileManager.addCloseFailedStream(this);
        }
        dir.deleteDataFile(pid);
        return flag;
    }

    public String getPath() {
        if (dir.getPath().endsWith("/")) {
            return dir.getPath() + fileName;
        } else {
            return dir.getPath() + "/" + fileName;
        }
    }

    public void write(byte[] b, int off, int len) throws Exception {
        this.lastModified = System.currentTimeMillis();
        dir.setLastModified(lastModified);
        stream.write(b, off, len);
        byteSize = byteSize + len;
    }

    public void flush() throws IOException {
        if (closed) {
            LOGGER.warn("DateFile has closed, no need to flush!");
            return;
        }
        if (stream instanceof HdfsDataOutputStream) {
            ((HdfsDataOutputStream) stream).hsync(EnumSet.of(HdfsDataOutputStream.SyncFlag.UPDATE_LENGTH));
        } else {
            stream.hsync();
        }
    }

    private String getNodeIDFromHostname() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String[] split1 = hostName.split("\\.")[0].split("-");
            String ret = split1[split1.length - 1].replaceAll("\\D+", "");
            if (StringUtils.isEmpty(ret)) {
                throw new Exception("empty nodeId");
            }
            return ret;
        } catch (Exception e) {
            Random random = new Random();
            String ret = "" + random.nextInt(100);
            LOGGER.error("exception when get nodeId {}, use randomID {}", e, ret);
            return ret;
        }
    }

    public void addUpstreamJob(UpstreamJob job) {
        upstreamJobs.add(job);
    }

    public FSDataOutputStream getOut() {
        return stream;
    }

    public void setOut(FSDataOutputStream out) {
        this.stream = out;
    }

    public String toString() {
        return "{file:" + fileName + ",lastModified:" + lastModified + "}";
    }

    public long getLastModified() {
        return lastModified;
    }

    public DataDir getDir() {
        return dir;
    }

    public String getPrefix() {
        return prefix;
    }

    public Long getPid() {
        return pid;
    }

    public FSDataOutputStream getStream() {
        return stream;
    }

    public void setStream(FSDataOutputStream stream) {
        this.stream = stream;
    }

    public long getByteSize() {
        return byteSize;
    }

    public String getFileName() {
        return fileName;
    }

    public ConcurrentLinkedQueue<UpstreamJob> getUpstreamJobs() {
        return upstreamJobs;
    }

    public int getCloseInvokedCount() {
        return closeInvokedCount;
    }

}
