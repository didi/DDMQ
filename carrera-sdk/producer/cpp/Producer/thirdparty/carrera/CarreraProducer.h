#ifndef CARRERA_PRODUCER_CLIENT_H
#define CARRERA_PRODUCER_CLIENT_H

#include "ProducerService.h"
#include "ServiceDiscoveryService.h"
#include "CarreraConfig.h"
#include "CarreraConnection.h"
#include <thrift/concurrency/Thread.h>
#include <thrift/concurrency/Mutex.h>
#include <mutex>
#include <map>
#include <exception>
#include <boost/shared_ptr.hpp>
#include <thread>
#include "ConnectionPool.h"
#include <condition_variable>
#include "CarreraDefine.h"
#include <spdlog.h>
#include <spdlog/sinks/rotating_file_sink.h>

using boost::shared_ptr;
using CarreraServiceDiscovery::ServiceDiscoveryServiceClient;
using CarreraServiceDiscovery::ClientMeta;
using CarreraServiceDiscovery::ServiceMeta;


namespace CarreraProducer {

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

    class Sender {
    public:
        virtual Result proceed(shared_ptr<CarreraConnection> conn)=0;
    };

    class DelaySender {
    public:
        virtual DelayResult proceed(shared_ptr<CarreraConnection> conn)=0;
    };

    class MessageBuilder;

    class CarreraProducer {
    public:
        CarreraProducer(CarreraConfig &config) : config_(config), is_running_(false) {
            logger_ = spdlog::get(DEFAULT_LOGGER);
            if (logger_ == nullptr) {
                auto logger = spdlog::rotating_logger_mt(DEFAULT_LOGGER, "./carrera.log", DEFAULT_LOG_FILE_MAX_SIZE, DEFAULT_LOG_FILE_NUM);
                logger->flush_on(spdlog::level::warn);
                logger_ = spdlog::get(DEFAULT_LOGGER);
            }
        }

        void Init();

        void Start() noexcept(false);

        void ShutDown();

        void BuildMessage(Message &msg, const std::string &topic, int partitionId,
                          long hashId, const std::string &body, const std::string &key, const std::string &tags);

        Result SendWithPartition(const std::string &topic, int partitionId, long hashId,
                                 const std::string &body, const std::string &key, const std::string &tags);

        Result Send(const std::string &topic, const std::string &body, const std::string &key, const std::string &tags);

        Result Send(Message &msg);

        Result Send(const std::string &topic, const std::string &body);

        Result SendBatchSync(std::vector<Message> &msgs);

        DelayResult SendDelayMessage(DelayMessage &delayMessage);

        void generateRandomKey(std::string &key);

    private:
        static const char keyChars[];
        static const int keyCharsNum;
        std::shared_ptr<spdlog::logger> logger_;
        CarreraConfig config_;
        bool is_running_;
        apache::thrift::concurrency::Mutex conn_mutex_;
        ConnectionPool<CarreraConnection> conn_pool_;
        std::vector<std::string> proxy_addrs_;

        Result sendKernel(Sender *sender);
        DelayResult sendKernel(DelaySender *delaySender);
    };


    /**
     * CarreraProducer with support for service discovery.
     */
    class CarreraSdProducer {

    public :
        CarreraSdProducer(std::string &topic, std::string &idc, std::string &sd_server)
                : topic_(topic), idc_(idc), sd_server_(sd_server) {
            logger_ = spdlog::get(DEFAULT_LOGGER);
            if (logger_ == nullptr) {
                auto logger = spdlog::rotating_logger_mt(DEFAULT_LOGGER, "./carrera.log", DEFAULT_LOG_FILE_MAX_SIZE, DEFAULT_LOG_FILE_NUM);
                logger->flush_on(spdlog::level::warn);
                logger_ = spdlog::get(DEFAULT_LOGGER);
            }
        };

        void SetSdServers(std::string &sd_server) {
            sd_server_ = sd_server;
        }

        void SetSdPullInterval(int sd_pull_interval) {
            sd_pull_interval_ = sd_pull_interval;
        }

        void SetCarreraConfig(CarreraConfig &config) {
            config_ = config;
        }

        CarreraConfig &GetCarreraConfig() {
            return config_;
        }

        std::string &GetSdServer() {
            return sd_server_;
        }

