#ifndef CARRERA_CONFIG_H
#define CARRERA_CONFIG_H

#include "ProducerService.h"
#include <vector>

namespace CarreraProducer {
    typedef struct _CarreraConfig__isset {
        _CarreraConfig__isset() : proxy_list_(false),
                                  proxy_timeout_(false),
                                  client_retry_(false),
                                  client_timeout_(false),
                                  pool_size_(false) {}
        bool proxy_list_ : 1;
        bool proxy_timeout_ : 1;
        bool client_retry_ : 1;
        bool client_timeout_ : 1;
        bool pool_size_ : 1;
    } _CarreraConfig__isset;


    class CarreraConfig {
    public:
        CarreraConfig() : proxy_timeout_(50), client_retry_(2), client_timeout_(200), pool_size_(20) {}

        CarreraConfig(std::vector<std::string> &proxy_list, int proxy_timeout, int client_retry, int client_timeout,
                      int pool_size) {
            proxy_list_ = proxy_list;
            proxy_timeout_ = proxy_timeout;
            client_retry_ = client_retry;
            client_timeout_ = client_timeout;
            pool_size_ = pool_size;

            isset_.proxy_list_ = true;
            isset_.proxy_timeout_ = true;
            isset_.client_retry_ = true;
            isset_.client_timeout_ = true;
            isset_.pool_size_ = true;
        }

        void SetProxyList(std::vector<std::string> &proxy_list) {
            proxy_list_ = proxy_list;
            isset_.proxy_list_ = true;
        }

        void SetProxyTimeOut(int proxy_timeout) {
            proxy_timeout_ = proxy_timeout;
            isset_.proxy_timeout_ = true;
        }

        void SetClientRetry(int client_retry) {
            client_retry_ = client_retry;
            isset_.client_retry_ = true;
        }

        void SetClientTimeOut(int client_timeout) {
            client_timeout_ = client_timeout;
            isset_.client_timeout_ = true;
        }

        void SetPoolSize(int pool_size) {
            pool_size_ = pool_size;
            isset_.pool_size_ = true;
        }

        std::vector<std::string> &GetProxyList() {
            return proxy_list_;
        }

        int GetProxyTimeOut() {
            return proxy_timeout_;
        }

        int GetClientRetry() {
            return client_retry_;
        }

        int GetClientTimeOut() {
            return client_timeout_;
        }

        int GetPoolSize() {
            return pool_size_;
        }

        bool IsValid() {
            if (proxy_list_.size() > 0 && proxy_timeout_ >= 0
                && client_retry_ >= 0 && client_timeout_ > 0 && pool_size_ > 0) {
                return true;
            }
            return false;
        }

        _CarreraConfig__isset isset_;

    private:
        std::vector<std::string> proxy_list_;
        int proxy_timeout_;
        int client_retry_;
        int client_timeout_;

        int pool_size_;
    };

}


#endif
