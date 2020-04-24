package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.mapper.MqServerMapper;
import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.dao.model.MqServerCriteria;
import com.didi.carrera.console.service.MqServerService;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.MqServerBo;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("didiMqServerServiceImpl")
public class MqServerServiceImpl implements MqServerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqServerServiceImpl.class);

    @Autowired
    private MqServerMapper mqServerMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> create(MqServerBo bo) throws Exception {
        if (findByName(bo.getName()) != null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster name existed");
        }

        if(bo.getType() == MqServerType.ROCKETMQ.getIndex()) {
            if(!bo.getAddr().contains(";") && bo.getAddr().split(":").length > 2) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "RMQ cluster address must be separated by semicolon");
            }
        } else {
            if(!bo.getAddr().contains(",") && bo.getAddr().split(":").length > 2) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Kafka cluster address must be separated by comma");
            }
        }

        MqServer mqServer = new MqServer();
        BeanUtils.copyProperties(bo, mqServer);
        mqServer.setIsDelete(IsDelete.NO.getIndex());
        mqServer.setCreateTime(new Date());

        mqServerMapper.insertSelective(mqServer);

        Map<String, Long> map = Maps.newHashMap();
        map.put("id", mqServer.getId());
        return ConsoleBaseResponse.success(map);
    }

    @Override
    public boolean updateAddrById(Long mqServerId, String addr) {
        MqServer mqServer = findById(mqServerId);
        if (mqServer == null) {
            LOGGER.error("mq_server not found id = {}", mqServerId);
            return false;
        }
        mqServer.setAddr(addr);
        mqServerMapper.updateByPrimaryKeySelective(mqServer);
        return true;
    }

    @Override
    public List<MqServer> findAll() {
        MqServerCriteria msc = new MqServerCriteria();
        msc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return mqServerMapper.selectByExample(msc);
    }

    @Override
    public MqServer findById(Long mqServerId) {
        MqServerCriteria msc = new MqServerCriteria();
        msc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andIdEqualTo(mqServerId);
        List<MqServer> list = mqServerMapper.selectByExample(msc);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public MqServer findByName(String mqServerName) {
        MqServerCriteria msc = new MqServerCriteria();
        msc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andNameEqualTo(mqServerName);
        List<MqServer> list = mqServerMapper.selectByExample(msc);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }
}