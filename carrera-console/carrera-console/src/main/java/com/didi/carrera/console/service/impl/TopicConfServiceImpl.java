package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.mapper.TopicConfMapper;
import com.didi.carrera.console.dao.mapper.custom.TopicConfCustomMapper;
import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.dao.model.TopicConfCriteria;
import com.didi.carrera.console.dao.model.custom.CustomTopicConf;
import com.didi.carrera.console.service.MqServerService;
import com.didi.carrera.console.service.RmqAdminService;
import com.didi.carrera.console.service.TopicConfService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service("didiTopicConfServiceImpl")
public class TopicConfServiceImpl implements TopicConfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicConfServiceImpl.class);

    @Autowired
    private TopicConfMapper topicConfMapper;

    @Autowired
    private TopicConfCustomMapper topicConfCustomMapper;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Resource(name = "didiRmqAdminServiceImpl")
    private RmqAdminService rmqAdminService;

    @Override
    public List<CustomTopicConf> findByTopicId(List<Long> topicIds) {
        return topicConfCustomMapper.selectByTopicId(topicIds);
    }

    @Override
    public List<TopicConf> findByTopicClusterIds(List<Long> topicIds, List<Long> clusterIds) {
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andTopicIdIn(topicIds).andClusterIdIn(clusterIds);
        return topicConfMapper.selectByExample(tcc);
    }

    @Override
    public List<TopicConf> findByTopicId(Long topicId) {
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andTopicIdEqualTo(topicId);
        return topicConfMapper.selectByExample(tcc);
    }

    @Override
    public List<TopicConf> findByClusterId(Long clusterId) {
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId);
        return topicConfMapper.selectByExample(tcc);
    }

    @Override
    public List<Long> findTopicByClusterIdWithDeleted(Long clusterId) {
        List<Long> ret = Lists.newArrayList();
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andClusterIdEqualTo(clusterId);
        List<TopicConf> list = topicConfMapper.selectByExample(tcc);
        if(CollectionUtils.isEmpty(list)) {
            return ret;
        }
        ret.addAll(list.stream().map(TopicConf::getTopicId).collect(Collectors.toSet()));
        return ret;
    }

    @Override
    public List<TopicConf> findAll() {
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return topicConfMapper.selectByExample(tcc);
    }


    @Override
    public List<TopicConf> findByTopicClusterId(Long topicId, Long clusterId) {
        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andTopicIdEqualTo(topicId).andClusterIdEqualTo(clusterId);
        return topicConfMapper.selectByExample(tcc);
    }

    @Override
    public boolean deleteByIds(List<Long> configIds) {
        TopicConf conf = new TopicConf();
        conf.setIsDelete(IsDelete.YES.getIndex());

        TopicConfCriteria tcc = new TopicConfCriteria();
        tcc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andIdIn(configIds);
        topicConfMapper.updateByExampleSelective(conf, tcc);

        return true;
    }

    @Override
    public boolean updateByPrimaryKey(TopicConf conf) throws Exception {
        return topicConfMapper.updateByPrimaryKeySelective(conf) > 0;
    }

    @Override
    public boolean insert(TopicConf conf) throws Exception {
        conf.setId(null);

        if (topicConfMapper.insertSelective(conf) > 0) {
            if (mqServerService.findById(conf.getMqServerId()).getType() == MqServerType.ROCKETMQ.getIndex()) {
                LOGGER.info("create rmq broker topic, conf:{}", conf);
                rmqAdminService.createTopic(conf.getClusterId(), conf.getTopicName());
            } else {
                LOGGER.info("conf mqserver is Kafka, skip create broker topic", conf);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean insert(List<TopicConf> confList) throws Exception {
        if (CollectionUtils.isEmpty(confList)) {
            return true;
        }

        final boolean[] ret = {false};
        for (TopicConf conf : confList) {
            ret[0] = insert(conf);
        }

        return ret[0];
    }
}