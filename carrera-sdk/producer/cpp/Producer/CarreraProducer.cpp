#include "CarreraProducer.h"


#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <protocol/TCompactProtocol.h>
#include "CarreraDefine.h"
#include <sys/time.h>
#include "producerProxy_constants.h"
#include <iostream>
#include <boost/algorithm/string.hpp>
#include <thread>
#include "thirdparty/jsoncpp/json/json.h"
#include <uuid/uuid.h>

using std::string;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

//#define DEBUG

#ifdef DEBUG
#define dprintf printf
#else
#define dprintf
#endif

namespace CarreraProducer {

    class SingleDelayMessageSender : public DelaySender {
    public:
        SingleDelayMessageSender(DelayMessage &m, int timeout) : delayMessage(m), timeout(timeout) {};

        DelayResult proceed(shared_ptr<CarreraConnection> conn) {
            return conn->Send(delayMessage, timeout);
        }
    private:
        const DelayMessage &delayMessage;
        int timeout;
    };

    class SingleMessageSender : public Sender {
    public:
        SingleMessageSender(Message &m, int timeout) : message(m), timeout(timeout) {};

        Result proceed(shared_ptr<CarreraConnection> conn) {
            return conn->Send(message, timeout);
        }

    private:
        const Message &message;
        int timeout;
    };

    class MultiMessageSender : public Sender {
    public:
        MultiMessageSender(const std::vector<Message> &msgs) : msgs(msgs) {};

        Result proceed(shared_ptr<CarreraConnection> conn) {
            return conn->SendBatchSync(msgs);
        }

    private:
        const std::vector<Message> &msgs;
    };

    void CarreraProducer::Start() noexcept(false) {
        srand(time(NULL));
        apache::thrift::concurrency::Guard g(conn_mutex_);
        if (is_running_) {
            return;
        }

        if (config_.IsValid()) {
            this->Init();
            is_running_ = true;
        } else {
            throw CarreraException("Config invalid");
        }
    }

    void CarreraProducer::Init() {
        proxy_addrs_ = (this->config_).GetProxyList();
        for (size_t i = 0; i < proxy_addrs_.size(); ++i) {
            for (int j = 0; j < (this->config_).GetPoolSize(); ++j) {
                boost::shared_ptr<CarreraConnection> conn_ptr
                        (new CarreraConnection(proxy_addrs_[i], (this->config_).GetClientTimeOut()));
                conn_pool_.AddConnection(proxy_addrs_[i], conn_ptr);
            }
        }
    }

    void CarreraProducer::ShutDown() {
        apache::thrift::concurrency::Guard g(conn_mutex_);
        if (!is_running_) {
            return;
        }

        this->is_running_ = false;
        conn_pool_.FreeConnection();
    }

    void CarreraProducer::BuildMessage(Message &message, const std::string &topic, int partitionId, long hashId,
                                       const std::string &body, const std::string &key, const std::string &tags) {
        message.__set_topic(topic);
        message.__set_partitionId(partitionId);
        message.__set_hashId(hashId);
        message.__set_body(body);
        message.__set_value("");
        message.__set_key(key);
        message.__set_version(CARRERA_VERSION);
        if (!tags.empty()) {
            message.__set_tags(tags);
        } else {
            message.__set_tags("*");
        }
    }

    Result CarreraProducer::Send(const std::string &topic,
                                 const std::string &body, const std::string &key, const std::string &tags) {
        return SendWithPartition(topic, -2, 0, body, key, tags);
    }

