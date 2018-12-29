package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotBlank;


public class IdcBo extends BaseBo {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotBlank(message = "IDC名称不能为空")
    private String name;

    private String remark;

    public IdcBo() {
    }

    public IdcBo(Long id, String name, String remark) {
        this.id = id;
        this.name = name;
        this.remark = remark;
    }

    @JSONField(serialize = false)
    public boolean isModify() {
        return id != null && id > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}