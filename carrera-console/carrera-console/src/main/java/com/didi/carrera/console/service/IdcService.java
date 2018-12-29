package com.didi.carrera.console.service;

import java.util.List;
import java.util.Map;

import com.didi.carrera.console.dao.model.Idc;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.IdcBo;


public interface IdcService {
    Idc findById(Long id);

    Idc findByName(String name);

    List<Idc> findAll();

    ConsoleBaseResponse<Long> create(IdcBo idcBo) throws Exception;

    Map<Long, Idc> findMap();

}