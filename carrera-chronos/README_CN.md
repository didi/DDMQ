[English](./README.md) | **中文**
## DDMQ Chronos ##

Chronos 是 DDMQ 的延迟消息模块，提供了可靠的海量延迟消息存储。Chronos 使用 RocksDB 作为延迟消息的存储引擎。

### 特性 ###

* 消息类型丰富：延迟消息、循环延迟消息、事务消息（解决子系统之间或跨库事务问题）
* 高可用 & 高可靠：主备自动切换及多副本
* 接入简单：深度整合到 DDMQ，和实时使用同一套生产消费 SDK、同一套用户控制台
* 容量大：底层是硬盘存储

### 架构 ###

<center>
<img src="../image/chronosArch.png" width = "70%" />
</center>


开发者使用 DDMQ 的生产 SDK 将延迟消息生产到 PProxy 中，然后 PProxy 会将延迟消息写入到提前配置好的 inner topic 中。之后 Chronos 会消费 inner topic 的消息并存储到内置的 RocksDB 存储引擎中。Chronos 内置的时间轮服务会将到期的消息再次发送给 DDMQ 供业务方消费。

### 部署 ###
* 控制台上申请 inner topic 和 group
* 修改 chronos.yaml 的配置
* 执行 ```build.sh``` 脚本打包
* 执行 ```control.sh start``` 启动