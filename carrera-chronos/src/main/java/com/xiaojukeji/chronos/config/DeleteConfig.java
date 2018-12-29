package com.xiaojukeji.chronos.config;


public class DeleteConfig implements ConfigValidator {
    private int deleteWhen;
    private int saveHours;

    public int getDeleteWhen() {
        return deleteWhen;
    }

    public void setDeleteWhen(int deleteWhen) {
        this.deleteWhen = deleteWhen;
    }

    public int getSaveHours() {
        return saveHours;
    }

    public void setSaveHours(int saveHours) {
        this.saveHours = saveHours;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String toString() {
        return "DeleteConfig{" +
                "deleteWhen=" + deleteWhen +
                ", saveHours=" + saveHours +
                '}';
    }
}