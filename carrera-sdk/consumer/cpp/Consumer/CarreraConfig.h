#ifndef CONSUMER_CARRERACONFIG_H
#define CONSUMER_CARRERACONFIG_H

#include <vector>
#include <string>

namespace CarreraConsumer {

    typedef struct _CarreraConfig__isset {
        _CarreraConfig__isset() : serverList_(false),
                                  groupName_(false),
                                  clientNumPerServer_(false),
                                  retryInterval_(false),
                                  socketTimeout_(false),
                                  maxBatchSize_(false),
                                  maxLingerTime_(false),
                                  submitMaxRetries_(false) {}
        bool serverList_ : 1;
        bool groupName_ : 1;
        bool clientNumPerServer_ : 1;
        bool retryInterval_ : 1;
        bool socketTimeout_ : 1;
        bool maxBatchSize_ : 1;
        bool maxLingerTime_ : 1;
        bool submitMaxRetries_ : 1;
    } _CarreraConfig__isset;

    class CarreraConfig {
    public:
        std::vector<std::string> &GetServerList() {
            return serverList_;
        }

        void SetServerList(std::vector<std::string> &serverList) {
            serverList_ = serverList;
            isset_.serverList_ = true;
        }

        std::string &GetGroupName() {
            return groupName_;
        }

        void SetGroupName(std::string &groupName) {
            groupName_ = groupName;
            isset_.groupName_ = true;
        }

        int GetClientNumPerServer() {
            return clientNumPerServer_;
        }

        void SetClientNumPerServer(int clientNumPerServer) {
            clientNumPerServer_ = clientNumPerServer;
            isset_.clientNumPerServer_ = true;
        }

        int GetRetryInterval() {
            return retryInterval_;
        }

        void SetRetryInterval(int retryInterval) {
            retryInterval_ = retryInterval;
            isset_.retryInterval_ = true;
        }

        int GetSocketTimeout() {
            return socketTimeout_;
        }

        void SetSocketTimeout(int socketTimeout) {
            socketTimeout_ = socketTimeout;
            isset_.socketTimeout_ = true;
        }

        int GetMaxBatchSize() {
            return maxBatchSize_;
        }

        void SetMaxBatchSize(int maxBatchSize) {
            maxBatchSize_ = maxBatchSize;
            isset_.maxBatchSize_ = true;
        }

        int GetMaxLingerTime() {
            return maxLingerTime_;
        }

        void SetMaxLingerTime(int maxLingerTime) {
            maxLingerTime_ = maxLingerTime;
            isset_.maxLingerTime_ = true;
        }

        int GetSubmitMaxRetries() {
            return submitMaxRetries_;
        }

        void SetSubmitMaxRetries(int submitMaxRetries) {
            submitMaxRetries_ = submitMaxRetries;
            isset_.submitMaxRetries_ = true;
        }

        _CarreraConfig__isset isset_;

    private:
        std::vector<std::string> serverList_;
        std::string groupName_;
        int  clientNumPerServer_;
        int retryInterval_ = 50;
        int socketTimeout_ = 5000;
        int maxBatchSize_ = 8;
        int maxLingerTime_ = 50;
        int submitMaxRetries_ = 3;
    };
}

#endif
