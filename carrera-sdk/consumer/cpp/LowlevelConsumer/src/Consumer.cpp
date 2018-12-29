#include "Consumer.h"
#include "ConsumerLogger.h"

#include <boost/make_shared.hpp>
#include <thrift/protocol/TCompactProtocol.h>

#include <time.h>
#include <math.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/syscall.h>

#include <exception>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

namespace CarreraConsumer {

Consumer::Consumer()
    : config_(NULL), ip_(), port_(-1), last_ack_time_(GetCurrentTimeMs()),
      topic_(), consumer_id_(), proc_(NULL), running_(0),
      socket_(), transport_(), protocol_(), client_(NULL), 
      request_(NULL), response_(NULL), ack_result_(NULL), context_(NULL), 
      start_process_(0), cur_queue_index_(0), cur_msg_index_(0) {
}

Consumer::Consumer(const ConsumerConfig *config, const char *ip, int port, 
                   const char *topic, Processor proc)
    : config_(config), ip_(ip), port_(port), last_ack_time_(GetCurrentTimeMs()), 
      topic_(topic), consumer_id_(), proc_(proc), running_(0),
      socket_(), transport_(), protocol_(), client_(NULL), 
      request_(NULL), response_(NULL), ack_result_(NULL), context_(NULL), 
      start_process_(0), cur_queue_index_(0), cur_msg_index_(0) {
}

Consumer::~Consumer() {
    if (client_) delete client_;
    if (request_) delete request_;
    if (response_) delete response_;
    if (ack_result_) delete ack_result_;
    if (context_) delete context_;
}

long Consumer::GetCurrentTimeMs() const {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return (ts.tv_sec * 1000 + round(ts.tv_nsec / 1.0e6));
}

int Consumer::SetConfig(const ConsumerConfig *config) {
    assert(config != NULL);
    config_ = config;
    return 1;
}

int Consumer::SetTopic(const char *topic) {
    assert(topic != NULL);
    topic_ = std::string(topic);
    return 1;
}

int Consumer::SetProcessor(Processor proc) {
    assert(proc != NULL);
    proc_ = proc;
    return 1;
}

int Consumer::SetServer(const char *ip, int port) {
    assert(ip && port >= 0);
    ip_ = std::string(ip);
    port_ = port;
    return 1;
}

int Consumer::Validate() const {
    const char *err;
    if (config_ == NULL || !config_->Validate()) {
        err = "consumer config is not set.";
    } else if (topic_.empty()) {
        err = "topic is not set.";
    } else if (proc_ == NULL) {
        err = "processor is not set.";
    } else if (ip_.empty() || port_ < 0) {
        err = "ip:port is not set properly.";
    } else {
        return 1;
    }
    LOG_ERROR(err);
    return 0;
}

void Consumer::SetConsumerId() {
    char buf[128];
    int len = snprintf(buf, sizeof(buf), "%s:%d@%ld@%x",
                       ip_.c_str(), port_, 
                       syscall(__NR_gettid), 
                       rand()&0xFFFFFFF);
    if (len >= static_cast<int>(sizeof(buf))) {
        buf[sizeof(buf) - 1] = '\0';
    } else if (len >= 0){
        buf[len] = '\0';
    } else {
        LOG_FATAL("SetConsumerId failed: %s", strerror(errno));
        _exit(1);
    }
    consumer_id_ = std::string(buf);
}

void Consumer::SetConsumerRequest() {
    request_ = new FetchRequest();
    request_->__set_consumerId(consumer_id_);
    request_->__set_groupId(config_->GetGroup());
    request_->__set_cluster(config_->GetCluster());
    request_->__set_maxBatchSize(config_->GetMaxBatchSize());
    request_->__set_maxLingerTime(config_->GetMaxLingerTime());
    request_->__set_version("C++");
}

void Consumer::SetConsumerResponse() {
    response_ = new FetchResponse();
}

void Consumer::SetConsumerAckResult() {
    ack_result_ = new AckResult();
    ack_result_->__set_consumerId(consumer_id_);
    ack_result_->__set_groupId(config_->GetGroup());
    ack_result_->__set_cluster(config_->GetCluster());
}

void Consumer::SetConsumerContext() {
    context_ = new Context();
    context_->__set_groupId(config_->GetGroup());
    context_->__set_topic(topic_);
}

void Consumer::ReconnectIfNeeded() {
    if (!transport_->isOpen()) transport_->open();
}

const void* Consumer::CurContext() {
    if (!response_) return NULL;
    if (cur_queue_index_ < response_->results.size()) {
        UpdateConsumerContextQid(response_->results[cur_queue_index_].qid);
        return context_;
    }
    return NULL;
}

const void* Consumer::NextMsg() {
    if (!response_ || response_->results.empty()) return NULL;
    if (start_process_ == 0) {
        // init cur_queue_index_ and cur_msg_index_
        start_process_ = 1;
        cur_queue_index_ = 0;
        cur_msg_index_ = 0;
    } else {
        ++cur_msg_index_;
    }

    while (cur_queue_index_ < response_->results.size() 
           && cur_msg_index_ >= response_->results[cur_queue_index_].messages.size()) {
        UpdateConsumerRequestOffset(response_->results[cur_queue_index_]);
        UpdateConsumerAckOffset(response_->results[cur_queue_index_]);
        cur_queue_index_++;
        cur_msg_index_ = 0;
    }
    
    if (cur_queue_index_ >= response_->results.size()) return NULL;
    return &(response_->results[cur_queue_index_].messages[cur_msg_index_]);
}

void Consumer::ProcessResponse() {
    request_->fetchOffset.clear();
    request_->__isset.fetchOffset = false;
    ack_result_->offsets.clear();
    try {
        start_process_ = 0;
        proc_(this);  
    } catch (std::exception &e) {
        LOG_ERROR(e.what());
    } catch (...) {
        LOG_ERROR("Unknown issue occured when processing message.");
    }
   
    /*
    long cur_ms = GetCurrentTimeMs();
    if (last_ack_time_ == 0 || 
        (cur_ms - last_ack_time_) > config_->GetAutoAckInterval()) {
        CommitAckOffset();
        last_ack_time_ = cur_ms;
    }
    */
    CommitAckOffset();
    request_->__isset.fetchOffset = true;
}

void Consumer::CommitAckOffset() {
    if (ack_result_->offsets.empty()) return;

    for (int i = 0; i < config_->GetSubmitMaxRetry(); ++i) {
        try {
            ReconnectIfNeeded();
            if (client_->ack(*ack_result_)) break;
        } catch (TApplicationException& e) {
            LOG_ERROR(e.what());
        } catch (TTransportException &e) {
            LOG_ERROR(e.what());
            transport_->close();
        } catch (TException &e) {
            LOG_ERROR(e.what());
        } catch (...) {
            LOG_ERROR("Unknown issue occured when committing offset."); 
            transport_->close();
        }
        Sleep();
    }
}

void Consumer::UpdateConsumerRequestOffset(const QidResponse &qresp) {
    if (qresp.__isset.nextRequestOffset) {
        request_->fetchOffset[qresp.topic][qresp.qid] = qresp.nextRequestOffset;
    }
}

void Consumer::UpdateConsumerAckOffset(const QidResponse &qresp) {
    if (qresp.messages.size() > 0) {
        ack_result_->offsets[qresp.topic][qresp.qid] = qresp.messages.back().offset + 1;
    }
}

void Consumer::UpdateConsumerContextQid(const std::string &qid) {
    context_->__set_qid(qid);
}

int Consumer::PullMessage() {
    try {
        client_->fetch(*response_, *request_);
        if (!(response_->results).empty()) return 1;
    } catch (TApplicationException e) {
        LOG_ERROR(e.what());
    }
    return 0;
}

void Consumer::Sleep() {
    int t = config_->GetRetryInterval();
    struct timespec ts;
    ts.tv_sec = t / 1000;
    ts.tv_nsec = (t - ts.tv_sec * 1000) * 1000000L;
    nanosleep(&ts, NULL);
}

void Consumer::SetTransport() {
    socket_ = boost::make_shared<TSocket>(ip_, port_);
    socket_->setConnTimeout(config_->GetTimeout());
    socket_->setRecvTimeout(config_->GetTimeout());
    socket_->setSendTimeout(config_->GetTimeout());
    transport_ = boost::make_shared<TFramedTransport>(socket_);
    protocol_ = boost::make_shared<TCompactProtocol>(transport_);
    client_ = new ConsumerServiceClient(protocol_);
}

int Consumer::Consume() {
    if (!Validate()) return 0;
    SetConsumerId();
    SetTransport();

    SetConsumerRequest();
    SetConsumerResponse();
    SetConsumerAckResult();
    SetConsumerContext();
    
    running_ = 1;
    while (running_) {
        try {
            ReconnectIfNeeded();
            if (PullMessage()) {
                ProcessResponse();
                request_->__isset.fetchOffset = true;
                response_->results.clear();
            } else {
                request_->__isset.fetchOffset = false;
                Sleep();
            }
        } catch (TTransportException &e) {
            transport_->close();
            Sleep();
        } catch (TException &e) {
            LOG_ERROR(e.what());
            Sleep();
        } catch (...) {
            LOG_ERROR("Unknown issue occured when consumming.");
            transport_->close();
            Sleep();
        }
    }
    return 1;
}

} // namespace CarreraConsumer
