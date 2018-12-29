package com.didi.carrera.console.dao.model;

import java.io.Serializable;
import java.util.Date;


public class Cluster implements Serializable {
    /**
	   * 自增id
	   * default = null
	   * length = 20
	   */
    private Long id;

    /**
	   * 集群名称
	   * default = 
	   * length = 256
	   */
    private String name;

    /**
	   * 描述信息
	   * default = 
	   * length = 1024
	   */
    private String description;

    /**
	   * idc table primarykey
	   * default = 0
	   * length = 19
	   */
    private Long idcId;

    /**
	   * 集群所属机房
	   * default = gz01
	   * length = 256
	   */
    private String idc;

    /**
	   * 备注
	   * default = 
	   * length = 1024
	   */
    private String remark;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Long getIdcId() {
        return idcId;
    }

    public void setIdcId(Long idcId) {
        this.idcId = idcId;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc == null ? null : idc.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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
        Cluster other = (Cluster) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getIdcId() == null ? other.getIdcId() == null : this.getIdcId().equals(other.getIdcId()))
            && (this.getIdc() == null ? other.getIdc() == null : this.getIdc().equals(other.getIdc()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getModifyTime() == null ? other.getModifyTime() == null : this.getModifyTime().equals(other.getModifyTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getIdcId() == null) ? 0 : getIdcId().hashCode());
        result = prime * result + ((getIdc() == null) ? 0 : getIdc().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
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
                ", name=" + name +
                ", description=" + description +
                ", idcId=" + idcId +
                ", idc=" + idc +
                ", remark=" + remark +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
        return sb;
    }
}