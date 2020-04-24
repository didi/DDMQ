package com.didi.carrera.console.web.controller.bo;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotBlank;


public class BaseBo {

    @NotBlank(message = "当前用户不能为空")
    @JSONField(serialize = false)
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "BaseBo{" +
                "user='" + user + '\'' +
                '}';
    }
}