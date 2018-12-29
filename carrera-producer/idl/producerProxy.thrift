namespace php didi.carrera.producer.proxy
namespace java com.xiaojukeji.carrera.thrift
namespace go CarreraProducer
namespace cpp CarreraProducer

struct Result {
	1: i32 code
	2: string msg
	3: optional string key //GO String方法已经重写，请根据新的Result进行修改，不要直接覆盖！
}

const string PRESSURE_TRAFFIC_KEY="isPressureTraffic"
const string PRESSURE_TRAFFIC_ENABLE="true"
const string PRESSURE_TRAFFIC_DISABLE="false"
const string TRACE_ID="traceId"
const string SPAN_ID="spanId"
const string CARRERA_HEADERS="carrera_headers"

struct RestMessage {
    1: byte type = 1;                            // 1: common, 2: delay
    2: byte mode = 1;                            // 1: get, 2: set
    3: string  url;                              // call back url
    4: map<string, string>   params;             // param
    5: map<string, string>   headers;            // header
    6: i64 timestamp;                            // timestamp of the message
    7: i64 expire;                               // call back time out
    8: i64 timeout;                              // expire time
    9: i32 retryCnt = 3;                         // noify retry times
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

struct DelayMeta {
    1: i64 timestamp;			                    // timestamp to trigger
    2: optional i32 dmsgtype;                       // type, 1-delay(default); 2-loop delay
    3: optional i64 interval                        // interval of loop message, if not loop, just leave it
    4: optional i64 expire;                         // expire time
    5: optional i64 times;                          // loop times
    7: optional map<string, string>  properties     // properties
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
