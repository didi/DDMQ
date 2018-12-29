#ifndef CARRERA_CONSUMER_POOL_H
#define CARRERA_CONSUMER_POOL_H

#include "Consumer.h"
#include "ConsumerConfig.h"

#include <vector>
#include <string>
#include <pthread.h> 

namespace CarreraConsumer {

class ConsumerPool {
  public:
    ConsumerPool();
    ConsumerPool(const ConsumerConfig *config, 
                 const char *topic, Processor proc);
    ~ConsumerPool();
    
    int SetConsumerConfig(const ConsumerConfig *conf);
    int SetConsumerProcessor(Processor proc);
    int SetConsumerTopic(const char *topic);
    
    int StartConsume();
    int Stop();
    bool IsRunning() { return running_; }

  private:
    // Noncopyable.
    ConsumerPool(const ConsumerPool &rhs);
    const ConsumerPool& operator=(const ConsumerPool &rhs);
    
    int Validate();
    void SetConsumerPool();
    void StartConsumerPool();
    static void* RunConsumer(void *arg);
    typedef struct ConsumerThread {
        Consumer *cons;
        pthread_t tid;
    } ConsumerThread;

    const ConsumerConfig *conf_; // Not owned.
    Processor proc_;
    std::string topic_;
    std::vector<ConsumerThread> consumer_pool_;
    std::vector<std::string> ips_;
    std::vector<int> ports_;
    volatile bool running_;
}; 
} // namespace CarreraConsumer
#endif // CARRERA_CONSUMER_POOL_H

