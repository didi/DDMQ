package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.annotation.JSONField;


public class BaseOrderBo extends BaseBo {

    public interface LocalOrderCheck {
    }

    @JSONField(serialize = false)
    private String orderRemark;

    @NotNull(message = "工单ID不能为空", groups = LocalOrderCheck.class)
    @Min(value = 0, message = "工单ID不能小于0", groups = LocalOrderCheck.class)
    @JSONField(serialize = false)
    private Long orderId;

    @NotNull(message = "工单状态不能为空", groups = LocalOrderCheck.class)
    @JSONField(serialize = false)
    private Byte orderState;

    public String getOrderRemark() {
        return orderRemark;
    }

    public void setOrderRemark(String orderRemark) {
        this.orderRemark = orderRemark;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Byte getOrderState() {
        return orderState;
    }

    public void setOrderState(Byte orderState) {
        this.orderState = orderState;
    }

    @Override
    public String toString() {
        return "BaseOrderBo{" +
                "orderRemark='" + orderRemark + '\'' +
                ", orderId=" + orderId +
                ", orderState=" + orderState +
                "} " + super.toString();
    }

}