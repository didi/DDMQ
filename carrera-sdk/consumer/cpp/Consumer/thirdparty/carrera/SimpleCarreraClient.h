#ifndef SimpleCarreraClient_H
#define SimpleCarreraClient_H


#include "ConsumerService.h"
#include <boost/shared_ptr.hpp>
//#include <transport/TBufferTransports.h>

#include "mq_logger.h"

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using std::string;
using boost::shared_ptr;

namespace CarreraConsumer {

    class ProcessorBase{
    public:
        virtual ~ProcessorBase(){}
        //virtual int Process() = 0;
        virtual bool Process(const Message& , const Context&) = 0;
    }; // class ProcessorBase
    
    static const std::string EMPTY_STRING;

    // 可以考虑使用virtual function pointer， 我懒得改了。。
    // return true if successfully consumed the message, false if failed.
    typedef bool (*processor)(const Message &message, const Context &context);

    class SimpleCarreraClient {
    private:
        std::string host;
        int port;
        std::string groupName;
        std::string topic;
        int retryInterval; // unit is ms
        int submitMaxRetries;
        int socketTimeout;
        int maxLingerTime;
        int maxBatchSize;
        ProcessorBase* p;

        volatile bool isRunning;

        void ensureConnection(boost::shared_ptr <TTransport> transport);

        bool doRequest(ConsumerServiceClient &client, PullRequest &request, PullResponse &response);

        void doRetrySleep();

        void doSubmit(ConsumerServiceClient &client, ConsumeResult &result, boost::shared_ptr <TTransport> transport);

        void processResponse(PullResponse &response, ConsumeResult &result);


    public:
        SimpleCarreraClient(const std::string &_host, int _port, 
                const std::string &_groupName, ProcessorBase* _p,
                            const std::string &_topic = EMPTY_STRING, int _retryInterval = 100,
                            int _submitMaxRetries = 3, int _socketTimeout = 5000, int _maxLingerTime = 50,
                            int _maxBatchSize = 8) :
                host(_host), port(_port), groupName(_groupName), p(_p) {
            topic = _topic;
            retryInterval = _retryInterval;
            submitMaxRetries = _submitMaxRetries;
            socketTimeout = _socketTimeout;
            maxLingerTime = _maxLingerTime;
            maxBatchSize = _maxBatchSize;
            isRunning = true;       // must init it as true or we must use a signal to tell the main process.
        };

        virtual ~SimpleCarreraClient();

        void stop();

        bool isStopped();

        void consume();

        static void *startConsume(void *simpleClientobj);
    };

} // namespace

#endif
