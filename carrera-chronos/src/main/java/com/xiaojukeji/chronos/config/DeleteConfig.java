package com.xiaojukeji.chronos.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class DeleteConfig implements ConfigurationValidator {
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