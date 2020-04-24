package com.didi.carrera.console.service.vo;


import com.didi.carrera.console.dao.model.custom.CustomTopicConf;
import com.didi.carrera.console.web.controller.bo.TopicConfBo;
import org.springframework.beans.BeanUtils;


public class TopicConfVo extends TopicConfBo {

    private Long confId;

    private Long mqServerId;
    private String mqServerName;


    private Integer msgRealMaxSize;
    private Integer msgRealAvgSize;

    private Integer weekMaxTps;
    private Integer weekAvgTps;

    private Integer consumerNum;

    private String monitorUrl;

    private String clusterDesc;

    public Long getConfId() {
        return confId;
    }

    public void setConfId(Long confId) {
        this.confId = confId;
    }

    public Integer getMsgRealMaxSize() {
        return msgRealMaxSize;
    }

    public void setMsgRealMaxSize(Integer msgRealMaxSize) {
        this.msgRealMaxSize = msgRealMaxSize;
    }

    public Integer getMsgRealAvgSize() {
        return msgRealAvgSize;
    }

    public void setMsgRealAvgSize(Integer msgRealAvgSize) {
        this.msgRealAvgSize = msgRealAvgSize;
    }

    public Integer getWeekMaxTps() {
        return weekMaxTps;
    }

    public void setWeekMaxTps(Integer weekMaxTps) {
        this.weekMaxTps = weekMaxTps;
    }

    public Integer getWeekAvgTps() {
        return weekAvgTps;
    }

    public void setWeekAvgTps(Integer weekAvgTps) {
        this.weekAvgTps = weekAvgTps;
    }

    public Integer getConsumerNum() {
        return consumerNum;
    }

    public void setConsumerNum(Integer consumerNum) {
        this.consumerNum = consumerNum;
    }

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public void setMonitorUrl(String monitorUrl) {
        this.monitorUrl = monitorUrl;
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
        this.mqServerName = mqServerName;
    }

    public String getClusterDesc() {
        return clusterDesc;
    }

    public void setClusterDesc(String clusterDesc) {
        this.clusterDesc = clusterDesc;
    }

    public static TopicConfVo buildTopicConfVo(CustomTopicConf conf) {
        TopicConfVo confVo = new TopicConfVo();
        BeanUtils.copyProperties(conf, confVo);
        confVo.setClientIdcMap(conf.getTopicConfClientIdc());
        confVo.setMsgRealAvgSize(0);
        confVo.setMsgRealMaxSize(0);
        confVo.setWeekAvgTps(0);
        confVo.setWeekMaxTps(0);

        return confVo;
    }

    @Override
    public String toString() {
        return "TopicConfVo{" +
                "confId=" + confId +
                ", mqServerId=" + mqServerId +
                ", mqServerName='" + mqServerName + '\'' +
                ", msgRealMaxSize=" + msgRealMaxSize +
                ", msgRealAvgSize=" + msgRealAvgSize +
                ", weekMaxTps=" + weekMaxTps +
                ", weekAvgTps=" + weekAvgTps +
                ", consumerNum=" + consumerNum +
                ", monitorUrl='" + monitorUrl + '\'' +
                ", clusterDesc='" + clusterDesc + '\'' +
                "} " + super.toString();
    }
}