## DDMQ PROD ##

1、创建自定义网络：

* docker network create --subnet=172.18.0.0/16 ddmq-net
* docker network ls

2、将carrera-prod/mysql/ddmq.sql导入宿主机MySQL的carrera_open_source库

* 这里宿主机的MySQL的root账号密码是wawa521
* 需要修改账号和密码，需要修改carrera-console/carrera-console/setting/carrera-console-dev.properties文件，并且重新编译

3、修改启动脚本

* 这里宿主机IP是192.168.11.24，请将carrera-prod/play.sh里面的192.168.11.24改为实际宿主机的IP
* mkdir -p /usr/local/var/rocketmq
* chmod 777 /usr/local/var/rocketmq
* 在docker中设置共享目录 /usr/local/var/rocketmq

4、启动集群

* sh carrera-prod/play.sh

