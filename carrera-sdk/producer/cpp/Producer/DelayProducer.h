#ifndef CARRERA_DELAY_PRODUCER_CLIENT_H
#define CARRERA_DELAY_PRODUCER_CLIENT_H

#include "ProducerService.h"
#include <thrift/concurrency/Thread.h>
#include <thrift/concurrency/Mutex.h>
#include <map>
#include <exception>
#include <boost/shared_ptr.hpp>
#include "HttpMQTask.h"
#include "BridgeQHolder.h"
#include "DelayConfig.h"

using boost::shared_ptr;

namespace CarreraProducer {

class DelayProducer {
public:
    DelayProducer(DelayConfig& config) : config_(config) {}

    int Init();
    RespInfo Send(HttpMQTask& task, const std::string& biz_key, const std::string& product, 
            const std::map<std::string, std::string>& http_headers = std::map<std::string, std::string> ());

    RespInfo Cancel(HttpMQTask& task, const std::string& biz_key, const std::string& product, 
            const std::map<std::string, std::string>& http_headers = std::map<std::string, std::string> ());
private:
    RespInfo Post(HttpMQTask& task, const std::string& biz_key, 
            const std::string& product, const std::map<std::string, std::string>& http_headers, int task_type);

private:
    boost::shared_ptr<BridgeQHolder> bridgeq_holder_;
    int timeout_;
    int connect_timeout_;
    DelayConfig config_;
};

}


#endif
