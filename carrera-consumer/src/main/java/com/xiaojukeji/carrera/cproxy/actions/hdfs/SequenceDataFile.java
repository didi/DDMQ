package com.xiaojukeji.carrera.cproxy.actions.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;



public class SequenceDataFile extends DataFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceDataFile.class);

    private static IntWritable writekey;

    private SequenceFile.Writer writer;

    private Configuration config;

    private Path sequenceFilePath;

    private Text writeValue;

    private FileSystem fileSystem;

    public SequenceDataFile(FileSystem fileSystem, DataDir dir, String prefix, Long pid, DataFileManager dataFileManager) {
        super(dir, prefix, pid, dataFileManager);
        this.fileSystem = fileSystem;
        writekey = new IntWritable();
        writeValue = new Text();
        config = new Configuration();
        sequenceFilePath = new Path(this.getPath());
        this.init();
    }

    private void init() {
        try {
            this.stream = fileSystem.create(sequenceFilePath, false, 4096);
            this.writer = SequenceFile.createWriter(config,
                    SequenceFile.Writer.stream(this.stream),
                    SequenceFile.Writer.keyClass(IntWritable.class),
                    SequenceFile.Writer.valueClass(Text.class),
                    SequenceFile.Writer.compression(SequenceFile.CompressionType.BLOCK));
        } catch (IOException e) {
            String error = "init()@SequenceDataFile; Create SequenceFile.Writer failed. file:" + this.getPath();
            LOGGER.error(error, e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.lastModified = System.currentTimeMillis();
        dir.setLastModified(lastModified);
        writeValue.set(b);
        writer.append(writekey, writeValue);
        writekey.set(writekey.get() + 1);
        this.byteSize = byteSize + len;
    }

    @Override
    public boolean close() {
        boolean flag = false;
        try {
            writer.close();
            stream.close();
            flag = true;
            closed = true;
        } catch (IOException e) {
            String error = "close()@SequenceDataFile; Close file failed, then add CloseFailStream,file:"
                    + this.getPath();
            LOGGER.error(error, e);
        }

        if (flag) {
            this.dir.deleteDataFile(this.getPid());
            LOGGER.info("close()@SequenceDataFile; Close file success,file:" + getPath());
        } else {
            LOGGER.info("close()@SequenceDataFile; Close file failed, file:" + getPath());
        }
        return flag;
    }

    @Override
    public boolean directClose() {
        boolean flag = false;
        closeInvokedCount++;
        try {
            flush();
            writer.close();
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

    @Override
    public void flush() throws IOException {
        if (closed) {
            LOGGER.warn("SequenceFile has closed, no need to flush!");
            return;
        }
        try {
            writer.sync();
            if (stream instanceof HdfsDataOutputStream) {
                ((HdfsDataOutputStream) stream).hsync(EnumSet.of(HdfsDataOutputStream.SyncFlag.UPDATE_LENGTH));
            } else {
                stream.hsync();
            }
        } catch (IOException e) {
            if (closed) {
                LOGGER.info("SequenceFile already close, file:" + getPath());
            } else {
                throw e;
            }
        }
    }
}
