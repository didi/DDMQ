package com.xiaojukeji.carrera.cproxy.actions.hbase;

import org.apache.hadoop.hbase.client.Mutation;


public class HbaseCommand {

    private String tableName;

    private Mutation mutation;

    public HbaseCommand(String tableName, Mutation mutation) {
        this.tableName = tableName;
        this.mutation = mutation;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Mutation getMutation() {
        return mutation;
    }

    public void setMutation(Mutation mutation) {
        this.mutation = mutation;
    }

}
