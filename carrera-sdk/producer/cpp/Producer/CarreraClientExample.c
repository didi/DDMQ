#include "CarreraProducerCWrapper.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

int main(){
    int i = 0;
    setvbuf(stdout, NULL, _IOLBF, 0);
    printf("init... \n");
    char *ipList[2];
    ipList[0]= (char*)malloc(sizeof(char) * 100);
    ipList[1] = (char*)malloc(sizeof(char) * 100);
    strcpy(ipList[0],"127.0.0.1:9613");
    strcpy(ipList[1],"127.0.0.2:9613");
    carreraProducer *producer = GetCarreraProducerInstance(ipList,2,5000,2,20);
    //send one msg

    printf("Send Single Message\n");
    carreraSendResult *result = CarreraSend(producer,-1,0,(char *)"test-0",(char *)"test-test-test",14,(char *)"c-style-send",(char *)"tag|tag");
    printf("Result.code=%d\n",result->code);
    ReleaseCarreraSendResult(&result);

    //send batch msg
    printf("Send Batch Message\n");

    carreraMsg msg[10];
    for(i=0 ; i < 10 ; ++i){
        msg[i].partitionId=-1;
        msg[i].hashId=0;
        char *buff = (char*)malloc(sizeof(char) * 200);
        sprintf(buff,"%s%d","test-test-test",i);
        msg[i].body=buff;
        msg[i].bodySize=strlen(buff);
        msg[i].key=(char *)"testkey";
        msg[i].tag=(char *)"tag";
    }
    result = CarreraBatchSend(producer,(char *)"test-0",msg,10);
    printf("Result.code=%d\n",result->code);

    for(i=0;i<10;++i){
        free(msg[i].body);
    }
    ReleaseCarreraSendResult(&result);
    


    free(ipList[0]);
    free(ipList[1]);
}
