#include "consumerwrapper.h"

#include <stdio.h>
#include <signal.h>

int running = 0;
void processOneMsg(const void *m, const void *c) {
    if (m && c) {
        const char *key, *value, *tag, *group, *topic, *qid;
        int64_t key_len, val_len, tag_len, grp_len, topic_len, qid_len, offset;
        key = GetMessageKey(m, &key_len);
        value = GetMessageValue(m, &val_len);
        tag = GetMessageTag(m, &tag_len);
        offset = GetMessageOffset(m);
        group = GetContextGroupId(c, &grp_len);
        topic = GetContextTopic(c, &topic_len);
        qid = GetContextQid(c, &qid_len);
        printf("Message: key: %s, key_len=%ld,"
               "value: %s, val_len=%ld,"
               "tag: %s, tag_len=%ld, offset=%ld,"
               "Context: group: %s, grp_len=%ld,"
               "topic: %s, topic_len=%ld,"
               "qid: %s, qid_len=%ld\n",
                key, key_len, 
                value, val_len, 
                tag, tag_len, offset, 
                group, grp_len, 
                topic, topic_len, 
                qid, qid_len 
                );
    }
}

void process(void *cons) {
    const void *m = NULL, *c = NULL;
    while ((m = GetNextMsg(cons))) {
        c = GetCurContext(cons);
        processOneMsg(m, c);
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

    ConsumerConfigWrapper *config = newConsumerConfigWrapper();
    /* Uncomment it if specific logger configuration is needed. */
    // setConfigLogger(config, "../default_log.conf");
    setConfigCluster(config, "R_test");
    setConfigGroup(config, "cg_kedis_test");
    addConfigServer(config, "127.0.0.1:9713");
    addConfigServer(config, "127.0.0.2:9713");
    setConfigAutoAckInterval(config, 10000);

    ConsumerWrapper *consumer = newConsumerWrapper(config, "test", process);
    startConsumer(consumer);

    while (running) { }

    freeConsumerWrapper(consumer);
    freeConsumerConfigWrapper(config);

    return 0;
}
