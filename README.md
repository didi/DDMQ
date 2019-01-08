**English** | [中文](./README_CN.md)
<p align="center">
<img align="left" width="175" src="image/logo.png">
<br>
</p>
<br>

**DDMQ** is a distributed messaging product built by DiDi Infrastructure Team based on [Apache RocketMQ](https://rocketmq.apache.org/). As a distributed messaging middleware, DDMQ provides low latency, high throughput and high available messaging service to many important large-scale distributed systems inside [DiDi](https://www.didiglobal.com/). DDMQ provides realtime messaging, delay-time messaging and transactional messaging to satisfy different scenarios. Through an easy-to-use Web Console and simple SDK Client, developers can experience producing and consuming messages in the most simple and stable way.


----------

### Features

* Messaging model: support both P2P and Pub/Sub messaging model

* Massive message storage, support both RocketMQ and Kafka as storage engine

* Low latency & High throughput

* Delay message, use RocksDB as storage engine

* Transactional message: provide transaction similar to X/Open XA

* Multiple language client SDK: provide client SDK in PHP, Java, Go, C/C++, Python

* Message transition and filter with user-defined Groovy script

* An easy-to-use Web Console


----------

### Architecture Overview

<center>
<img src="image/arch.png" width = "80%" />
</center>



----------

### Modules

* carrera-common: common code for other modules, such as encapsulate zk operations.

* carrera-producer: message producer proxy with built-in Thrift Server, responsible for forwarding message from SDK client to broker.

* carrera-consumer: message consumer proxy with built-in Thrift Server, provide SDK PULL and HTTP PUSH for message consumption.

* carrera-chronos: delay message module, use RocksDB as storage engine.

* carrera-sdk: producer and consumer SDK, support Java/C/C++/Go/PHP/Python. 

* rocketmq: based on RocketMQ (Ver 4.2.0)，add new features such as broker auto fail-over.

* carrera-console: a Spring-based User Web Console.

* carrera-monitor: consumer lag monitor and DDMQ cluster monitor. 

* carrera-docker: provide a DDMQ docker image that runs in standalone mode.

----------

### Quick Start
We provide a standalone version of DDMQ as Docker image, read [this](carrera-docker/README.md) for more information.


----------


### Deployment
#### Dependencies
* 64bit OS, Linux/Unix/Mac
* 64bit JDK 1.8+
* Maven 3.2.x
* MySQL 5.7.x
* Tomcat 7/8/9
* Zookeeper 3.4.x


#### Deployment Procedures

*  Deploy MySQL & Zookeeper
 	>  Install MySQL 5.7:  <https://dev.mysql.com/doc/mysql-installation-excerpt/5.7/en/>

 	>  Install Zookeeper 3.4.x:  <https://zookeeper.apache.org/doc/r3.1.2/zookeeperStarted.html>
 	
 	
*  Deploy RocketMQ

    >Guide: [RocketMQ](rocketmq/README.md)
  
*  Init MySQL Tables

    >Guide: [MySQL](carrera-console/README.md)
  
*  Deploy Console

    >Guide: [Console](carrera-console/README.md)

*  Init Zookeeper Node and Data
	
	> call console api： 
	
	> * curl http://console_addr:8080/carrera/api/odin/internal/v4/initZkPath
	> * curl http://console_addr:8080/carrera/api/odin/internal/v4/initAllZk


*  Deploy Producer Proxy

    >Guide: [Deploy PProxy](carrera-producer/README.md)

*  Deploy Consumer Proxy

    >Guide: [Deploy CProxy](carrera-consumer/README.md)

*  Deploy Chronos

    >Guide: [Deploy Chronos](carrera-chronos/README.md)



----------  

### Usage

  * read [DDMQ Console Manual](carrera-console/USAGE.md) for more information.


----------

### Contributing
Welcome to contribute by creating issues or sending pull requests. See [Contributing Guide](CONTRIBUTING.md) for guidelines.

----------

### Community
<img src="image/wechatGroup.png" alt="Mand Mobile Community" width="200"/>
<img src="image/dingGroup.jpg" alt="Mand Mobile Community" width="200"/>


----------

### License

DDMQ is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file.
