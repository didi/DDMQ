#ifndef LEGO_MQLogger_H_
#define LEGO_MQLogger_H_

#include <string>

namespace CarreraConsumer {


typedef void (* LEGO_LOG_FUNC)(const char *);


class MQLogger
{
public:
    MQLogger():func_(&DefaultOutputWrapper){}

    virtual ~MQLogger(){}

    void SetOutputFunction(LEGO_LOG_FUNC func);
    
    void Print(const char *message);

    void Printf(const char *message, ...);

    static void DefaultOutputWrapper(const char* msg);

private:
    LEGO_LOG_FUNC func_;
};

extern MQLogger mq_logger;

}


#endif  // LEGO_MQLogger_H_


