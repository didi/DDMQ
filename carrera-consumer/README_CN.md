[English](./README.md) | **中文**
## DDMQ Consumer Proxy ##

Consumer Proxy（简称 CProxy）是 DDMQ 的消费代理模块。 DDMQ 的主要功能（如：消息体转换）都实现在 CProxy 中，提供了轻量级的消息流计算功能。CProxy 支持 Thrift 和 HTTP 两种协议，也可以将消息直接写入 Redis/Hbase/HDFS 等外部存储系统。


### Thrift IDL ###
```c++
struct Message {
    1: string key;
    2: binary value;
    3: string tag;
    4: i64 offset;
    5: optional map<string, string> properties;
}

struct Context {
    1: string groupId;
    2: string topic;
    3: string qid;
}

struct ConsumeResult {
    1: Context context;
    3: list<i64> successOffsets;
    4: list<i64> failOffsets;
    10: optional ConsumeResult nextResult;
}

struct PullRequest {
    1: required string groupId;
    2: optional string topic;
    10: optional i32 maxBatchSize;
    11: optional i32 maxLingerTime;
    50: optional ConsumeResult result;
    60: optional string version;
}

struct PullResponse {
    1: Context context;
    2: list<Message> messages;
}

struct ConsumeStatsRequest {
    1: required string group
    2: optional string topic
    3: optional string version;
}

struct FetchRequest {
    1: required string consumerId;
    2: required string groupId;
    3: required string cluster;
    4: optional map<string,map<string,i64>> fetchOffset;
    10: optional i32 maxBatchSize;
    11: optional i32 maxLingerTime;
    60: optional string version;
}

struct QidResponse {
    1: required string topic;
    2: required string qid;
    3: optional i64 nextRequestOffset;
    10: required list<Message> messages;
}

struct FetchResponse {
    1: optional i32 code;
    10: required list<QidResponse> results;
}

struct AckResult {
    1: required string consumerId;
    2: required string groupId;
    3: required string cluster;
    4: required map<string,map<string,i64>> offsets;
}

struct ConsumeStats {
    1: string group;
    2: string topic;
    3: map<string,i64> consumeOffsets;
    4: map<string,i64> produceOffsets;
}

exception PullException {
    1: i32 code;
    2: string message;
}

service ConsumerService {

    PullResponse pull(1: PullRequest request) throws (1: PullException error) // pull msgs
    
    bool submit(1: ConsumeResult result) throws (1: PullException error) // submit ack
    
    list<ConsumeStats> getConsumeStats(1: ConsumeStatsRequest request) throws (1: PullException error)

    FetchResponse fetch(1: FetchRequest request) // for low-level 
    
    bool ack(1: AckResult result) // for low-level
}

```

### 部署 ###
* 修改 carrera.yaml 的配置

```yml
zookeeperAddr: 127.0.0.1:2181/carrera/v4/config # config zk cluster address here.
host: 127.0.0.1 # proxy ip (optional)
port: 9713 # thrift server port.
```

* 调用 console 的接口绑定 cproxy
* 执行 ```build.sh``` 脚本打包
* 执行 ```control.sh start``` 启动