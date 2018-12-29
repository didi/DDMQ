#ifndef CARRERA_DELAYMQ_TASK_H
#define CARRERA_DELAYMQ_TASK_H

#include <map>
#include <vector>

#include "HttpClient.h"
#include "thirdparty/jsoncpp/json/json.h"

namespace CarreraProducer{

class HttpMQTask{

public:
    HttpMQTask(){
    }

    HttpMQTask(const std::string& topic, const std::string& callback_url, int type, int mode, 
            int retry=3, int retry_interval=1,  
            int timeout_ms=3000, int expire=604800, int trigger_timestamp=time(NULL), 
            const std::string& params_json=std::string(), int params_pattern=2,
            const std::map<std::string, std::string>& callback_headers=std::map<std::string, std::string> ()) 
        : topic_(topic), callback_url_(callback_url), type_(type), mode_(mode), retry_(retry), 
        retry_interval_(retry_interval), timeout_ms_(timeout_ms), expire_(expire), 
        trigger_timestamp_(trigger_timestamp), params_json_(params_json), 
        params_pattern_(params_pattern), callback_headers_(callback_headers) {}

    HttpMQTask(std::string& taskid) : taskid_(taskid) {}

    void InitHttpClient(HttpClient& http_client);

    void SetTaskId(const std::string& taskid) { taskid_ = taskid; }
    void SetTopic(const std::string& topic) { topic_ = topic; }
    void SetCallBackUrl(const std::string& callback_url) { callback_url_ = callback_url; }
    void SetType(int type) { type_ = type; }
    void SetMode(int mode) { mode_ = mode; }
    void SetRetry(int retry) { retry_ = retry; }
    void SetRetryInterval(int retry_interval) { retry_interval_ = retry_interval; }
    void SetTimeOut(int timeout_ms) { timeout_ms_ = timeout_ms; }
    void SetExpire(int expire) { expire_ = expire; }
    void SetTriggerTimeStamp(int trigger_timestamp) { trigger_timestamp_ = trigger_timestamp; }
    void SetLogId(int64_t logid) { logid_ = logid; }    
    void SetParamsPattern(int params_pattern) { params_pattern_ = params_pattern; }    
    void SetParamsJson(const std::string& params_json){
        params_json_ = params_json;
    }
   
    void AddCallBackHeaders(const std::string& header_key, const std::string& header_value){
        callback_headers_[header_key] = header_value;
    }

    std::string GetTopic(){
        return topic_;
    }

    std::string GetTaskId(){
        return taskid_;
    }
    
    std::string GetCallBackUrl(){
        return callback_url_;
    }

    std::string GetTypeStr(){
        char buf[128];
        sprintf(buf, "%d", type_);
        return buf;
    }

    std::string GetModeStr(){
        char buf[128];
        sprintf(buf, "%d", mode_);
        return buf;
    }

    std::string GetRetryStr(){
        char buf[128];
        sprintf(buf, "%d", retry_);
        return buf;
    }

    std::string GetRetryIntervalStr(){
        char buf[128];
        sprintf(buf, "%d", retry_interval_);
        return buf;
    }

    std::string GetTimeOutStr(){
        char buf[128];
        sprintf(buf, "%d", timeout_ms_);
        return buf;
    }

    std::string GetExpireStr(){
        char buf[128];
        sprintf(buf, "%d", expire_);
        return buf;
    }

    std::string GetParamsPatternStr(){
        char buf[128];
        sprintf(buf, "%d", params_pattern_);
        return buf;
    }
    
    std::string GetTriggerTimestampStr(){
        char buf[128];
        sprintf(buf, "%d", trigger_timestamp_);
        return buf;
    }

    std::string GetLogIdStr(){
        char buf[128];
        sprintf(buf, "%ld", logid_);
        return buf;
    }
    
    std::string GetValueParams(){
        return params_json_;
    }

    std::string GetCallBackHeadersJson(){
        Json::Value root;
        for(std::map<std::string, std::string>::iterator iter=callback_headers_.begin(); 
                iter != callback_headers_.end(); ++iter){
            root[iter->first] = iter->second;
        }
        return root.toStyledString(); 
    }


private:
    std::string topic_;
    std::string callback_url_;
    std::string taskid_;
    int type_;
    int mode_;
    int retry_;
    int retry_interval_;
    int timeout_ms_;
    int expire_;
    int trigger_timestamp_;
    int64_t logid_;
    std::string params_json_;
    int params_pattern_;
    std::map<std::string, std::string> callback_headers_;    
};


}


#endif
