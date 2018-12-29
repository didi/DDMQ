**English** | [中文](./README_CN.md)
## DDMQ Producer Proxy ##

Producer Proxy(PProxy) is the producer module of DDMQ. PProxy is responsible for forwarding messages to message storage engine(RocketMQ or Kafka). PProxy provides features such as ratelimit, auto-batch and auto-retry.


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

### Deploy ###
* modify carrera.yaml

```yml
zookeeperAddr: 127.0.0.1:2181/carrera/v4/config # config zk cluster address here.
host: 127.0.0.1 # proxy ip (optional)
port: 9613 # thrift server port.
```

* call console api to bind pproxy
* run ```build.sh``` to build package
* start pproxy with ```control.sh start```