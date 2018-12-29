package com.xiaojukeji.chronos.enums;


public enum RestoreState {
    SUCCESS("success"), FAIL("fail for exception when restore"), BEING_RESTORE("can not restore for last restore not finish"), ERROR("error");

    private String desc;

    RestoreState(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}