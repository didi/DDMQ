package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;


public class Node implements Serializable {
    /**
	   * 自增id
	   * default = null
	   * length = 20
	   */
    private Long id;

    /**
	   * 集群id
	   * default = 0
	   * length = 19
	   */
    private Long clusterId;

    /**
	   * 机型id
	   * default = 0
	   * length = 19
	   */
    private Long modelId;

    /**
	   * broker主节点
	   * default = 0
	   * length = 19
	   */
    private Long masterId;

    /**
	   * 机器地址 ip:port
	   * default = 
	   * length = 128
	   */
    private String host;

    /**
	   * node类型, kafkabroker, rmqbroker xxxx
	   * default = 0
	   * length = 3
	   */
    private Byte nodeType;

    /**
	   * 是否删除 0:未删除 1:删除
	   * default = 0
	   * length = 3
	   */
    private Byte isDelete;

    /**
	   * 创建时间
	   * default = 1970-01-01 00:00:00
	   * length = 19
	   */
    private Date createTime;

    /**
	   * 修改时间
	   * default = CURRENT_TIMESTAMP
	   * length = 19
	   */
    private Date modifyTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Byte getNodeType() {
        return nodeType;
    }

    public void setNodeType(Byte nodeType) {
        this.nodeType = nodeType;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Node other = (Node) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getMasterId() == null ? other.getMasterId() == null : this.getMasterId().equals(other.getMasterId()))
            && (this.getHost() == null ? other.getHost() == null : this.getHost().equals(other.getHost()))
            && (this.getNodeType() == null ? other.getNodeType() == null : this.getNodeType().equals(other.getNodeType()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getMasterId() == null) ? 0 : getMasterId().hashCode());
        result = prime * result + ((getHost() == null) ? 0 : getHost().hashCode());
        result = prime * result + ((getNodeType() == null) ? 0 : getNodeType().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        String sb = getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", clusterId=" + clusterId +
                ", modelId=" + modelId +
                ", masterId=" + masterId +
                ", host=" + host +
                ", nodeType=" + nodeType +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}