#include "Consumer.h"
#include "ConsumerConfig.h"
#include "ConsumerPool.h"

#include <stdint.h>
#include <assert.h>

using namespace CarreraConsumer;
#ifdef __cplusplus
extern "C" {
#endif

typedef void * MessagePtr;
typedef void * ContextPtr;
typedef void * ConsumerPtr;

struct ConsumerWrapper {
    ConsumerPool *cons_pool;
};

typedef struct ConsumerWrapper ConsumerWrapper;

struct ConsumerConfigWrapper {
    ConsumerConfig *cconf;
};

typedef struct ConsumerConfigWrapper ConsumerConfigWrapper;

ConsumerWrapper* newConsumerWrapper(ConsumerConfigWrapper *config, const char *topic, Processor proc) {
    assert(config && topic);
    ConsumerPool *cons_pool = new ConsumerPool(config->cconf, topic, proc);
    ConsumerWrapper *cw = (ConsumerWrapper*)malloc(sizeof(*cw));
    cw->cons_pool = cons_pool;
    return cw;
}

ConsumerWrapper* newConsumerWrapperWithoutArg() {
    ConsumerPool *cons_pool = new ConsumerPool();
    ConsumerWrapper *cw = (ConsumerWrapper*)malloc(sizeof(*cw));
    cw->cons_pool = cons_pool;
    return cw;
}

ConsumerConfigWrapper* newConsumerConfigWrapper() {
    ConsumerConfig *cconf = new ConsumerConfig();
    ConsumerConfigWrapper *ccw = (ConsumerConfigWrapper*)malloc(sizeof(*ccw));
    ccw->cconf = cconf;
    return ccw;
}

void freeConsumerWrapper(ConsumerWrapper *cw) {
    if (cw) {
        delete (cw->cons_pool);
        free(cw);
    }
}

void freeConsumerConfigWrapper(ConsumerConfigWrapper *ccw) {
    if (ccw) {
        delete (ccw->cconf);
        free(ccw);
    }
}

// Set consumer config.
int setConfigCluster(ConsumerConfigWrapper *ccw, const char *cluster) {
    assert(ccw && ccw->cconf && cluster);
    return ccw->cconf->SetCluster(cluster);
}

int setConfigGroup(ConsumerConfigWrapper *ccw, const char *group) {
    assert(ccw && ccw->cconf && group);
    return ccw->cconf->SetGroup(group);
}

int addConfigServer(ConsumerConfigWrapper *ccw, const char *server) {
    assert(ccw && ccw->cconf && server);
    return ccw->cconf->AddServer(std::string(server));
}

int setConfigTimeout(ConsumerConfigWrapper *ccw, int timeout) {
    assert(ccw && ccw->cconf && timeout > 0);
    return ccw->cconf->SetTimeout(timeout);
}

int setConfigRetryInterval(ConsumerConfigWrapper *ccw, int interval) {
    assert(ccw && ccw->cconf && interval > 0);
    return ccw->cconf->SetRetryInterval(interval);
}

int setConfigConnPerServer(ConsumerConfigWrapper *ccw, int conn) {
    assert(ccw && ccw->cconf && conn > 0);
    return ccw->cconf->SetConnPerServer(conn);
}

int setConfigSubmitMaxRetry(ConsumerConfigWrapper *ccw, int max_retry) {
    assert(ccw && ccw->cconf && max_retry > 0);
    return ccw->cconf->SetSubmitMaxRetry(max_retry);
}

int setConfigMaxBatchSize(ConsumerConfigWrapper *ccw, int max_batch) {
    assert(ccw && ccw->cconf && max_batch > 0);
    return ccw->cconf->SetMaxBatchSize(max_batch);
}

int setConfigMaxLingerTime(ConsumerConfigWrapper *ccw, int max_linger) {
    assert(ccw && ccw->cconf && max_linger > 0);
    return ccw->cconf->SetMaxLingerTime(max_linger);
}

int setConfigAutoAckInterval(ConsumerConfigWrapper *ccw, int auto_ack) {
    assert(ccw && ccw->cconf && auto_ack > 0);
    return ccw->cconf->SetAutoAckInterval(auto_ack);
}

int setConfigLogger(ConsumerConfigWrapper *ccw, const char *conf_file) {
    assert(ccw && ccw->cconf && conf_file);
    return ccw->cconf->SetLogger(conf_file);
}

// Set consumer pool.
int setConsumerConfig(ConsumerWrapper *cw, const ConsumerConfigWrapper *config) {
    assert(cw && cw->cons_pool && config && config->cconf);
    return cw->cons_pool->SetConsumerConfig(config->cconf);
}

int setConsumerTopic(ConsumerWrapper *cw, const char *topic) {
    assert(cw && cw->cons_pool && topic);
    return cw->cons_pool->SetConsumerTopic(topic);
}

int setConsumerProcessor(ConsumerWrapper *cw,  Processor proc) {
    assert(cw && cw->cons_pool && proc);
    return cw->cons_pool->SetConsumerProcessor(proc);
}

int startConsumer(ConsumerWrapper *cw) {
    assert(cw && cw->cons_pool);
    cw->cons_pool->StartConsume();
    return 1;
}

int isRunning(ConsumerWrapper *cw) {
    assert(cw && cw->cons_pool);
    return cw->cons_pool->IsRunning();
}

int stopConsumer(ConsumerWrapper *cw) {
    assert(cw && cw->cons_pool);
    return cw->cons_pool->Stop();
}

const void* GetNextMsg(void *cons) {
    assert(cons);
    Consumer *c = (Consumer*)cons;
    return c->NextMsg();
}

const void* GetCurContext(void *cons) {
    assert(cons);
    Consumer *c = (Consumer*)cons;
    return c->CurContext();
}

const char* GetMessageKey(const MessagePtr m, int64_t *len) {
    const Message *msg = (const Message *) m;
    if (msg) {
        if (len) *len = msg->key.size();
        return msg->key.c_str();
    }
    return NULL;
}

const char* GetMessageValue(const MessagePtr m, int64_t *len) {
    const Message *msg = (const Message *) m;
    if (msg) {
        if (len) *len = msg->value.size();
        return msg->value.c_str();
    }
    return NULL;
}

const char* GetMessageTag(const MessagePtr m, int64_t *len) {
    const Message *msg = (const Message *) m;
    if (msg) {
        if (len) *len = msg->tag.size();
        return msg->tag.c_str();
    }
    return NULL;
}

int64_t GetMessageOffset(const MessagePtr m) {
    const Message *msg = (const Message *) m;
    if (msg) return msg->offset;
    return -1;
}

const char* GetContextGroupId(const ContextPtr cntx, int64_t *len) {
    const Context *context = (const Context *) cntx;
    if (context) {
        if (len) *len = context->groupId.size();
        return context->groupId.c_str();
    }
    return NULL;
}

const char* GetContextTopic(const ContextPtr cntx, int64_t *len) {
    const Context *context = (const Context *) cntx;
    if (context) {
        if (len) *len = context->topic.size();
        return context->topic.c_str();
    }
    return NULL;
}

const char* GetContextQid(const ContextPtr cntx, int64_t *len) {
    const Context *context = (const Context *) cntx;
    if (context) {
        if (len) *len = context->qid.size();
        return context->qid.c_str();
    }
    return NULL;
}

#ifdef __cplusplus
}
#endif

