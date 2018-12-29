package com.didi.carrera.console.service.vo;

import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupBo;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;


public class ConsumeGroupVo extends ConsumeGroupBo {

    private Integer subscriptionNum;

    private Integer subscriptionEnableNum;

    public Integer getSubscriptionNum() {
        return subscriptionNum;
    }

    public void setSubscriptionNum(Integer subscriptionNum) {
        this.subscriptionNum = subscriptionNum;
    }

    public Integer getSubscriptionEnableNum() {
        return subscriptionEnableNum;
    }

    public void setSubscriptionEnableNum(Integer subscriptionEnableNum) {
        this.subscriptionEnableNum = subscriptionEnableNum;
    }

    private static Map<String, List<Long>> getConsumeModeStringMapper(Map<Long, List<Long>> mapper) {
        if (MapUtils.isEmpty(mapper)) {
            return null;
        }
        Map<String, List<Long>> stringMapper = Maps.newHashMap();
        for (Map.Entry<Long, List<Long>> entry : mapper.entrySet()) {
            stringMapper.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return stringMapper;
    }

    public static ConsumeGroupVo buildConsumeGroupVo(ConsumeGroup consumeGroup) {
        ConsumeGroupVo vo = new ConsumeGroupVo();
        BeanUtils.copyProperties(consumeGroup, vo);
        vo.setGroupId(consumeGroup.getId());
        vo.setAlarmGroup(consumeGroup.getGroupAlarmGroup());
        vo.setExtraParams(consumeGroup.getGroupExtraParams());
        vo.setConsumeModeMapper(getConsumeModeStringMapper(consumeGroup.getGroupConsumeModeMapper()));
        vo.setSubscriptionNum(0);
        vo.setSubscriptionEnableNum(0);
        return vo;
    }

    @Override
    public String toString() {
        return "ConsumeGroupVo{" +
                "subscriptionNum=" + subscriptionNum +
                ", subscriptionEnableNum=" + subscriptionEnableNum +
                "} " + super.toString();
    }
}