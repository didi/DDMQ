package com.didi.carrera.console.service.vo;

import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import com.google.common.collect.Maps;
import org.springframework.beans.BeanUtils;


public class SubscriptionOrderListVo extends ConsumeSubscriptionOrderBo {

    private Byte state;

    private Long idcId;

    private String idc;

    private String clusterDesc;

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public Long getIdcId() {
        return idcId;
    }

    public void setIdcId(Long idcId) {
        this.idcId = idcId;
    }

    public String getClusterDesc() {
        return clusterDesc;
    }

    public void setClusterDesc(String clusterDesc) {
        this.clusterDesc = clusterDesc;
    }

    public static SubscriptionOrderListVo buildSubscriptionListVo(ConsumeSubscription sub, Long idcId, String idc, String clusterDesc) {
        SubscriptionOrderListVo vo = new SubscriptionOrderListVo();
        BeanUtils.copyProperties(sub, vo);
        vo.setSubId(sub.getId());
        vo.setExtraParams(sub.getSubExtraParams());
        vo.setHttpHeaders(sub.getSubHttpHeaders());
        vo.setHttpQueryParams(sub.getSubHttpQueryParams());
        vo.setRetryIntervals(sub.getSubRetryIntervals());
        vo.setTransit(sub.getSubTransit());
        vo.setUrls(sub.getSubUrls());
        vo.setIdc(idc);
        vo.setIdcId(idcId);
        vo.setClusters(Maps.newHashMap());
        vo.getClusters().put(sub.getClusterName(), sub.getClusterId());
        vo.setClusterDesc(clusterDesc);

        return vo;
    }

    @Override
    public String toString() {
        return "SubscriptionOrderListVo{" +
                "state=" + state +
                ", idcId=" + idcId +
                ", idc='" + idc + '\'' +
                ", clusterDesc='" + clusterDesc + '\'' +
                "} " + super.toString();
    }
}