package com.didi.carrera.console.config;

import com.didi.carrera.console.service.ZKV4ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartedLintener {
    @Autowired
    private ZKV4ConfigService zkv4ConfigService;


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartedListener() {
        log.info("应用启动成功，开始注册zk");
        try {
            zkv4ConfigService.initZkPath();
            zkv4ConfigService.initAllZk();
            log.info("zk注册成功");
        } catch (Exception e) {
            log.error("zk注册失败", e);
        }
    }
}
