#include "BridgeQHolder.h"
#include <vector>
#include <boost/shared_ptr.hpp>
#include <boost/algorithm/string.hpp>
#include "thirdparty/jsoncpp/json/json.h"
#include "CarreraDefine.h"

using std::string;
using boost::shared_ptr;

#define HTTP_STATUS_OK 200

namespace CarreraProducer {

unsigned int BridgeQHolder::client_index_ = 0;

const char* kAddurl = "/bridgeq/addTask?product=";
const char* kCacurl = "/bridgeq/cancelTask?product=";

RespInfo::RespInfo() {}

void BridgeQHolder::Post(HttpMQTask& task, const std::string& biz_key, const std::string& product, 
        const std::map<std::string, std::string>& http_headers, int url_type, RespInfo& resp_info){
    string url = bridgeq_list_[client_index_ % bridgeq_list_.size()];
   
    std::map<std::string, std::string> headers = http_headers; 
    headers["Authorization"] = biz_key;

    if(URL_TYPE_ADDTASK == url_type){
        url.append(kAddurl);
    }else if(URL_TYPE_CANCELTASK == url_type){
        url.append(kCacurl);
    }

    url.append(product);

    HttpClient http_client(url, connect_timeout_, timeout_);
    PrepareData(http_client, task, url_type);

    std::string result;
    bool ret = http_client.Post(&result, headers);

    if(ret != true || http_client.status() != HTTP_STATUS_OK){
        fprintf(stderr, "Error. Http SC=%ld", http_client.status());
    }
    Json::Reader reader;
    Json::Value value;
    if(true == reader.parse(result, value)){
        resp_info.err_no_ = atoi(value["errno"].asString().c_str());
        resp_info.err_msg_ = value["errmsg"].asString();
        resp_info.taskid_ = value["taskid"].asString();
    }else{
        resp_info.err_no_ = DECODE_HTTP_RESPONSE_ERROR;
        resp_info.err_msg_ = "Resp json string parse error";
    }
}

void BridgeQHolder::PrepareData(HttpClient& http_client, HttpMQTask& task, int url_type){
    if(URL_TYPE_ADDTASK == url_type){
        http_client.Set("topic", task.GetTopic());
        http_client.Set("url", task.GetCallBackUrl());
        http_client.Set("type", task.GetTypeStr());
        http_client.Set("mode", task.GetModeStr());
        http_client.Set("retry", task.GetRetryStr());
        http_client.Set("retry_interval", task.GetRetryIntervalStr());
        http_client.Set("timeout", task.GetTimeOutStr());
        http_client.Set("expire", task.GetExpireStr());
        http_client.Set("timestamp", task.GetTriggerTimestampStr());
        http_client.Set("params", task.GetValueParams());
        http_client.Set("params_pattern", task.GetParamsPatternStr());
        http_client.Set("headers", task.GetCallBackHeadersJson());
    }else{
        http_client.Set("taskid", task.GetTaskId());
    }
}

}


