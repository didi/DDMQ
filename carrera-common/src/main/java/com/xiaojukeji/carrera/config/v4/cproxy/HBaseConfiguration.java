package com.xiaojukeji.carrera.config.v4.cproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.cproxy.hbase.HBaseTableConfig;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


public class HBaseConfiguration implements ConfigurationValidator, Cloneable {

    private String hbaseZK;
    private String hbasePort;
    private String hbaseBuffer;
    private String user;
    private String password;

    private HBaseTableConfig hBaseTableConfig;

    public String getHbaseZK() {
        return hbaseZK;
    }

    public void setHbaseZK(String hbaseZK) {
        this.hbaseZK = hbaseZK;
    }

    public String getHbasePort() {
        return hbasePort;
    }

    public void setHbasePort(String hbasePort) {
        this.hbasePort = hbasePort;
    }

    public String getHbaseBuffer() {
        return hbaseBuffer;
    }

    public void setHbaseBuffer(String hbaseBuffer) {
        this.hbaseBuffer = hbaseBuffer;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HBaseTableConfig gethBaseTableConfig() {
        return hBaseTableConfig;
    }

    public void sethBaseTableConfig(HBaseTableConfig hBaseTableConfig) {
        this.hBaseTableConfig = hBaseTableConfig;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(hbaseZK)) {
            throw new ConfigException("hbaseZK is empty");
        }

        if (StringUtils.isEmpty(hbasePort)) {
            throw new ConfigException("hbasePort is emtpy");
        }
        try {
            Integer.parseInt(hbasePort);
        } catch (NumberFormatException e) {
            throw new ConfigException("hbasePort is invalid");
        }

        if (StringUtils.isEmpty(hbaseBuffer)) {
            throw new ConfigException("hbaseBuffer is empty");
        }
        try {
            Integer.parseInt(hbaseBuffer);
        } catch (NumberFormatException e) {
            throw new ConfigException("hbaseBuffer is invalid");
        }

        if (StringUtils.isEmpty(user)) {
            throw new ConfigException("user is emtpy");
        }
        if (StringUtils.isEmpty(password)) {
            throw new ConfigException("password is emtpy");
        }

        if (hBaseTableConfig == null || !hBaseTableConfig.validate()) {
            throw new ConfigException("hBaseTableConfig is null or invalid");
        }

        return true;
    }

    @Override
    public String toString() {
        return "HBaseConfiguration{" +
                "hbaseZK='" + hbaseZK + '\'' +
                ", hbasePort='" + hbasePort + '\'' +
                ", hbaseBuffer='" + hbaseBuffer + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", hBaseTableConfig=" + hBaseTableConfig +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HBaseConfiguration that = (HBaseConfiguration) o;
        return Objects.equals(hbaseZK, that.hbaseZK) &&
                Objects.equals(hbasePort, that.hbasePort) &&
                Objects.equals(hbaseBuffer, that.hbaseBuffer) &&
                Objects.equals(user, that.user) &&
                Objects.equals(password, that.password) &&
                Objects.equals(hBaseTableConfig, that.hBaseTableConfig);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hbaseZK, hbasePort, hbaseBuffer, user, password, hBaseTableConfig);
    }

    @Override
    public HBaseConfiguration clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), HBaseConfiguration.class);
    }
}