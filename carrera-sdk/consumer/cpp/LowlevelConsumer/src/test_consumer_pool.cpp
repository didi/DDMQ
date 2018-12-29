#include "Consumer.h"
#include "ConsumerConfig.h"
#include "ConsumerPool.h"
#include "ConsumerLogger.h"

#include <iostream>

#include <signal.h>

using namespace CarreraConsumer;
using namespace std;

int running = 0;

void process(void *c) {
    Consumer *cons = (Consumer*)c;
    const void *m = NULL;
    while ((m = cons->NextMsg())) {
        const Message *msg = (const Message*)m;
        const Context *cntx = (const Context*)cons->CurContext();
        if (msg) cout << *msg << endl;
        if (cntx) cout << *cntx << endl;
    }
}

void sigShutdownHandler(int sig) { 
    const char *msg;
    switch (sig) {
        case SIGINT:
            msg = "Received SIGINT shutdown.";
            break;
        case SIGTERM:
            msg = "Received SIGTERM shutdown.";
            break;
        default:
            msg = "Received shutdown signal."; 
    }
    printf("%s Bye bye...\n", msg);  
    running = 0; 
}

void setupSignalHandlers(void) {
    struct sigaction act;
    sigemptyset(&act.sa_mask);
    act.sa_flags = 0;
    act.sa_handler = sigShutdownHandler;
    sigaction(SIGTERM, &act, NULL);
    sigaction(SIGINT, &act, NULL);
}

int main() {
    running = 1;
    setupSignalHandlers();

    ConsumerConfig config;
    /* Uncomment it when using specific logger configuration. */
    // ConsumerLogger::ConfigLogger("../default_log.conf");
    LOG_DEBUG("Just test.");
    config.SetCluster("R_test");
    config.SetGroup("cg_test");
    config.SetAutoAckInterval(10000);
    config.AddServer("127.0.0.1:9713");
    config.AddServer("127.0.0.2:9713");
    ConsumerPool cons_pool(&config, "test", process);
    
    cons_pool.StartConsume();

    while (running) { }

    return 0;
}