    DelayResult CarreraProducer::sendKernel(DelaySender *delaySender) {
        DelayResult ret;
        if (!is_running_) {
            ret.__set_msg("Producer not start.");
            return ret;
        }

        ret.code = CLIENT_EXCEPTION;
        int retry_count = config_.GetClientRetry();

        ret.code = FAIL_TIMEOUT;
        while (retry_count--) {
            shared_ptr<CarreraConnection> connection = conn_pool_.FetchConnection();

            if (NULL == connection) {
                continue;
            }

            ret = delaySender->proceed(connection);

            if (ret.code > CACHE_OK) {
                switch (ret.code) {
                    case FAIL_ILLEGAL_MSG:
                    case FAIL_TOPIC_NOT_ALLOWED:
                    case FAIL_TOPIC_NOT_EXIST:
                        connection->SetState(CONNECTION_HEALTHY);
                        break;
                    case FAIL_TIMEOUT:
                        break;
                    default:
                        usleep(config_.GetClientTimeOut() * 1000);
                }
                conn_pool_.ReleaseConnection(connection);
                continue;
            } else {
                connection->SetState(CONNECTION_HEALTHY);
            }
            conn_pool_.ReleaseConnection(connection);
            return ret;
        }

        return ret;

    }

    Result CarreraProducer::sendKernel(Sender *sender) {
        Result ret;
        if (!is_running_) {
            ret.__set_msg("Producer not start.");
            return ret;
        }

        ret.code = CLIENT_EXCEPTION;
        int retry_count = config_.GetClientRetry();

        ret.code = FAIL_TIMEOUT;
        while (retry_count--) {
            shared_ptr<CarreraConnection> connection = conn_pool_.FetchConnection();

            if (NULL == connection) {
                continue;
            }

            ret = sender->proceed(connection);

            if (ret.code > CACHE_OK) {
                switch (ret.code) {
                    case FAIL_ILLEGAL_MSG:
                    case FAIL_TOPIC_NOT_ALLOWED:
                    case FAIL_TOPIC_NOT_EXIST:
                        connection->SetState(CONNECTION_HEALTHY);
                        break;
                    case FAIL_TIMEOUT:
                        break;
                    default:
                        usleep(config_.GetClientTimeOut() * 1000);
                }
                conn_pool_.ReleaseConnection(connection);
                continue;
            } else {
                connection->SetState(CONNECTION_HEALTHY);
            }
            conn_pool_.ReleaseConnection(connection);
            return ret;
        }

        return ret;
    }

    Result CarreraProducer::SendBatchSync(std::vector<Message> &msgs) {
        Result ret;
        timeval time;
        uint64_t begin_time = 0;
        uint64_t end_time = 0;
        std::string key;
        int i = 0;
        for (std::vector<Message>::iterator it = msgs.begin(); it != msgs.end(); ++it, ++i) {
            if (it->key.empty()) {
                if (key.empty()) {
                    generateRandomKey(key);
                }
                it->key.reserve(key.length() + 3);
                it->key = key;
                it->key += "-";
                it->key += keyChars[(i / keyCharsNum) % keyCharsNum];
                it->key += keyChars[i % keyCharsNum];
            }
        }
        ::gettimeofday(&time, 0);
        begin_time = time.tv_sec * 1000 * 1000 + time.tv_usec;

        MultiMessageSender sender = MultiMessageSender(msgs);
        ret = sendKernel(&sender);
        if (!key.empty()) {
            ret.key = key;
        }
        ::gettimeofday(&time, 0);
        end_time = time.tv_sec * 1000 * 1000 + time.tv_usec;
        if (ret.code > CACHE_OK) {
            //Fail log record
            logger_->warn("SEND BATCH MESSAGE FAILED: ret_code: {}, ret_msg: {}, ret_key: {}, timecost: {}",
                    ret.code, ret.msg, ret.key, end_time - begin_time);
        }

        return ret;
    }

    Result CarreraProducer::SendWithPartition(const std::string &topic, int partitionId, long hashId,
                                              const std::string &body, const std::string &key,
                                              const std::string &tags) {
        Message msg;
        BuildMessage(msg, topic, partitionId, hashId, body, key, tags);
        return Send(msg);
    }

