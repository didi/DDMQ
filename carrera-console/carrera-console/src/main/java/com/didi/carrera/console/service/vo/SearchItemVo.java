package com.didi.carrera.console.service.vo;


public class SearchItemVo {
    private Long id;
    private String desc;

    public SearchItemVo(Long id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SearchItemVo{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                '}';
    }
}