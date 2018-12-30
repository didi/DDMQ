package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.mapper.IdcMapper;
import com.didi.carrera.console.dao.model.Idc;
import com.didi.carrera.console.dao.model.IdcCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class IdcServiceImpl implements com.didi.carrera.console.service.IdcService {

    @Autowired
    private IdcMapper idcMapper;

    @Override
    public Map<Long, Idc> findMap() {
        IdcCriteria cc = new IdcCriteria();
        cc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        List<Idc> idcList = idcMapper.selectByExample(cc);

        return idcList.stream().collect(Collectors.toMap(Idc::getId, Function.identity()));
    }
}