package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;


public class ClusterMqserverRelation implements Serializable {
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
	   * 集群名称
	   * default = 
	   * length = 256
	   */
    private String clusterName;

    /**
	   * mqserver id
	   * default = 0
	   * length = 19
	   */
    private Long mqServerId;

    /**
	   * mqserver名称
	   * default = 
	   * length = 256
	   */
    private String mqServerName;

    /**
	   * 集群配置
	   * default = 
	   * length = 2048
	   */
    private String proxyConf;

    /**
	   * 集群和mqserver关联类型，0:pproxy 1:cproxy
	   * default = 0
	   * length = 3
	   */
    private Byte type;

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

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public Long getMqServerId() {
        return mqServerId;
    }

    public void setMqServerId(Long mqServerId) {
        this.mqServerId = mqServerId;
    }

    public String getMqServerName() {
        return mqServerName;
    }

    public void setMqServerName(String mqServerName) {
        this.mqServerName = mqServerName == null ? null : mqServerName.trim();
    }

    public String getProxyConf() {
        return proxyConf;
    }

    public void setProxyConf(String proxyConf) {
        this.proxyConf = proxyConf == null ? null : proxyConf.trim();
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
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
        ClusterMqserverRelation other = (ClusterMqserverRelation) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()))
            && (this.getMqServerId() == null ? other.getMqServerId() == null : this.getMqServerId().equals(other.getMqServerId()))
            && (this.getMqServerName() == null ? other.getMqServerName() == null : this.getMqServerName().equals(other.getMqServerName()))
            && (this.getProxyConf() == null ? other.getProxyConf() == null : this.getProxyConf().equals(other.getProxyConf()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
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
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        result = prime * result + ((getMqServerId() == null) ? 0 : getMqServerId().hashCode());
        result = prime * result + ((getMqServerName() == null) ? 0 : getMqServerName().hashCode());
        result = prime * result + ((getProxyConf() == null) ? 0 : getProxyConf().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
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
                ", clusterName=" + clusterName +
                ", mqServerId=" + mqServerId +
                ", mqServerName=" + mqServerName +
                ", proxyConf=" + proxyConf +
                ", type=" + type +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}