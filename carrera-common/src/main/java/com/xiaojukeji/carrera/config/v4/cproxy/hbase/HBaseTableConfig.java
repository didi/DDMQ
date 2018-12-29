package com.xiaojukeji.carrera.config.v4.cproxy.hbase;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class HBaseTableConfig implements ConfigurationValidator, Cloneable {

    private List<HBaseTableInfo> hbaseTables;

    private Map<Pattern, HBaseTableInfo> tableMapping;
    private Map<String, HBaseTableInfo> cacheResult = new ConcurrentHashMap<>();

    public HBaseTableInfo getTableInfo(String table) {
        if (!cacheResult.containsKey(table)) {
            if (tableMapping == null) {
                initMap();
            }

            for (Map.Entry<Pattern, HBaseTableInfo> entry : tableMapping.entrySet()) {
                if (entry.getKey().matcher(table).matches()) {
                    cacheResult.put(table, entry.getValue());
                    break;
                }
            }
        }
        return cacheResult.get(table);
    }

    private synchronized void initMap() {
        if (tableMapping != null) {
            return;
        }
        ConcurrentHashMap<Pattern, HBaseTableInfo> tmp = new ConcurrentHashMap<>();
        for (HBaseTableInfo info : hbaseTables) {
            tmp.put(Pattern.compile(info.getOriginalTable()), info);
        }
        tableMapping = tmp;
    }

    public List<HBaseTableInfo> getHbaseTables() {
        return hbaseTables;
    }

    public void setHbaseTables(List<HBaseTableInfo> hbaseTables) {
        this.hbaseTables = hbaseTables;
    }

    @Override
    public boolean validate() throws ConfigException {
        return true;
    }

    @Override
    public String toString() {
        return "HBaseTableConfig{" +
                ", hbaseTables=" + hbaseTables +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HBaseTableConfig that = (HBaseTableConfig) o;
        return Objects.equals(hbaseTables, that.hbaseTables);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hbaseTables);
    }

    @Override
    public HBaseTableConfig clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), HBaseTableConfig.class);
    }
}