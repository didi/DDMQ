package com.didi.carrera.console.service.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


public class PageModel<T> {

    private Integer curPage;
    private Integer pageSize;
    private Integer totalSize;
    private Integer totalPage;

    private List<T> list;

    public PageModel(Integer curPageNum, Integer pageSize, Integer totalSize) {
        this.curPage = curPageNum;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        this.totalPage =  (this.totalSize - 1) / this.pageSize + 1;

        if (curPageNum < 1) {
            this.curPage = 1;
        } else if (curPageNum > this.totalPage) {
            this.curPage = this.totalPage;
        } else {
            this.curPage = curPageNum;
        }
    }

    public Integer getCurPage() {
        return curPage;
    }

    public void setCurPage(Integer curPage) {
        this.curPage = curPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    @JSONField(serialize = false)
    public Integer getPageIndex() {
        return (curPage-1) * pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void addListItem(T t) {
        if(CollectionUtils.isEmpty(list)) {
            list = Lists.newArrayList();
        }

        list.add(t);
    }

    @Override
    public String toString() {
        return "PageModel{" +
                "curPage=" + curPage +
                ", pageSize=" + pageSize +
                ", totalSize=" + totalSize +
                ", totalPage=" + totalPage +
                ", list=" + list +
                '}';
    }
}