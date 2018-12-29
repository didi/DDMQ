#ifndef CARRERA_C_PRODUCER_CLIENT_H
#define CARRERA_C_PRODUCER_CLIENT_H
#include <stdint.h>

#ifdef __cplusplus 
struct carreraProducer;
struct carreraSendResult{
    int32_t code;//
    char *msg;
};
struct carreraMsg{
    int32_t partitionId;
    int32_t hashId;
    int32_t bodySize;
    char *key;
    char *body;
    char *tag;
};
#else
typedef struct {} carreraProducer;
typedef struct {
    int32_t code;//
    char *msg;
} carreraSendResult;
typedef struct{
    int32_t partitionId;
    int32_t hashId;
    int32_t bodySize;
    char *key;
    char *body;
    char *tag;
} carreraMsg;
#endif  



#ifdef __cplusplus  
extern "C" {  
#endif  
struct carreraProducer *GetCarreraProducerInstance(char **ipList , int32_t ipListSize,int32_t timeout,int32_t retry,int32_t poolSize);
void ReleaseCarreraProducerInstance(struct carreraProducer **instance);  
carreraSendResult *CarreraSend(struct carreraProducer *instance,int32_t paratitionId, int32_t hashId, char* topic, char* body,int32_t bodySize, char* key,char* tag);
carreraSendResult *CarreraBatchSend(struct carreraProducer *instance, char* topic ,carreraMsg *msgs , int32_t msgSize);
void ReleaseCarreraSendResult(carreraSendResult **result);
//extern void SetColor(struct tagApple *pApple, int color);  
//extern int GetColor(struct tagApple *pApple);  
#ifdef __cplusplus  
};  

#endif

#endif