#include <time.h>
#include <stdarg.h>
#include <stdio.h>
#include <unistd.h>
#include "mq_logger.h"

namespace CarreraConsumer {

MQLogger mq_logger;

void MQLogger::DefaultOutputWrapper(const char* msg) {
    time_t now;
    char dbgtime[26];
  
    time(&now);
    ctime_r(&now, dbgtime);
    dbgtime[24] = 0;
  
    fprintf(stderr, "Lego: %s %s\n", dbgtime, msg);
}

void MQLogger::SetOutputFunction(LEGO_LOG_FUNC func) {
    if (func) {
        func_ = func;
    }
}

void MQLogger::Print(const char *message) {
    func_(message);
}

void MQLogger::Printf(const char *message, ...) {
    static const int STACK_BUF_SIZE = 256;
    char stack_buf[STACK_BUF_SIZE];
    va_list ap;

    va_start(ap, message);
    int need = vsnprintf(stack_buf, STACK_BUF_SIZE, message, ap);
    va_end(ap);

    if (need < STACK_BUF_SIZE) {
        func_(stack_buf);
        return;
    }

    return;
}

}