    Result CarreraProducer::Send(Message &msg) {
        Result ret;
        timeval time;
        uint64_t begin_time = 0;
        uint64_t end_time = 0;
        ::gettimeofday(&time, 0);
        if (msg.key.empty()) {
            generateRandomKey(msg.key);
        }
        begin_time = time.tv_sec * 1000 * 1000 + time.tv_usec;
        SingleMessageSender sender = SingleMessageSender(msg, config_.GetProxyTimeOut());
        ret = sendKernel(&sender);
        ::gettimeofday(&time, 0);
        end_time = time.tv_sec * 1000 * 1000 + time.tv_usec;
        if (ret.code > CACHE_OK) {
            //Fail log record
            logger_->warn("SEND MESSAGE FAILED: topic: {}, partition: {}, hashId: {}, body: {}, key: {}, tags: {}, ret_code: {}, timecost: {}"  ,
                      msg.topic, msg.partitionId, msg.hashId, msg.body, msg.key, msg.tags, ret.code, end_time - begin_time);
        }
        return ret;
    }

    DelayResult CarreraProducer::SendDelayMessage(DelayMessage &delayMessage) {
        DelayResult ret;
        timeval time;
        uint64_t begin_time = 0;
        uint64_t end_time = 0;
        ::gettimeofday(&time, 0);

        begin_time = time.tv_sec * 1000 * 1000 + time.tv_usec;
        SingleDelayMessageSender delaySender = SingleDelayMessageSender(delayMessage, config_.GetProxyTimeOut());
        ret = sendKernel(&delaySender);
        ::gettimeofday(&time, 0);
        end_time = time.tv_sec * 1000 * 1000 + time.tv_usec;
        if (ret.code > CACHE_OK) {
            //Fail log record
            logger_->warn("SEND DELAY MESSAGE FAILED: topic: {}, uniqDelayMsgId: {}, body: {}, tags: {}, action: {}, dmsgtype: {}, ret_code: {}, timecost: {}"  ,
                          delayMessage.topic, delayMessage.uniqDelayMsgId, delayMessage.body, delayMessage.tags, delayMessage.action, delayMessage.dmsgtype, ret.code, end_time - begin_time);
        }
        return ret;
    }

    Result CarreraProducer::Send(const std::string &topic, const std::string &body) {
        return MessageBuilder(*this).setTopic(topic).setBody(body).send();
    }

    const char CarreraProducer::keyChars[] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const int CarreraProducer::keyCharsNum = 62;

    void CarreraProducer::generateRandomKey(std::string &str) {
        str.clear();
        str.reserve(sizeof(keyChars));
        for (int i = 0; i < RANDOM_KEY_SIZE; i++) {
            str += keyChars[rand() % keyCharsNum];
        }
    }

    /**
     * impl of MessageBuilder
     * */
    MessageBuilder::MessageBuilder(CarreraSdProducer &sdProducer) : pProducer(nullptr), pSdProducer(&sdProducer) {
        message.__set_version(CARRERA_VERSION);
        message.__set_partitionId(-2);
    }

    MessageBuilder::MessageBuilder(CarreraProducer &producer) : pProducer(&producer), pSdProducer(nullptr) {
        message.__set_version(CARRERA_VERSION);
        message.__set_partitionId(-2);
    }

    MessageBuilder &MessageBuilder::setTopic(const std::string &topic) {
        this->message.__set_topic(topic);
        return *this;
    }

    MessageBuilder &MessageBuilder::setPartitionId(int partitionId) {
        this->message.__set_partitionId(partitionId);
        return *this;
    }

    MessageBuilder &MessageBuilder::setRandomPartition() {
        this->message.__set_partitionId(-2);
        return *this;
    }

    MessageBuilder &MessageBuilder::setPartitionByHashId(long hashId) {
        this->message.__set_partitionId(-1);
        this->message.__set_hashId(hashId);
        return *this;
    }

    MessageBuilder &MessageBuilder::setBody(const std::string &body) {
        this->message.__set_body(body);
        return *this;
    }

    MessageBuilder &MessageBuilder::setKey(const std::string &key) {
        this->message.__set_key(key);
        return *this;
    }

    MessageBuilder &MessageBuilder::setTag(const std::string &tag) {
        this->message.__set_tags(tag);
        return *this;
    }

