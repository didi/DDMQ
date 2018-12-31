[English](./README.md) | **中文**
## DDMQ Docker ##

DDMQ 涉及到的模块较多，集群部署步骤比较复杂。为了方便开发者体验 DDMQ 的各项功能，我们制作了 docker 镜像，将涉及到的模块都打包到一个镜像中，方便用户运行一个单机版的 DDMQ。

### 容器内容 ###

* 一个单机版的 Zookeeper（版本 3.4.10）
* Web 应用服务器 tomcat 9 和 console 服务
* rocketmq 存储引擎（一个 namesvr、一个 master broker）
* 单 consumer-proxy 实例
* 单 producer-proxy 实例
* 单 chronos 实例（延迟模块）

备注：DDMQ 容器依赖一个 mysql 5.7 的容器

### 使用方式 ###
* 安装 Docker
* 安装 MySQL 客户端（建议使用 5.7.x版本）
* 运行 ```build.sh``` 构建打包
* 运行 ```play-ddmq.sh``` （首次执行将下载 centos7,mysql,tomcat,zookeeper 等依赖，大约20分钟，具体情况视网络情况）
* 打开 DDMQ 用户控制台  

    > <http://localhost:8080/carrera/index.html>


<center>
<img src="../image/localDDMQ.png" width = "70%" />
</center>

*备注：producer-proxy port: 9613、consumer-proxy port: 9713*
