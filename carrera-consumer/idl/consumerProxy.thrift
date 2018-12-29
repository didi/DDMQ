namespace java com.xiaojukeji.carrera.thrift.consumer
namespace go CarreraConsumer
namespace cpp CarreraConsumer

const string PRESSURE_TRAFFIC_KEY="isPressureTraffic"
const string PRESSURE_TRAFFIC_ENABLE="true"
const string PRESSURE_TRAFFIC_DISABLE="false"
const string TRACE_ID="traceId"
const string SPAN_ID="spanId"
const string CARRERA_HEADERS="carrera_headers"

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
    PullResponse pull(1: PullRequest request) throws (1: PullException error)
    bool submit(1: ConsumeResult result) throws (1: PullException error)
    list<ConsumeStats> getConsumeStats(1: ConsumeStatsRequest request) throws (1: PullException error)

    FetchResponse fetch(1: FetchRequest request)
    bool ack(1: AckResult result)
}
