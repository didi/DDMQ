[English](./README.md) | **中文**
## DDMQ Console ##
DDMQ Console 是用户控制台的 Web 服务，使用 Spring 框架开发。负责 topic/group/subscription 等资源的配置和维护，负责推送和更新配置到 Zookeeper。提供了 CProxy 和 PProxy 等模块的配置信息。


### 部署 ###
* 前端项目

  > 参考： [console-frontend](carrera-console-fe/README.md)

* console-backend
  >  初始化 MySQL 表结构，使用 ddl/ddmq_init.sql
  
  >  执行 ```build.sh && control.sh start	```

### 扩容 & 缩容 ###

* 增加 PProxy
  > 调用接口 http://console_address:8080/carrera/api/odin/internal/v4/addPProxy?cluster=ddmq&host=pproxy_addr	
  
* 增加 CProxy
  > 调用接口 http://console_address:8080/carrera/api/odin/internal/v4/addCProxy?cluster=ddmq&host=cproxy_addr

* 扩容 broker & namesvr
  > 扩容 broker 自动生效
  
  > 扩容 namesvr 目前需要手动修改 mq_server 和 cluster_mqserver_relation表