    MessageBuilder &MessageBuilder::setTraceId(const std::string &traceId) {
        this->message.properties[g_producerProxy_constants.TRACE_ID] = traceId;
        this->message.__isset.properties = true;
        return *this;
    }

    MessageBuilder &MessageBuilder::setSpanId(const std::string &spanId) {
        this->message.properties[g_producerProxy_constants.SPAN_ID] = spanId;
        this->message.__isset.properties = true;
        return *this;
    }

    MessageBuilder &MessageBuilder::setPressureTraffic(bool isOpen) {
        this->message.properties[g_producerProxy_constants.PRESSURE_TRAFFIC_KEY] =
                isOpen ? g_producerProxy_constants.PRESSURE_TRAFFIC_ENABLE
                       : g_producerProxy_constants.PRESSURE_TRAFFIC_DISABLE;
        this->message.__isset.properties = true;
        return *this;
    }

    MessageBuilder &MessageBuilder::addProperty(const std::string &key, const std::string &value) {
        this->message.properties[key] = value;
        this->message.__isset.properties = true;
        return *this;
    }

    Result MessageBuilder::send() {
        if (pProducer != nullptr) {
            return pProducer->Send(this->message);
        } else {
            return pSdProducer->Send(this->message);
        }
    }

    Message &MessageBuilder::getMessage() {
        return this->message;
    }

    void CarreraSdProducer::Start() noexcept(false) {
        apache::thrift::concurrency::Guard g(mutex_);
        if (state != STATE_INIT) {
            throw CarreraException("state is not STATE_INIT");
        }
        state = STATE_START;
        buildClientMeta();
        auto ret = fetchMeta(this->sd_service_meta_);
        for (auto &c_itr : this->sd_service_meta_.metaList) {
            for (auto &endpoint : c_itr.endpoints) {
                logger_->info("Start: endpoint.ip = {}, endpoint.port = {}", endpoint.ip, endpoint.port);
            }
        }
        if (!ret && !config_.isset_.proxy_list_) {
            throw CarreraException("Fetch Meta failed and config.proxy_list is empty");
        }

        CarreraConfig newConfig;
        updateCarreraConfig(newConfig, this->sd_service_meta_, config_);
        bg_thread = std::thread(start_sd_pull_thread, std::ref(*this));

        this->producer_ = std::make_shared<CarreraProducer>(newConfig);
        this->producer_->Start();
    }

    std::string join(std::vector<std::string> &v, const std::string &t) {
        std::string result;
        std::vector<std::string>::iterator it;
        for (it = v.begin(); it != v.end(); it++) {
            if (!result.empty())
                result.append(t);
            result.append(*it);
        }
        return result;
    }

    void CarreraSdProducer::buildClientMeta() {
        sd_client_meta_.idc = this->idc_;
        sd_client_meta_.version = CARRERA_VERSION;

        //Get host name
        char host[MAX_HOST_NAME_LEN];
        gethostname(host, MAX_HOST_NAME_LEN - 1);

        struct timeval tv;
        gettimeofday(&tv, NULL);
        long timestamp = tv.tv_sec * 1000 + tv.tv_usec / 1000;

        int pid = (int) getpid();

        srand(time(NULL));
        int rid = rand();

        char cid_buffer[MAX_CLIENT_ID_LEN];
        sprintf(cid_buffer, "%d_%ld_%s_%x", pid, timestamp, host, rid);
        sd_client_meta_.clientId = std::string(cid_buffer);

        sd_client_meta_.config["proxy_list_"].userDefined = config_.isset_.proxy_list_;
        sd_client_meta_.config["proxy_list_"].value = join(config_.GetProxyList(), std::string(";"));
        sd_client_meta_.config["proxy_timeout_"].userDefined = config_.isset_.proxy_timeout_;
        sd_client_meta_.config["proxy_timeout_"].value = std::to_string(config_.GetProxyTimeOut());
        sd_client_meta_.config["client_retry_"].userDefined = config_.isset_.client_retry_;
        sd_client_meta_.config["client_retry_"].value = std::to_string(config_.GetClientRetry());
        sd_client_meta_.config["client_timeout_"].userDefined = config_.isset_.client_timeout_;
        sd_client_meta_.config["client_timeout_"].value = std::to_string(config_.GetClientTimeOut());
        sd_client_meta_.config["pool_size_"].userDefined = config_.isset_.pool_size_;
        sd_client_meta_.config["pool_size_"].value = std::to_string(config_.GetPoolSize());
    }

