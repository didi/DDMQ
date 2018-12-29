#include "ConsumerPool.h"
#include "ConsumerLogger.h"

#include <string.h>
#include <errno.h>

namespace CarreraConsumer {

ConsumerPool::ConsumerPool()
    : conf_(NULL), proc_(NULL), 
      topic_(), consumer_pool_(),
      ips_(), ports_(), running_(false) {
}

ConsumerPool::ConsumerPool(const ConsumerConfig *conf, 
                           const char *topic, Processor proc)
    : conf_(conf), proc_(proc), 
      topic_(topic), consumer_pool_(),
      ips_(), ports_(), running_(false) {
}

ConsumerPool::~ConsumerPool() {
    if (running_) Stop();
    for (unsigned int i = 0; i < consumer_pool_.size(); ++i) {
        Consumer *cons = consumer_pool_[i].cons;
        if (cons) delete cons;
    }
}

int ConsumerPool::SetConsumerConfig(const ConsumerConfig *conf) {
    assert(conf);
    conf_ = conf;
    return 1;
}

int ConsumerPool::SetConsumerProcessor(Processor proc) {
    assert(proc);
    proc_ = proc;
    return 1;
}

int ConsumerPool::SetConsumerTopic(const char *topic) {
    assert(topic);
    topic_ = topic;
    return 1;
}

int ConsumerPool::Validate() {
    const char *err;

    if (!conf_ || !conf_->Validate()) {
        err = "ConsumerPool: config is not set.";
    } else if (!proc_) {
        err = "ConsumerPool: procssor is not set.";
    } else if (topic_.empty()) {
        err = "ConsumerPool: topic is not set.";
    } else {
        conf_->GetServerIpPort(ips_, ports_);
        if (ips_.empty() || ports_.empty()) {
            err = "ConsumerPool: ip:port is not set.";
        } else {
            return 1;
        }
    }
    LOG_ERROR(err);
    return 0;
}

void ConsumerPool::SetConsumerPool() {
    int conn_num_per_server = conf_->GetConnPerServer();
    int server_num = ips_.size();
    consumer_pool_.reserve(conn_num_per_server * server_num);
    for (int i = 0; i < server_num; ++i) {
        for (int j = 0; j < conn_num_per_server; ++j) {
            ConsumerThread ct;
            ct.cons = new Consumer(conf_, ips_[i].c_str(), ports_[i],
                                    topic_.c_str(), proc_); 
            ct.tid = 0;
            consumer_pool_.push_back(ct);
        }
    }
}

void ConsumerPool::StartConsumerPool() {
    for (unsigned int i = 0; i < consumer_pool_.size(); ++i) {
        int ret = pthread_create(&(consumer_pool_[i].tid), NULL, 
                             &RunConsumer, consumer_pool_[i].cons);
        if (ret) {
            LOG_ERROR("pthread create error: %s", strerror(errno));
            _exit(1);
        }
    }
    LOG_DEBUG("Consumer pool started.");
}

int ConsumerPool::Stop() {
    if (!running_) return 0;
    unsigned int i;
    for (i = 0; i < consumer_pool_.size(); ++i) {
        consumer_pool_[i].cons->Stop();
    }
    for (i = 0; i < consumer_pool_.size(); ++i) {
        if (pthread_join(consumer_pool_[i].tid, NULL)) {
            LOG_ERROR("pthread join error: %s", strerror(errno));
        }
    }
    running_ = false;
    return 1;
}

int ConsumerPool::StartConsume() {
    if (running_ || !Validate()) return 0;
    SetConsumerPool();
    StartConsumerPool();
    running_ = true;
    return 1;
}

void* ConsumerPool::RunConsumer(void *arg) {
    ((Consumer*)arg)->Consume();
    return NULL;
}

} // namespace CarreraConsumer
