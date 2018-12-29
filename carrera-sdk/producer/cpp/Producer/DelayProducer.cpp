#include "DelayProducer.h"

#include <vector>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <protocol/TCompactProtocol.h>

#include "CarreraDefine.h"
#include <time.h>
#include <sys/time.h>


using std::string;
using boost::shared_ptr;

namespace CarreraProducer{


int DelayProducer::Init(){
    bridgeq_holder_ = boost::shared_ptr<BridgeQHolder> 
        (new BridgeQHolder(config_.GetTimeout(), config_.GetConnectTimeout()));
    int ret = bridgeq_holder_->Init(config_.GetProxyList());
    if(!config_.IsValid()){
        return -1;
    }
    return ret;
}

RespInfo DelayProducer::Send(HttpMQTask& task, const std::string& biz_key, 
        const std::string& product, const std::map<std::string, std::string>& http_headers){
    return Post(task, biz_key, product, http_headers, URL_TYPE_ADDTASK);
}

RespInfo DelayProducer::Cancel(HttpMQTask& task, const std::string& biz_key, 
        const std::string& product, const std::map<std::string, std::string>& http_headers){
    return Post(task, biz_key, product, http_headers, URL_TYPE_CANCELTASK);
}

RespInfo DelayProducer::Post(HttpMQTask& task, const std::string& biz_key, 
      const std::string& product, const std::map<std::string, std::string>& http_headers, int task_type){
    int retry_count = config_.GetRetry();

    RespInfo result;
    if(NULL == bridgeq_holder_){
        fprintf(stderr, "Producer need init.");
        return result;
    }

    while(retry_count--){
        bridgeq_holder_->Post(task, biz_key, product, http_headers, task_type, result);

        //Retry is meaningless
        if(HTTP_QPS_LIMIT == result.err_no_ || UNAUTHORIZED_REQUEST == result.err_no_){
            break;
        }

        if(result.err_no_ != 0){
            continue;
        }

        //Succ log
        break;
    }

    if(result.err_no_ != 0){
        //log, decode json or not
        fprintf(stderr, "Post error. err_no=%d, msg=%s, taskid(maybe useless)=%s",
                result.err_no_, result.err_msg_.c_str(), result.taskid_.c_str());
    }
    return result;
}

}
