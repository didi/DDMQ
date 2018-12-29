#ifndef CARRERA_CONNECTION_H
#define CARRERA_CONNECTION_H

#include "ProducerService.h"
#include <thrift/concurrency/Thread.h>
#include <thrift/concurrency/Mutex.h>

namespace CarreraProducer{

class CarreraConnection{
public:
    CarreraConnection(std::string& addr, int timeout) : proxy_addr_(addr), client_timeout_(timeout){}

    boost::shared_ptr<ProducerServiceClient> CreateThriftClient();
    
    Result Send(const Message& msg, int64_t timeout);

    Result SendBatchSync(const std::vector<Message> & msgs);

    DelayResult Send(const DelayMessage &msg, int64_t proxy_timeout);

    std::string GetProxyAddr() { return proxy_addr_; }
   
    void SetState(int state){ state_ = state; }
    
    int GetState() { return state_; }

    void Close();

private:
    int state_;
    std::string proxy_addr_;
    int client_timeout_;
    boost::shared_ptr<ProducerServiceClient> thrift_client_;
    apache::thrift::concurrency::Mutex conn_mutex_;
};

}


#endif