    std::shared_ptr<ServiceDiscoveryServiceClient> CarreraSdProducer::createSdClient() {
        std::vector<std::string> ipport;
        boost::split(ipport, this->sd_server_, boost::is_any_of(":"));
        if (ipport.size() != 2) {
            return std::shared_ptr<ServiceDiscoveryServiceClient>();
        }
        std::string ip = ipport[0];
        int port = std::stoi(ipport[1]);

        boost::shared_ptr<TSocket> socket(new TSocket(ip, port));
        boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
        
        socket->setConnTimeout(this->sd_client_timeout_);
        socket->setRecvTimeout(this->sd_client_timeout_);
        socket->setSendTimeout(this->sd_client_timeout_);

        try {
            transport->open(); //This may throws exceptions.
        }catch (TException &e) {
            logger_->error("transport open failed: {}", e.what());
//            transport->close();
        }

        boost::shared_ptr<TProtocol> protocol(new TCompactProtocol(transport));

        return std::shared_ptr<ServiceDiscoveryServiceClient>(new ServiceDiscoveryServiceClient(protocol));
    }

    bool CarreraSdProducer::fetchMeta(ServiceMeta &meta) {
        int retries = 1 + config_.GetClientRetry();
        while (retries-- > 0) {
            try {
                if (NULL == this->sd_client_) {
                    this->sd_client_ = createSdClient();
                    if (NULL == this->sd_client_) {
                        return false;
                    }
                }
                this->sd_client_->discoverProducerService(meta, topic_, sd_client_meta_);
                return true;
            } catch (TException &e) {
                logger_->error("fetchMeta failed: {}", e.what());
            }
        }
        return false;
    }

    void CarreraSdProducer::Stop() {
        apache::thrift::concurrency::Guard g(mutex_);
        if (state != STATE_START) {
            throw CarreraException("state is not STATE_START");
        }
        {
            std::lock_guard<std::mutex> lock(bg_mutex);
            state = STATE_STOP;
        }
        producer_->ShutDown();
        bg_cond.notify_one();
        if (bg_thread.joinable()) {
            bg_thread.join();
        }
    }

    void CarreraSdProducer::UpdateServiceMeta() {
        std::unique_lock<std::mutex> lock(bg_mutex);
        while (state != STATE_STOP) {
            bg_cond.wait_for(lock, std::chrono::seconds(10));
            if (state == STATE_STOP) {
                break;
            }

            try {
                ServiceMeta newServiceMeta;
                auto ret = fetchMeta(newServiceMeta);
                if (!ret) {
                    continue;
                }
                for (auto &c_itr : newServiceMeta.metaList) {
                      for (auto &endpoint : c_itr.endpoints) {
                          logger_->info("Update: endpoint.ip = {}, endpoint.port = {}", endpoint.ip, endpoint.port);
                      }
                }

                if (!needUpdate(this->sd_service_meta_, newServiceMeta)) {
                    continue;
                }
                CarreraConfig newConfig;
                updateCarreraConfig(newConfig, newServiceMeta, config_);

                auto newProducer = std::make_shared<CarreraProducer>(newConfig);

                newProducer->Start();

                this->sd_service_meta_ = newServiceMeta;

                updateProducer(newProducer);
            } catch (std::exception &e) {
                logger_->error("exception in CarreraSdProducer::UpdateServiceMeta, error = {}", e.what());
            } catch (...) {
                logger_->error("unknown exception in CarreraSdProducer::UpdateServiceMeta");
            }
        }
    }

