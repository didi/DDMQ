package com.xiaojukeji.chronos.enums;


public enum BackupState {
    SUCCESS("success"), FAIL("fail for exception when backup"), BEING_BACKUP("can not backup for last backup not finish"), ERROR("error");

    private String desc;

    BackupState(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}