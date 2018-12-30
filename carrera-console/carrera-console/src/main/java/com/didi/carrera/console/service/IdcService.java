package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.Idc;

import java.util.Map;


public interface IdcService {

    Map<Long, Idc> findMap();

}