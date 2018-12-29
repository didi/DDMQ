package com.xiaojukeji.carrera.cproxy.actions.hdfs;

import com.xiaojukeji.carrera.config.v4.cproxy.HdfsConfiguration;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;



public class HDFSWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HDFSWriter.class);

    private static final int MAX_TRY_TIMES = 5;

    private static final byte[] CTRL_BYTES = "\n".getBytes();

    private volatile boolean stop = false;

    private String group;

    private String topic;

    private HdfsConfiguration hdfsConfiguration;

    private DataFileManager dataFileManager;

    public HDFSWriter(String group, String topic, HdfsConfiguration hdfsConfiguration) {
        this.group = group;
        this.topic = topic;
        this.hdfsConfiguration = hdfsConfiguration;
        dataFileManager = new DataFileManager(group, this.hdfsConfiguration);
        dataFileManager.start();
    }

    public DataFileManager getDataFileManager() {
        return dataFileManager;
    }

    public void doWrite(UpstreamJob job, byte[] data) throws Exception {
        String topic = job.getTopic();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(data);
        byteStream.write(CTRL_BYTES);

        DataFile dataFile = dataFileManager.loadDataFile(group, topic, System.currentTimeMillis(), Thread.currentThread().getId());

        int tryTimes = 1;
        while (true) {
            try {
                dataFile.write(byteStream.toByteArray(), 0, byteStream.size());
                break;
            } catch (Exception e) {
                tryTimes++;
                LOGGER.error("WRITE_ERROR_EXCEPTION count " + tryTimes + ", write error! path:"
                        + dataFile.getPath() + ", rewrite:" + tryTimes, e);

                try {
                    Thread.sleep(1000L * (1 << (tryTimes >= MAX_TRY_TIMES ? MAX_TRY_TIMES : tryTimes)));
                } catch (InterruptedException e1) {
                    LOGGER.error("Retry sleep interrupted.", e1);
                }
                if (stop) {
                    LOGGER.error("It is stop time, break and return.");
                    return;
                }
            }
        }

        if (dataFile != null) {
            dataFile.addUpstreamJob(job);
        }
    }

    public boolean stop() {
        if (!stop) {
            stop = true;
            dataFileManager.flush(true);
            LOGGER.info("writer stopped.");
        }
        return true;
    }

    public String getTopic() {
        return topic;
    }
}
