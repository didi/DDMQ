package com.didi.carrera.console.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.didi.carrera.console.web.controller.LoginInterceptor;
import com.xiaojukeji.carrera.biz.ZkService;
import com.xiaojukeji.carrera.biz.ZkServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.TransactionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

@Configuration
@EnableTransactionManagement
@Slf4j
public class SpringBeanConfig implements WebMvcConfigurer {

    @Value("${console.carrera.zookeeper}")
    private String zkHost;

    @Bean
    public ZkService getZkService() {
        try {
            return new ZkServiceImpl(zkHost, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @Primary
    public TransactionManager atomikosTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean
    @Primary
    public UserTransaction atomikosUserTransaction() {
        try {
            UserTransactionImp userTransactionImp = new UserTransactionImp();
            userTransactionImp.setTransactionTimeout(300);
            return userTransactionImp;
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @Primary
    public TransactionFactory jtaTransactionManager(TransactionManager atomikosTransactionManager, UserTransaction atomikosUserTransaction) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager);
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction);
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        jtaTransactionManager.setDefaultTimeout(300);
        return jtaTransactionManager;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
    }
}
