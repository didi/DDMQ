package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.MqServerBo;

import java.util.List;


public interface MqServerService {

    List<MqServer> findAll();

    MqServer findById(Long mqServerId);

    MqServer findByName(String mqServerName);

    ConsoleBaseResponse<?> create(MqServerBo bo) throws Exception;

    boolean updateAddrById(Long mqServerId, String addr);
}