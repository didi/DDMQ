package com.xiaojukeji.carrera.config.v4.cproxy.hbase;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


public class HBaseColumnInfo implements ConfigurationValidator, Cloneable {

    /**
     * hbase column name
     */
    private String hbaseColumn;
    /**
     * msg column name
     * */
    private String columnName;
    /**
     * 数据类型  int/short/long/double/float/boolean/other
     */
    private String columnType;
    /**
     * key column
     */
    private int key;

    public String getHbaseColumn() {
        return hbaseColumn;
    }

    public void setHbaseColumn(String hbaseColumn) {
        this.hbaseColumn = hbaseColumn;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(hbaseColumn)) {
            throw new ConfigException("hbaseColumn is empty");
        }
        if (StringUtils.isEmpty(columnName)) {
            throw new ConfigException("columnName is empty");
        }
        if (StringUtils.isEmpty(columnType)) {
            throw new ConfigException("columnType is empty");
        }
        if (key != 0 && key != 1) {
            throw new ConfigException("invalid key value");
        }
        return true;
    }

    @Override
    public String toString() {
        return "HBaseColumnInfo{" +
                "hbaseColumn='" + hbaseColumn + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", key=" + key +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HBaseColumnInfo that = (HBaseColumnInfo) o;
        return key == that.key &&
                Objects.equals(hbaseColumn, that.hbaseColumn) &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(columnType, that.columnType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hbaseColumn, columnName, columnType, key);
    }


    @Override
    public HBaseColumnInfo clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), HBaseColumnInfo.class);
    }
}