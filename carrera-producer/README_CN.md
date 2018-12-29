[English](./README.md) | **中文**
## DDMQ Producer Proxy ##

Producer Proxy（简称 PProxy）是 DDMQ 的生产代理模块。PProxy 内置的一个 Thrift Server 负责将 SDK 发送的消息生产到具体的消息存储引擎中去（RcoketMQ or Kafka）。 PProxy 内部提供生产限流、批量生产、生产重试、自动聚合批量生产等功能。


### Thrift IDL ###

```c++
struct Result {
	1: i32 code
	2: string msg
	3: optional string key
}

struct Message {
    1: string topic;			                    // topic
    2: string key;				                    // uniq key to trace or get the partition
    3: string value;			                    // msg body
    4: i64 hashId;                                  // hashId for kafka partation.
    5: string tags;                                 // tags for RocketMQ message.
    6: i32 partitionId = -1;                        // partition id for kafka, -1: use hashId; -2: rand to get
    7: optional binary body;	                    // binary msg body
    8: optional string version;                     // sdk version
    9: optional map<string, string> properties      // properties
}

struct DelayResult {
	1: i32 code
	2: string msg
	3: string uniqDelayMsgId                    // unique identifier for a delay message, can be used to cancel or trace a delay message
}

struct DelayMessage {
    1: string topic;                                // topic
    2: optional string uniqDelayMsgId;		        // use to cancel or trace a message
    3: optional binary body;	                    // binary msg body
    4: string tags;                                 // tags for delay message
    5: i32 action;                                  // action, 1: add; 2: cancel
    6: optional i64 timestamp;			            // timestamp to trigger
    7: optional i32 dmsgtype;                       // dmsgtype, 2: delay; 3: loop delay
    8: optional i64 interval                        // interval of loop message, if not loop, just leave it
    9: optional i64 expire;                         // expire time
    10: optional i64 times;                         // loop times
    11: optional string uuid;                       // uuid for delay message
    13: optional string version;                    // sdk version
    14: optional map<string, string> properties     // properties
}

service ProducerService {
    // sync send normal message
    Result sendSync(1: Message message, 2: i64 timeout)

    // sync send batch normal message
    Result sendBatchSync(1: list<Message> messages)

    // async send normal message
    Result sendAsync(1: Message message)

    // sync send delay message
    DelayResult sendDelaySync(1: DelayMessage delayMessage, 2: i64 timeout)
}
```

### 部署 ###
* 修改 carrera.yaml 的配置

```yml
zookeeperAddr: 127.0.0.1:2181/carrera/v4/config # config zk cluster address here.
host: 127.0.0.1 # proxy ip (optional)
port: 9613 # thrift server port.
```

* 调用 console 的接口绑定 pproxy
* 执行 ```build.sh``` 脚本打包
* 执行 ```control.sh start``` 启动