    void CarreraSdProducer::updateCarreraConfig(CarreraConfig &newConfig, ServiceMeta &meta, CarreraConfig &userConfig) {
        newConfig.SetClientRetry(userConfig.GetClientRetry());
        newConfig.SetClientTimeOut(userConfig.GetClientTimeOut());
        newConfig.SetPoolSize(userConfig.GetPoolSize());
        newConfig.SetProxyTimeOut(userConfig.GetProxyTimeOut());
        newConfig.GetProxyList().clear();
        for (auto &c_itr : meta.metaList) {
            for (auto &endpoint : c_itr.endpoints) {
                newConfig.GetProxyList().push_back(endpoint.ip + ":" + std::to_string(endpoint.port));
            }
        }
        if (newConfig.GetProxyList().empty()) {
            newConfig.SetProxyList(userConfig.GetProxyList());
        }
    }

    bool CarreraSdProducer::needUpdate(const ServiceMeta &oldServiceMeta, const ServiceMeta &newServiceMeta) {
        return oldServiceMeta.metaVersion < newServiceMeta.metaVersion
               && oldServiceMeta.metaHash != newServiceMeta.metaHash;
    }

    Result CarreraSdProducer::Send(Message &msg) {
        std::lock_guard<std::mutex> g(mutex_producer_);
        checkTopic(msg.topic);
        return producer_->Send(msg);
    }

    Result CarreraSdProducer::Send(std::string &topic, std::string &body) {
        std::lock_guard<std::mutex> g(mutex_producer_);
        checkTopic(topic);
        return producer_->Send(topic, body);
    }

    Result CarreraSdProducer::SendBatchSync(std::vector<Message> &msgs) {
        std::lock_guard<std::mutex> g(mutex_producer_);
        checkTopic(msgs[0].topic);
        return producer_->SendBatchSync(msgs);
    }

    DelayResult CarreraSdProducer::SendDelayMessage(DelayMessage &delayMessage) {
        std::lock_guard<std::mutex> g(mutex_producer_);
        checkTopic(delayMessage.topic);
        return producer_->SendDelayMessage(delayMessage);
    }

    void CarreraSdProducer::updateProducer(std::shared_ptr<CarreraProducer> &producer) {
        std::lock_guard<std::mutex> g(mutex_producer_);
        this->producer_->ShutDown();
        this->producer_ = producer;
    }

    void CarreraSdProducer::checkTopic(std::string &topic) {
        if (this->topic_ != topic) {
            throw CarreraException("topic not match.");
        }
    }

    void start_sd_pull_thread(CarreraSdProducer &sdProducer) {
        sdProducer.UpdateServiceMeta();
    }

    /**
     * impl of AddDelayMessageBuilder
     * */
    AddDelayMessageBuilder::AddDelayMessageBuilder(CarreraProducer &producer) : pProducer(&producer), pSdProducer(nullptr) {
    }

