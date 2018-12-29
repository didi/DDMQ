package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.mapper.IdcMapper;
import com.didi.carrera.console.dao.model.Idc;
import com.didi.carrera.console.dao.model.IdcCriteria;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.TopicConfService;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.IdcBo;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class IdcServiceImpl implements com.didi.carrera.console.service.IdcService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdcServiceImpl.class);

    @Autowired
    private IdcMapper idcMapper;

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiTopicConfServiceImpl")
    private TopicConfService topicConfService;

    @Override
    public Idc findById(Long id) {
        return idcMapper.selectByPrimaryKey(id);
    }

    @Override
    public Idc findByName(String name) {
        IdcCriteria cc = new IdcCriteria();
        cc.createCriteria().andNameEqualTo(name).andIsDeleteEqualTo(IsDelete.NO.getIndex());
        List<Idc> list = idcMapper.selectByExample(cc);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public List<Idc> findAll() {
        IdcCriteria cc = new IdcCriteria();
        cc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return idcMapper.selectByExample(cc);
    }

    @Override
    public Map<Long, Idc> findMap() {
        Map<Long, Idc> map = Maps.newHashMap();
        findAll().forEach(idc -> map.put(idc.getId(), idc));

        return map;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<Long> create(IdcBo idcBo) throws Exception {
        Idc idc = new Idc();
        idc.setName(idcBo.getName());
        idc.setRemark(idcBo.getRemark());
        if (!idcBo.isModify()) {
            idc.setId(null);
            idc.setCreateTime(new Date());
            idc.setIsDelete(IsDelete.NO.getIndex());
            idcMapper.insertSelective(idc);
        } else {
            idc.setId(idcBo.getId());
            Idc oldIdc = findById(idcBo.getId());
            if (oldIdc == null) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "id<" + idcBo.getId() + ">not found");
            }
            int ret = idcMapper.updateByPrimaryKeySelective(idc);
            if (ret <= 0) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "id<" + idcBo.getId() + ">update failed");
            }
            if (ret > 0 && !Objects.equals(oldIdc.getName(), idc.getName())) {
                updateIdcName(idc.getId(), idc.getName());
            }
        }
        return ConsoleBaseResponse.success(idc.getId());
    }

    private void updateIdcName(Long idcId, String name) throws Exception {
        clusterService.updateIdcName(idcId, name);
        topicConfService.updateIdcName(idcId, name);
    }
}