        int GetSdPullInterval() {
            return sd_pull_interval_;
        }

        void Start() noexcept(false);

        void Stop();

        Result Send(Message &msg);

        DelayResult SendDelayMessage(DelayMessage &delayMessage);

        void UpdateServiceMeta();

    private :
        std::shared_ptr<spdlog::logger> logger_;
        volatile int state = 0; // 0:init, 1:started 2: stopped
        std::mutex mutex_producer_;
        std::shared_ptr<CarreraProducer> producer_;
        std::shared_ptr<ServiceDiscoveryServiceClient> sd_client_;

        apache::thrift::concurrency::Mutex mutex_;

        std::string topic_;
        std::string idc_;
        CarreraConfig config_;
        ClientMeta sd_client_meta_;
        ServiceMeta sd_service_meta_;
        std::string sd_server_;

        int sd_pull_interval_ = 10000;
        int sd_client_timeout_ = 5000;

        std::thread bg_thread;
        std::mutex bg_mutex;
        std::condition_variable bg_cond;

        void buildClientMeta();

        std::shared_ptr<ServiceDiscoveryServiceClient> createSdClient();

        bool fetchMeta(ServiceMeta &meta);

        void updateCarreraConfig(CarreraConfig &newConfig, ServiceMeta &meta, CarreraConfig &carreraConfig);

        bool needUpdate(const ServiceMeta &oldServiceMeta, const ServiceMeta &newServiceMeta);

        void updateProducer(std::shared_ptr<CarreraProducer>& producer);
    };

    void start_sd_pull_thread(CarreraSdProducer &sdProducer);

    class MessageBuilder {
    private :
        CarreraProducer *pProducer;
        CarreraSdProducer *pSdProducer;
        Message message;

    public :
        MessageBuilder(CarreraProducer &producer);

        MessageBuilder(CarreraSdProducer &sdProducer);

        MessageBuilder &setTopic(const std::string &topic);

        MessageBuilder &setPartitionId(int partitionId);

        MessageBuilder &setRandomPartition();

        MessageBuilder &setPartitionByHashId(long hashId);

        MessageBuilder &setBody(const std::string &body);

        MessageBuilder &setKey(const std::string &key);

        MessageBuilder &setTag(const std::string &tag);

        MessageBuilder &setTraceId(const std::string &traceId);

        MessageBuilder &setSpanId(const std::string &spanId);

        MessageBuilder &setPressureTraffic(bool isOpen);

        MessageBuilder &addProperty(const std::string &key, const std::string &value);

        Message &getMessage();

        Result send();
    };

    class DelayMessageBuilder {
    private :
        CarreraProducer *pProducer;
        CarreraSdProducer *pSdProducer;
        DelayMessage delay_message;
        std::map<std::string, std::string> headers;

    public :
        DelayMessageBuilder(CarreraProducer &producer);

        DelayMessageBuilder(CarreraSdProducer &sdProducer);

        DelayMessageBuilder &setTopic(const std::string &topic);

        DelayMessageBuilder &setUniqDelayMsgId(const std::string &uniqDelayMsgId);

        //DelayMessageBuilder &setDelayMeta(DelayMeta &delayMeta);

        DelayMessageBuilder &setDmsgType(const int32_t &val);

        DelayMessageBuilder &setTimeStamp(const int64_t &val);

        DelayMessageBuilder &setBody(const std::string &body);

        DelayMessageBuilder &setTags(const std::string &tag);

        DelayMessageBuilder &setTraceId(const std::string &traceId);

        DelayMessageBuilder &setSpanId(const std::string &spanId);

        DelayMessageBuilder &setPressureTraffic(bool isOpen);

        DelayMessageBuilder &setAction(const int32_t &val);

        DelayMessageBuilder &setInterval(const int64_t &val);

        DelayMessageBuilder &setExpire(const int64_t &val);

        DelayMessageBuilder &setTimes(const int64_t &val);

        DelayMessageBuilder &setUuid(const std::string& val);

        DelayMessageBuilder &setVersion(const std::string& val);

        DelayMessageBuilder &addProperty(const std::string &key, const std::string &value);

        DelayMessageBuilder &addHeader(const std::string &key, const std::string &value);

        DelayMessage &getMessage();

        DelayResult send();
    };

}


#endif
