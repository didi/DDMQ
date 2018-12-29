#ifndef CARRERA_BRIDGEQ_HOLDER_H
#define CARRERA_BRIDGEQ_HOLDER_H

#include "HttpClient.h"
#include <thrift/concurrency/Thread.h>
#include <thrift/concurrency/Mutex.h>
#include <boost/shared_ptr.hpp>
#include <boost/algorithm/string.hpp>
#include <vector>

#include "ProducerService.h"
#include "HttpMQTask.h"


namespace CarreraProducer{

struct RespInfo{
    RespInfo(); 
    RespInfo(int err_no, const std::string& err_msg, const std::string& taskid = std::string()) 
        : err_no_(err_no), err_msg_(err_msg), taskid_(taskid) {
        err_no_ = 0;
        err_msg_ = "undefined.";
    }
    int err_no_;
    std::string err_msg_;
    std::string taskid_;
};

class BridgeQHolder{

public:
    BridgeQHolder(int timeout=5000, int connect_timeout=300000)
        : timeout_(timeout), connect_timeout_(connect_timeout){
        client_index_++;
    }

    int Init(std::vector<std::string>& bridgeq_list){
        bridgeq_list_ = bridgeq_list;
        return 0;
    }

    void Post(HttpMQTask& task, const std::string& biz_key, const std::string& product, 
            const std::map<std::string, std::string>& http_headers, int url_type, RespInfo& resp_info);
    
private:
    void PrepareData(HttpClient& http_client, HttpMQTask& task, int url_type);

private:
    std::vector<std::string> bridgeq_list_;
    int timeout_;
    int connect_timeout_;
    static unsigned int client_index_;
};

}


#endif
