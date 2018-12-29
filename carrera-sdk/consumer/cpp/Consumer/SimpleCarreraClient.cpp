/**
 * carrera consumer thrift sdk - simple client 
 */
#include "SimpleCarreraClient.h"
#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TCompactProtocol.h>
#include <iostream>
// for sleep
#include <time.h>

#include <string>
#include <vector>
#include <exception>
//#include <pthread.h>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using std::string;
using std::vector;
using std::exception;
using std::cout;

namespace CarreraConsumer {

// min retry interval is 50ms
    static const int MIN_RETRY_INTERVAL = 50;


    void SimpleCarreraClient::stop() {
        isRunning = false;
    }

// should stopped
    SimpleCarreraClient::~SimpleCarreraClient() {
        isRunning = false;
    }

    bool SimpleCarreraClient::isStopped() {
        return !isRunning;
    }

// ensure the connection is ok
// will throw network related exception
    void SimpleCarreraClient::ensureConnection(boost::shared_ptr <TTransport> transport) {
        if (!transport->isOpen()) {
            transport->open();
        }
    }

// send request to and get response from server
// return true:  get msg
//        false: get nothing
// throw: TException - network error.
//        TApplicationException - unknown issue.
//     both exceptions reqire reconnection.
    bool SimpleCarreraClient::doRequest(ConsumerServiceClient &client, PullRequest &request, PullResponse &response) {
        try {
            client.pull(response, request);
            if (response.messages.empty()) {
                //std::cout << __func__ << ": no message responsed\n";
                //mq_logger.Printf("%s: no message responsed", __func__);
                //mq_logger.debug("retry in {}ms, response={}", config.getRetryInterval(), response);
                return false;
            } else {
                return true;
            }
        } catch (PullException &e) {
            mq_logger.Printf("%s: pull exception[%s]. code=%d, msg=%s", __func__, 
                    e.what(), e.code, e.message.c_str());
            //std::cout << __func__ << ": pull exception: " << e.what() << ", code: " << e.code << ", message: "
              //        << e.message << "\n";
            // mq_logger.error("pull exception, code={}, message={}", e.code, e.message);
        }
        return false;
    }

