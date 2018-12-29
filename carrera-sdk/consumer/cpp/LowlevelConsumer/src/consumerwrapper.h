#ifndef CARRERA_CONSUMER_WRAPPER_H
#define CARRERA_CONSUMER_WRAPPER_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif 

struct ConsumerWrapper;
struct ConsumerConfigWrapper;
struct MessageWrapper;
struct ContextWrapper;

typedef struct ConsumerWrapper ConsumerWrapper;
typedef struct ConsumerConfigWrapper ConsumerConfigWrapper;

typedef void (*ProcessMessage)(void *cons);

const void* GetNextMsg(void *cons);
const void* GetCurContext(void *cons);
const char* GetMessageKey(const void *m, int64_t *len);
const char* GetMessageValue(const void *m, int64_t *len);
const char* GetMessageTag(const void *m, int64_t *len);
int64_t GetMessageOffset(const void *m);

const char* GetContextGroupId(const void *cntx, int64_t *len);
const char* GetContextTopic(const void *cntx, int64_t *len);
const char* GetContextQid(const void *cntx, int64_t *len);

ConsumerWrapper* newConsumerWrapper(ConsumerConfigWrapper *config, const char *topic, ProcessMessage proc);
ConsumerWrapper* newConsumerWrapperWithoutArg();
ConsumerConfigWrapper* newConsumerConfigWrapper();
void freeConsumerWrapper(ConsumerWrapper *cw);
void freeConsumerConfigWrapper(ConsumerConfigWrapper *ccw);

// Set consumer config.
int setConfigCluster(ConsumerConfigWrapper *ccw, const char *cluster); 
int setConfigGroup(ConsumerConfigWrapper *ccw, const char *group);
int addConfigServer(ConsumerConfigWrapper *ccw, const char *server);
int setConfigTimeout(ConsumerConfigWrapper *ccw, int timeout);
int setConfigRetryInterval(ConsumerConfigWrapper *ccw, int interval);
int setConfigConnPerServer(ConsumerConfigWrapper *ccw, int conn);
int setConfigSubmitMaxRetry(ConsumerConfigWrapper *ccw, int max_retry);
int setConfigMaxBatchSize(ConsumerConfigWrapper *ccw, int max_batch);
int setConfigMaxLingerTime(ConsumerConfigWrapper *ccw, int max_linger);
int setConfigAutoAckInterval(ConsumerConfigWrapper *ccw, int auto_ack_interval);
int setConfigLogger(ConsumerConfigWrapper *ccw, const char* conf_file);

// Set consumer.
int setConsumerConfig(ConsumerWrapper *cw, const ConsumerConfigWrapper *config);
int setConsumerTopic(ConsumerWrapper *cw, const char *topic);
int setConsumerProcessor(ConsumerWrapper *cw, ProcessMessage proc);
int startConsumer(ConsumerWrapper *cw);
int isRunning(ConsumerWrapper *cw);
int stopConsumer(ConsumerWrapper *cw);

#ifdef cplusplus
}
#endif

#endif // CARRERA_CONSUMER_C_WRAPPER_H

