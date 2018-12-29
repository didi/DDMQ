#ifndef CARRERA_DELAY_CONFIG_H
#define CARRERA_DELAY_CONFIG_H

#include "ProducerService.h"
#include <vector>

namespace CarreraProducer{

class DelayConfig{
public:
    DelayConfig() : timeout_(5000), retry_(2), connect_timeout_(300000) {}
    DelayConfig(std::vector<std::string>& proxy_list, int timeout=5000, int retry=2, int connect_timeout=300000){
        proxy_list_ = proxy_list;
        timeout_ = timeout;
        retry_ = retry;
        connect_timeout_ = connect_timeout;
    }

    void SetProxyList(std::vector<std::string>& proxy_list){
        proxy_list_ = proxy_list;
    }

    void SetTimeout(int timeout){
        timeout_ = timeout;
    }

    void SetRetry(int retry){
        retry_ = retry;
    }

    void SetConnectTimeout(int connect_timeout){
        connect_timeout_ = connect_timeout;
    }

    std::vector<std::string>& GetProxyList(){
        return proxy_list_;
    }

    int GetTimeout(){
        return timeout_;
    }
    
    int GetRetry(){
        return retry_;
    }

    int GetConnectTimeout(){
        return connect_timeout_;
    }

    bool IsValid(){
        if(proxy_list_.size() > 0 && timeout_ >= 0 && retry_ >=0 && connect_timeout_ >0){
            return true;
        }
        return false;
    }

private:
    std::vector<std::string> proxy_list_;
    int timeout_;
    int retry_;
    int connect_timeout_;
};

}


#endif