    void threadSleep(int ms) {
        struct timespec sleepTime;
        struct timespec returnTime;
        sleepTime.tv_sec = 0;
        if (ms >= 1000) {
            sleepTime.tv_sec = (time_t) ms / 1000;
            ms = ms % 1000;
        }
        sleepTime.tv_nsec = ms * 1000 * 1000;
        nanosleep(&sleepTime, &returnTime);
    }

// sleep for a while if get nothing from server 
    void SimpleCarreraClient::doRetrySleep() {
        threadSleep(retryInterval > MIN_RETRY_INTERVAL ? retryInterval : MIN_RETRY_INTERVAL);
    }

// submit commit offset to server
// should not throw exception
    void SimpleCarreraClient::doSubmit(ConsumerServiceClient &client, ConsumeResult &result,
                                       boost::shared_ptr <TTransport> transport) {
        for (int i = 0; i < submitMaxRetries; i++) {
            try {
                ensureConnection(transport);
                if (client.submit(result)) {
                    return;
                } else {
                    //std::cout << __func__ << ": failed to submit " << i << "\n";
                    mq_logger.Printf("%s: failed to submit", __func__);
                    //mq_logger.warn("doSubmit failed!, retryCnt=" + i);
                }
            } catch (PullException &e) {
                mq_logger.Printf("%s: submit exception[%s], code=%d, msg=%s", __func__, 
                        e.what(), e.code, e.message.c_str());
                //std::cout << __func__ << ": pull exception: " << e.what() << ", code: " << e.code << ", message: "
                //          << e.message << "\n";
                // mq_logger.error("pull exception, code={}, message={}", e.code, e.message);
            } catch (TTransportException &e) {
                mq_logger.Printf("%s: tte=%s", __func__, e.what());
                //std::cout << __func__ << ": tte, " << e.what() << "\n";
                //mq_logger.error("submit transport error, retryCnt=" + i, e);
                transport->close();
            } catch (TException &e) {
                mq_logger.Printf("%s: te=%s", __func__, e.what());
                //std::cout << __func__ << ": te, " << e.what() << "\n";
                //mq_logger.error("submit error, retryCnt=" + i, e);
            } catch (...) {
                mq_logger.Printf("%s: unkown e", __func__);
                //std::cout << __func__ << ": unkown e \n";
                //mq_logger.error("submit transport error, retryCnt=" + i, e);
                transport->close();
            }
            doRetrySleep();
        }
    }

// deal msg responsed from server.
// should not throw exception.
    void SimpleCarreraClient::processResponse(PullResponse &response, ConsumeResult &result) {
        result.context = response.context;
        // clear offset vector
        result.successOffsets.clear();
        result.failOffsets.clear();

        for (std::vector<Message>::iterator it = response.messages.begin(); it != response.messages.end(); it++) {
            bool success = false;
            try {
                success = p->Process(*it, result.context);
            } catch (std::exception &e) {
                //std::cout << __func__ << ": e, " << e.what() << "\n";
                mq_logger.Printf("%s: e=%s", __func__, e.what());
                //mq_logger.error("exception when processing message, msg=" + msg + ",context=" + context, e);
            } catch (...) {
                //std::cout << __func__ << ": unknown e\n";
                mq_logger.Printf("%s: unkown e", __func__);
                //mq_logger, unknown error.
            }

            if (success) {
                result.successOffsets.push_back(it->offset);
            } else {
                result.failOffsets.push_back(it->offset);
            }
        }
    }

// consume msg function running in a thread
    void SimpleCarreraClient::consume() {
        boost::shared_ptr <TSocket> socket(new TSocket(host, port));
        boost::shared_ptr <TTransport> transport(new TFramedTransport(socket));
        boost::shared_ptr <TProtocol> protocol(new TCompactProtocol(transport));

        // since this a single thread model function, following params can be reused.
        ConsumerServiceClient client(protocol);
        PullRequest request;
        //ConsumeResult result;

        // set socket timtout
        socket->setConnTimeout(socketTimeout);
        socket->setRecvTimeout(socketTimeout);
        socket->setSendTimeout(socketTimeout);

        // set request
        //std::cout << "group name: " << groupName << "\n";
        request.__set_groupId(groupName);
        request.__set_maxBatchSize(maxBatchSize);
        request.__set_maxLingerTime(maxLingerTime);
        if (!topic.empty()) {
            request.__set_topic(topic);
        }

        // set running state flag
        isRunning = true;

        bool ret;
        //std::cout << __func__ << ": start consume group " << groupName << "\n";
        // mq_logger.info("start consume group:{}", consumer_group_name);
        while (isRunning) {
            try {
                ensureConnection(transport);
                PullResponse response;
                ret = doRequest(client, request, response);
                if (!ret) { //no new message
                    if(request.__isset.result){
                        request.result.successOffsets.clear();
                        request.result.failOffsets.clear();
                    }
                    request.__isset.result = false;
                    doRetrySleep();
                } else {
                    processResponse(response, request.result);
                    request.__isset.result = true;
                }
            } catch (TTransportException &e) {
                mq_logger.Printf("%s: tte=%s", __func__, e.what());
                //std::cout << __func__ << ": tte, " << e.what() << "\n";
                //mq_logger.error("TTransportException, consumer=" + this.toString(), e);
                transport->close();
                doRetrySleep();
            } catch (TException &e) {
                mq_logger.Printf("%s: te=%s", __func__, e.what());
                //std::cout << __func__ << ": te, " << e.what() << "\n";
                //mq_logger.error("TException, consumer=" + this.toString(), e);
                doRetrySleep();
            } catch (...) {
                // unknown issue
                mq_logger.Printf("%s: unkown e", __func__);
                //std::cout << __func__ << ": unknown e\n";
                transport->close();
                doRetrySleep();
            }

        }

        try {
            if ((!request.result.failOffsets.empty()) || (!request.result.successOffsets.empty())) {
                doSubmit(client, request.result, transport);
            }
        } catch (...) {
            mq_logger.Printf("%s: unkown e for submit last", __func__);
            //std::cout << __func__ << ": unknown e for submit last\n";
            //unknown issue
        }

        if (transport->isOpen()) {
            transport->close();
        }
        //mq_logger.info("consume group[{}] finished!", config.getGroupId());
    }

    void *SimpleCarreraClient::startConsume(void *simpleClientobj) {
        ((SimpleCarreraClient *) simpleClientobj)->consume();
        return NULL;
    }

    /* the other way that to new thread in simpleClient
    void * SimpleCarreraClient::consumeHelperFunc(void * This) {
        ((SimpleCarreraClient *)This)->consume();
        return NULL;
    }

    bool SimpleCarreraClient::startConsumeThread() {
        return (pthread_create(&tid, NULL, consumeHelperFunc, this) == 0);
    }
     */

} // namespace

