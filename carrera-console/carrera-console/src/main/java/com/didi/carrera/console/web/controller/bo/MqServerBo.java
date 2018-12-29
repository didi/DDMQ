package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;


public class MqServerBo {

    @NotBlank(message = "集群名称不能为空")
    private String name;

    @NotNull(message = "集群类型不能为空")
    @Range(min = 0, max = 2, message = "集群类型只能为 0:rocketmq 1:kafka 2:virtual kafka")
    private Byte type;

    @NotBlank(message = "集群地址不能为空")
    private String addr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "MqServerBo{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", addr='" + addr + '\'' +
                '}';
    }
}