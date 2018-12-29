#include "ConsumerConfig.h"
#include "ConsumerLogger.h"

#include <assert.h>

#include <iostream>
#include <sstream>
#include <algorithm>
#include <iterator>

namespace CarreraConsumer {

ConsumerConfig::ConsumerConfig()
    : servers_(), cluster_(), group_(),
      conn_per_server_(1), timeout_(CONSUMER_DEFAULT_TIMEOUT),
      retry_interval_(CONSUMER_DEFAULT_MIN_RETRY_INTERVAL),
      submit_max_retry_(CONSUMER_DEFAULT_SUBMIT_MAX_RETRY),
      max_batch_size_(CONSUMER_DEFAULT_MAX_BATCH_SIZE),
      max_linger_time_(CONSUMER_DEFAULT_MAX_LINGER_TIME),
      auto_ack_interval_(CONSUMER_DEFAULT_AUTO_ACK_INTERVAL),
      serial_version_uid_(CONSUMER_DEFAULT_VERSION_UID) {      
}

int ConsumerConfig::SetLogger(const char *conf_file) {
    ConsumerLogger::ConfigLogger(conf_file);
    return 1;
}

int ConsumerConfig::SetCluster(const char *cluster) {
    assert(cluster != NULL);
    cluster_ = std::string(cluster);
    return 1;
}

int ConsumerConfig::SetGroup(const char *group) {
    assert(group != NULL);
    group_ = std::string(group);
    return 1;
}

int ConsumerConfig::SetConnPerServer(int conn_num) {
    assert(conn_num >= 1);
    conn_per_server_ = conn_num;
    return 1;
}

int ConsumerConfig::AddServer(const std::string &server) {
    assert(!server.empty());
    servers_.insert(server);
    return 1;
}

int ConsumerConfig::SetTimeout(int timeout) {
    assert(timeout > 0);
    timeout_ = timeout;
    return 1;
}

int ConsumerConfig::SetRetryInterval(int interval) {
    assert(interval >= 0);
    if (interval < CONSUMER_DEFAULT_MIN_RETRY_INTERVAL) {
        retry_interval_ = CONSUMER_DEFAULT_MIN_RETRY_INTERVAL;
    } else {
        retry_interval_ = interval;
    }
    return 1;
}

int ConsumerConfig::SetSubmitMaxRetry(int max_retry) {
    assert(max_retry > 0);
    submit_max_retry_ = max_retry;
    return 1;
}

int ConsumerConfig::SetMaxBatchSize(int max_batch) {
    assert(max_batch > 0);
    max_batch_size_ = max_batch;
    return 1;
}

int ConsumerConfig::SetMaxLingerTime(int max_linger) {
    assert(max_linger > 0);
    max_linger_time_ = max_linger;
    return 1;
}

int ConsumerConfig::SetAutoAckInterval(int auto_ack_interval) {
    assert(auto_ack_interval > 0);
    auto_ack_interval_ = auto_ack_interval;
    return 1;
}

static int SplitServers(std::string &ip, int *port, const std::string &server) {
    std::stringstream ss;
    ss.str(server);
    std::string token;
    int cnt = 0;
    while (std::getline(ss, token, ':')) {
        if (cnt == 0) ip = token;
        else if (cnt == 1) {
            std::istringstream iss(token);
            iss >> (*port);
        }
        ++cnt;
    }
    if (cnt == 2 && port >= 0) return 1;
    else return 0;
}

void ConsumerConfig::GetServerIpPort(std::vector<std::string> &ips, 
                                     std::vector<int> &ports) const {
    std::string ip;
    int port = -1;
    for (std::set<std::string>::iterator it = servers_.begin();
         it != servers_.end(); ++it) {
        if (SplitServers(ip, &port, (*it))) {
            ips.push_back(ip);
            ports.push_back(port);
        } 
    }
}

// 1: success, 0: failed
int ConsumerConfig::Validate() const {
    const char *err;
    if (servers_.size() == 0) {
        err = "server is not set in config.";
    } else if (cluster_.empty()) {
        err = "cluster is not set in config.";
    } else if (group_.empty()) {
        err = "group is not set in config.";
    } else {
        return 1;
    }
    LOG_ERROR(err);
    return 0;
}

} // namespace carrera_consumer
