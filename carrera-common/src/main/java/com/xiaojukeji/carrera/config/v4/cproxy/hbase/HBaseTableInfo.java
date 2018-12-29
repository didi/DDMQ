package com.xiaojukeji.carrera.config.v4.cproxy.hbase;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class HBaseTableInfo implements ConfigurationValidator, Cloneable {

    private String originalTable;
    private String hbaseTable;
    private String columnFamily;
    private String nameSpace;

    private List<HBaseColumnInfo> columns;
    private volatile Map<String, HBaseColumnInfo> name2Info;
    private HBaseColumnInfo keyCol;

    public Map<String, HBaseColumnInfo> getColumnName2ColumnInfoMap() {
        if (name2Info != null) {
            return name2Info;
        }
        if (columns == null) {
            name2Info = new HashMap<>();
            return name2Info;
        }
        HashMap<String, HBaseColumnInfo> name2InfoTmp = new HashMap<>();
        for (HBaseColumnInfo column : columns) {
            if (column.getKey() == 1) {
                keyCol = column;
            }
            name2InfoTmp.put(column.getColumnName(), column);
        }
        name2Info = name2InfoTmp;
        return name2Info;
    }

    public String getTableName() {
        return  nameSpace + ":" + hbaseTable;
    }

    public String getOriginalTable() {
        return originalTable;
    }

    public void setOriginalTable(String originalTable) {
        this.originalTable = originalTable;
    }

    public String getHbaseTable() {
        return hbaseTable;
    }

    public void setHbaseTable(String hbaseTable) {
        this.hbaseTable = hbaseTable;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public List<HBaseColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<HBaseColumnInfo> columns) {
        this.columns = columns;
    }

    public Map<String, HBaseColumnInfo> getName2Info() {
        return name2Info;
    }

    public void setName2Info(Map<String, HBaseColumnInfo> name2Info) {
        this.name2Info = name2Info;
    }

    public HBaseColumnInfo getKeyCol() {
        return keyCol;
    }

    public void setKeyCol(HBaseColumnInfo keyCol) {
        this.keyCol = keyCol;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(hbaseTable)) {
            throw new ConfigException("hbaseTable is empty");
        }
        if (StringUtils.isEmpty(columnFamily)) {
            throw new ConfigException("columnFamily is empty");
        }
        if (StringUtils.isEmpty(nameSpace)) {
            throw new ConfigException("nameSpace is empty");
        }
        if (CollectionUtils.isEmpty(columns)) {
            throw new ConfigException("columns is empty");
        }
        for (HBaseColumnInfo column : columns) {
            if (!column.validate()) {
                throw new ConfigException("column is invalid");
            }
        }
        if (keyCol != null && !keyCol.validate()) {
            throw new ConfigException("keyCol is invalid");
        }
        return true;
    }

    @Override
    public String toString() {
        return "HBaseTableInfo{" +
                "originalTable='" + originalTable + '\'' +
                ", hbaseTable='" + hbaseTable + '\'' +
                ", columnFamily='" + columnFamily + '\'' +
                ", nameSpace='" + nameSpace + '\'' +
                ", columns=" + columns +
                ", name2Info=" + name2Info +
                ", keyCol=" + keyCol +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HBaseTableInfo that = (HBaseTableInfo) o;
        return Objects.equals(originalTable, that.originalTable) &&
                Objects.equals(hbaseTable, that.hbaseTable) &&
                Objects.equals(columnFamily, that.columnFamily) &&
                Objects.equals(nameSpace, that.nameSpace) &&
                Objects.equals(columns, that.columns) &&
                Objects.equals(keyCol, that.keyCol);
    }

    @Override
    public int hashCode() {

        return Objects.hash(originalTable, hbaseTable, columnFamily, nameSpace, columns, keyCol);
    }
}