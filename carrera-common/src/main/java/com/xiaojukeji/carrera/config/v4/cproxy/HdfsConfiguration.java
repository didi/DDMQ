package com.xiaojukeji.carrera.config.v4.cproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.lang3.StringUtils;


public class HdfsConfiguration implements ConfigurationValidator, Cloneable {
    /**
     * hdfs 写入目录
     */
    private String rootPath;
    /**
     * 文件存储路径的格式，支持${topic} ${yyyy} ${MM} ${dd} ${HH}
     */
    private String filePath;

    private String userName;

    private String password;
    /**
     * HDFS存储的文件类型，目前有Text和SequenceFile两种
     */
    private String fileType;
    /**
     * 将分库的数据库名映射为所需库名,如(\\w+)_(\\d+):{1};
     */
    private String reExpDatabase = "";
    /**
     * 将分表的表名映射为所需表名，如(\\w+)_(\\d+):{1};
     */
    private String reExpTable = "";

    private int maxFileCount = 128;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getReExpDatabase() {
        return reExpDatabase;
    }

    public void setReExpDatabase(String reExpDatabase) {
        this.reExpDatabase = reExpDatabase;
    }

    public String getReExpTable() {
        return reExpTable;
    }

    public void setReExpTable(String reExpTable) {
        this.reExpTable = reExpTable;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public void setMaxFileCount(int maxFileCount) {
        this.maxFileCount = maxFileCount;
    }

    @Override
    public String toString() {
        return "HdfsConfiguration{" +
                "rootPath='" + rootPath + '\'' +
                ", filePath='" + filePath + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", fileType='" + fileType + '\'' +
                ", reExpDatabase='" + reExpDatabase + '\'' +
                ", reExpTable='" + reExpTable + '\'' +
                ", maxFileCount=" + maxFileCount +
                '}';
    }

    @Override
    public HdfsConfiguration clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), HdfsConfiguration.class);
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isNotEmpty(rootPath)
                && StringUtils.isNotEmpty(filePath)
                && StringUtils.isNotEmpty(userName)
                && StringUtils.isNotEmpty(password)
                && StringUtils.isNotEmpty(fileType)) {
            return true;
        }
        return false;
    }
}