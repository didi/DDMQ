package com.didi.carrera.console.web.controller.bo;

import com.didi.carrera.console.dao.model.ConsumeSubscription;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;


public class ConsumeSubscriptionOrderBo extends ConsumeSubscriptionBaseBo {

    @NotEmpty(message = "集群不能为空")
    private Map<String, Long> clusters;

    public Map<String, Long> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Long> clusters) {
        this.clusters = clusters;
    }

    @Override
    public ConsumeSubscription buildConsumeSubscription() {
        return super.buildConsumeSubscription();
    }

    @Override
    public String toString() {
        return "ConsumeSubscriptionOrderBo{" +
                "clusters=" + clusters +
                "} " + super.toString();
    }
}