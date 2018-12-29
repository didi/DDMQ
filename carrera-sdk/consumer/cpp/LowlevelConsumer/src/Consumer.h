#ifndef CARRERA_CONSUEMR_H
#define CARRERA_CONSUEMR_H

#include "ConsumerConfig.h"
#include "consumerProxy_types.h"
#include "ConsumerService.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TTransportUtils.h>
#include <thrift/TApplicationException.h>
#include <string>

namespace CarreraConsumer {

typedef void (*Processor)(void *cons);

class Consumer {
  public:
    Consumer();
    Consumer(const ConsumerConfig *config, 
             const char *ip, int port, 
             const char *topic, Processor proc);
    ~Consumer();

    int SetConfig(const ConsumerConfig *config);
    int SetTopic(const char *topic);
    int SetProcessor(Processor proc);
    int SetServer(const char *ip, int port);
    int Consume();
    int IsRunning() const { return running_; }
    void Stop() { running_ = 0; }
    int Validate() const;
    const void* NextMsg();
    const void* CurContext();

  private:
    // Noncopyable.
    Consumer(const Consumer &rhs);
    const Consumer& operator=(const Consumer &rhs);

    long GetCurrentTimeMs() const;
    void SetConsumerRequest();
    void SetConsumerResponse();
    void SetConsumerAckResult();
    void SetTransport();
    void ReconnectIfNeeded();
    void ProcessResponse();
    void CommitAckOffset();
    void UpdateConsumerRequestOffset(const QidResponse &qresp);
    void UpdateConsumerAckOffset(const QidResponse &qresp);
    void SetConsumerContext();
    void SetConsumerId();
    void UpdateConsumerContextQid(const std::string &qid);
    int PullMessage();
    void Sleep();

    const ConsumerConfig *config_; // Not owned.
    std::string ip_;
    int port_;
    long last_ack_time_;
    std::string topic_;
    std::string consumer_id_;
    Processor proc_;
    volatile int running_;

    boost::shared_ptr < ::apache::thrift::transport::TSocket > socket_;
    boost::shared_ptr < ::apache::thrift::transport::TTransport > transport_;
    boost::shared_ptr < ::apache::thrift::protocol::TProtocol > protocol_;
    ConsumerServiceClient *client_;
    
    FetchRequest *request_;
    FetchResponse *response_;
    AckResult *ack_result_;
    Context *context_;
    int start_process_;
    unsigned long long cur_queue_index_;
    unsigned long long cur_msg_index_;
};
} // CarreraConsumer
#endif // CARRERA_CONSUEMR_H