    AddDelayMessageBuilder::AddDelayMessageBuilder(CarreraSdProducer &sdProducer) : pProducer(nullptr), pSdProducer(&sdProducer) {
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setTopic(const std::string &topic) {
        this->topic = topic;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setBody(const std::string &body) {
        this->body = body;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setDelayMeta(DelayMeta &delayMeta) {
        this->delayMeta = delayMeta;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setTags(const std::string &tag) {
        this->tags = tag;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::addProperty(const std::string &key, const std::string &value) {
        if (key != "" && key.compare(g_producerProxy_constants.PRESSURE_TRAFFIC_KEY)) {
            this->properties[key] = value;
        }
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::addHeader(const std::string &key, const std::string &value) {
        if (key != "") {
            if (boost::iequals(key, "didi-header-rid")) {
                this->setTraceId(value);
            } else if (boost::iequals(key, "didi-header-spanid")) {
                this->setSpanId(value);
            } else {
                if (!boost::iequals(key, "carrera_headers")) {
                    this->headers[key]= value;
                }
            }
        }
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setTraceId(const std::string &traceId) {
        this->properties[g_producerProxy_constants.TRACE_ID] = traceId;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setSpanId(const std::string &spanId) {
        this->properties[g_producerProxy_constants.SPAN_ID] = spanId;
        return *this;
    }

    AddDelayMessageBuilder &AddDelayMessageBuilder::setPressureTraffic(bool isOpen) {
        this->properties[g_producerProxy_constants.PRESSURE_TRAFFIC_KEY] =
                isOpen ? g_producerProxy_constants.PRESSURE_TRAFFIC_ENABLE
                       : g_producerProxy_constants.PRESSURE_TRAFFIC_DISABLE;
        return *this;
    }

    DelayResult AddDelayMessageBuilder::send() {
        if (!this->headers.empty()) {
            Json::Value root;
            for(std::map<std::string, std::string>::iterator iter=this->headers.begin(); iter != this->headers.end(); ++iter) {
                root[iter->first] = iter->second;
            }
            this->properties["carrera_headers"] = root.toStyledString();

        }
        if (!this->properties.empty()) {
            this->delayMeta.properties.insert(this->properties.begin(), this->properties.end());
        }

        uuid_t uuid;
        char uuid_[36];
        uuid_generate(uuid);
        uuid_unparse(uuid, uuid_);

        DelayMessage delayMessage;
        delayMessage.__set_topic(topic);
        delayMessage.__set_body(body);
        delayMessage.__set_action(ADD_ACTION);
        delayMessage.__set_timestamp(delayMeta.timestamp);
        delayMessage.__set_dmsgtype(delayMeta.dmsgtype);
        delayMessage.__set_interval(delayMeta.interval);
        delayMessage.__set_expire(delayMeta.expire);
        delayMessage.__set_times(delayMeta.times);
        delayMessage.__set_uuid(uuid_);
        delayMessage.__set_version(CARRERA_VERSION);

        if (!delayMeta.properties.empty()) {
            delayMessage.__set_properties(delayMeta.properties);
        }
        if (tags != "") {
            delayMessage.__set_tags(tags);
        }

        if (pProducer != nullptr) {
            return pProducer->SendDelayMessage(delayMessage);
        } else {
            return pSdProducer->SendDelayMessage(delayMessage);
        }
    }

    /**
     * impl of CancelDelayMessageBuilder
     * */
    CancelDelayMessageBuilder::CancelDelayMessageBuilder(CarreraProducer &producer) : pProducer(&producer), pSdProducer(nullptr) {
    }

    CancelDelayMessageBuilder::CancelDelayMessageBuilder(CarreraSdProducer &sdProducer) : pProducer(nullptr), pSdProducer(&sdProducer) {
    }

    CancelDelayMessageBuilder &CancelDelayMessageBuilder::setTopic(const std::string &topic) {
        this->topic = topic;
        return *this;
    }

    CancelDelayMessageBuilder &CancelDelayMessageBuilder::setUniqDelayMsgId(const std::string &uniqDelayMsgId) {
        this->uniqDelayMsgId = uniqDelayMsgId;
        return *this;
    }

    CancelDelayMessageBuilder &CancelDelayMessageBuilder::setTags(const std::string &tag) {
        this->tags = tag;
        return *this;
    }

    DelayResult CancelDelayMessageBuilder::send() {
        DelayMessage delayMessage;
        delayMessage.__set_topic(topic);
        delayMessage.__set_uniqDelayMsgId(uniqDelayMsgId);
        delayMessage.__set_action(CANCEL_ACTION);
        delayMessage.__set_version(CARRERA_VERSION);
        delayMessage.__set_body("c");

        if (tags != "") {
            delayMessage.__set_tags(tags);
        }

        if (pProducer != nullptr) {
            return pProducer->SendDelayMessage(delayMessage);
        } else {
            return pSdProducer->SendDelayMessage(delayMessage);
        }
    }
}

