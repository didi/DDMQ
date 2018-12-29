#include "CarreraProducerCWrapper.h"
#include "CarreraProducer.h"
#include <cstring>
#include <string>
#include <iostream> 

#ifdef __cplusplus  
extern "C" {  
#endif  
struct carreraProducer  
{  
    CarreraProducer::CarreraProducer *producer;  
};

struct carreraProducer *GetCarreraProducerInstance(char **ipList , int32_t ipListSize,int32_t timeout,int32_t retry,int32_t poolSize){
    carreraProducer *p = new struct carreraProducer;
    std::vector<std::string> proxy_list;
    for(int i = 0 ; i < ipListSize ; ++i){
        std::string ip;
        ip = *ipList;
        proxy_list.push_back(ip);
        ipList++;
    }
    CarreraProducer::CarreraConfig config;
    config.SetProxyList(proxy_list);
    config.SetProxyTimeOut(100);
    config.SetClientRetry(retry);
    config.SetClientTimeOut(timeout);
    config.SetPoolSize(poolSize);
    p->producer = new CarreraProducer::CarreraProducer(config);
    p->producer->Start();
    return p;
}

void ReleaseCarreraProducerInstance(struct carreraProducer **instance){
    (*instance)->producer->ShutDown();
    delete *instance;
    *instance = 0;
}

carreraSendResult* CarreraSend(struct carreraProducer *instance,int32_t paratitionId, int32_t hashId, char* topic, 
        char* body,int32_t bodySize , char* key,char* tag){
            std::string topicS,bodyS(body,bodySize),keyS,tagS;
            topicS = topic;
            keyS = key;
            tagS = tag;
            CarreraProducer::Result result = instance->producer->SendWithPartition(topicS,paratitionId,hashId,bodyS,keyS,tagS);
            carreraSendResult *sendResult = (carreraSendResult*)malloc(sizeof(carreraProducer));
            sendResult->code = result.code;
            sendResult->msg = (char*)malloc(sizeof(char) * result.msg.length() + 1);
            strcpy(sendResult->msg,result.msg.c_str());
            return sendResult;

}

carreraSendResult* CarreraBatchSend(struct carreraProducer *instance, char* topic ,carreraMsg *msgs , int32_t msgSize){
    std::vector<CarreraProducer::Message> msgsToSend;
    for(int i=0; i< msgSize; ++i){
        CarreraProducer::Message msg;
        std::string topicS=topic;
        std::string bodyS(msgs->body,msgs->bodySize);
        std::string keyS=msgs->key;
        std::string tagS=msgs->tag;
        instance->producer->BuildMessage(msg,topicS ,msgs->partitionId,msgs->hashId,bodyS,keyS,tagS);
        msgsToSend.push_back(msg);
        msgs++;
    }
    CarreraProducer::Result result = instance->producer->SendBatchSync(msgsToSend);
    carreraSendResult *sendResult = (carreraSendResult*)malloc(sizeof(carreraProducer));
    sendResult->code = result.code;
    sendResult->msg = (char*)malloc(sizeof(char) * result.msg.length() + 1);
    strcpy(sendResult->msg,result.msg.c_str());
    return sendResult;
}
void ReleaseCarreraSendResult(carreraSendResult **result){
    free((*result)->msg);
    free(*result);
}
#ifdef __cplusplus  
};  
#endif  