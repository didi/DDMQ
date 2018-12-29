#ifndef CarreraConsumer_H
#define CarreraConsumer_H


#include "SimpleCarreraClient.h"
#include "ServiceDiscoveryService.h"
#include <string>
#include <map>
#include <vector>
#include <pthread.h>
#include <thrift/concurrency/Thread.h>
#include <thrift/concurrency/Mutex.h>
#include <boost/shared_ptr.hpp>
#include <mutex>
#include <condition_variable>
#include <thread>
#include "CarreraConfig.h"
#include <spdlog.h>
#include <spdlog/sinks/rotating_file_sink.h>
#include "CarreraDefine.h"

using boost::shared_ptr;
using CarreraServiceDiscovery::ServiceDiscoveryServiceClient;
using CarreraServiceDiscovery::ClientMeta;
using CarreraServiceDiscovery::ServiceMeta;

namespace CarreraConsumer {
    static const std::map<std::string, int> EMPTY_EXTRA_CONCURRENCY;

   class CarreraException : std::exception {
    public:
        CarreraException(const char *msg) {
            msg_ = msg;
        }

    private:
        const char *msg_;

        virtual const char *what() const throw() {
            return msg_;
        }
    };

    class CarreraConsumer {
    private:
        CarreraConfig config_;
        ProcessorBase* p;
        std::map<std::string, int> extraConcurrency;
        std::map<pthread_t, SimpleCarreraClient *> clientThreadMap;

        bool validateParams();

        bool createSimpleClient(const std::string &_host, int port, int clientNum, const std::string &topic = EMPTY_STRING);

    public:

        void SetProcessor(ProcessorBase* pro){
            p = pro;
        }
        /* Param user guide:
         * 1. _serverList: consumer proxy ip:port list, use ';' for multi, e.g. : "127.0.0.1:9713;127.0.0.2:9713;..."
         * 2. _groupName: consumer group name
         * 3. p: call back function for each message. User should implement this function, it is defined in SimpleCarrraClient.h：
         *       typedef bool (*processor)(const Message &message, const Context &context);
         *       // return true if successfully consumed the message, false if failed.
         * 4. _clientNumPerServer： thread number to connect to each consumer proxy.
         * 5. _retryInterval: the interval to sleep if there is no message now. unit is ms.
         * 6. _submitMaxRetries:  max retry times to submit consume result to proxy to update the offset.
         * 7. _socketTimeout: connect and r/w socket time out to proxy. unit is ms.
         * 8. _maxLingerTime: not used yet, for future used in long poll mode.
         * 9. _maxBatchSize: max batched message number per pull request.
         * 10._extraConcurrency: extra thread connect to each proxy for specified topic, e.g. <"test-0", 2>, it means
         *        besides _clientNumPerServer threads, it will create 2 more threads to consume topic "test-0" to each proxy.
         *
         */
        CarreraConsumer(std::string &_serverList, std::string &_groupName, ProcessorBase* p,
                      int _clientNumPerServer = 2, int _retryInterval = 100,
                      int _submitMaxRetries = 3, int _socketTimeout = 5000,
                      int _maxLingerTime = 100, int _maxBatchSize = 8,
                      const std::map<std::string, int> &_extraConcurrency = EMPTY_EXTRA_CONCURRENCY);
        
        CarreraConsumer (CarreraConfig & config, ProcessorBase * p_) : config_ (config)
        {
           p = p_;
        }

        virtual ~CarreraConsumer();

        void stop();

        void startConsume();

        void waitFinish();
    };

    class CarreraSdCconsumer {
    public:
        CarreraSdCconsumer(std::string &groupName, std::string &idc, std::string &sd_server)
                : groupName_(groupName), idc_(idc), sd_server_(sd_server) {
            logger_ = spdlog::get(DEFAULT_LOGGER);
            if (logger_ == nullptr) {
                auto logger = spdlog::rotating_logger_mt(DEFAULT_LOGGER, "./carrera_consumer.log", DEFAULT_LOG_FILE_MAX_SIZE, DEFAULT_LOG_FILE_NUM);
                logger->flush_on(spdlog::level::warn);
                logger_ = spdlog::get(DEFAULT_LOGGER);
            }
        };

        virtual ~CarreraSdCconsumer(){};
        void SetSdServers(std::string &sd_server) {
            sd_server_ = sd_server;
        }

        std::string &GetSdServer() {
            return sd_server_;
        }

        void SetCarreraConfig(CarreraConfig &config) {
            config_ = config;
        }

        void SetProcessor(ProcessorBase* pro){
            p = pro;
        }   

        void StartConsume();
        void UpdateServiceMeta(); 
        void Stop();

    private:
        std::shared_ptr<spdlog::logger> logger_;
        volatile int state = 0; // 0:init, 1:started 2: stopped
        std::mutex mutex_client_;
        std::shared_ptr<CarreraConsumer> client_;
        std::shared_ptr<ServiceDiscoveryServiceClient> sd_client_;

        apache::thrift::concurrency::Mutex mutex_;

        std::string groupName_;
        std::string idc_;
        std::string sd_server_;
        ProcessorBase* p;
        CarreraConfig config_;

        ClientMeta sd_client_meta_;
        ServiceMeta sd_service_meta_;

        int sd_client_timeout_ = 5000;

        std::thread bg_thread;
        std::mutex bg_mutex;
        std::condition_variable bg_cond;
    
        void buildClientMeta();

        std::shared_ptr<ServiceDiscoveryServiceClient> createSdClient();

        bool fetchMeta(ServiceMeta &meta);

        void updateCarreraConfig(CarreraConfig &newConfig, ServiceMeta &meta, CarreraConfig &carreraConfig);

        bool needUpdate(const ServiceMeta &oldServiceMeta, const ServiceMeta &newServiceMeta);

        void updateClient(std::shared_ptr<CarreraConsumer>& client);
};

   void start_sd_pull_thread(CarreraSdCconsumer &sdClient);
} // namespace

#endif
