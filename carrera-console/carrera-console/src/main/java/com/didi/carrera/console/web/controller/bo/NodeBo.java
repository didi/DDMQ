package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;


public class NodeBo extends BaseBo {

    @NotNull(message = "集群ID不能为空")
    private Long clusterid;

    @NotNull(message = "节点类型不能为空")
    @Range(min = 0, max = 9, message = "节点类型只能为0-9")
    private Byte nodetype;

    @NotBlank(message = "地址不能为空")
    private String host;

    private Long modelid = 1L;

    public Long getClusterid() {
        return clusterid;
    }

    public void setClusterid(Long clusterid) {
        this.clusterid = clusterid;
    }

    public Byte getNodetype() {
        return nodetype;
    }

    public void setNodetype(Byte nodetype) {
        this.nodetype = nodetype;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getModelid() {
        return modelid;
    }

    public void setModelid(Long modelid) {
        this.modelid = modelid;
    }

    @Override
    public String toString() {
        return "NodeBo{" +
                "clusterid=" + clusterid +
                ", nodetype=" + nodetype +
                ", host='" + host + '\'' +
                ", modelid=" + modelid +
                "} " + super.toString();
    }
}