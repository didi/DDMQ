**English** | [中文](./README_CN.md)
## DDMQ Console ##
DDMQ Console is a Web service developed with Spring and Vue. Console is used to manage Topic/Group/Subscription, responsible for pushing configurations to zookeeper. 



### Deploy ###
* Front-End

  > Guide: [console-frontend](carrera-console-fe/README.md)

* console-backend
  >  Init MySQL table (use ddl/ddmq_init.sql)
  
  >  run ```build.sh && control.sh start	```
* add pproxy & cproxy
  > http://console_address:8080/carrera/api/odin/internal/v4/addPProxy?cluster=ddmq&host=pproxy_addr
  
  > http://console_address:8080/carrera/api/odin/internal/v4/addCProxy?cluster=ddmq&host=cproxy_addr


* add broker & namesvr
  > need to manually update mq_server and cluster_mqserver_relation table.
