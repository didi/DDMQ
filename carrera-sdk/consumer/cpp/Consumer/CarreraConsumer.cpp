/**
 * carrera consumer thrift sdk - simple client 
 */
#include "CarreraConsumer.h"
#include "CarreraDefine.h"
#include "CarreraConfig.h"
#include "consumerProxy_constants.h"
#include <pthread.h>
#include <map>
#include <string>
#include <sstream>

#include <iostream>

#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <protocol/TCompactProtocol.h>
#include <boost/algorithm/string.hpp>
#include <thread>
#include <sys/time.h>

using std::string;
using std::vector;
using std::map;

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

namespace CarreraConsumer {

    // utility to split server list and ip&port
    std::vector <std::string> split(const std::string &text, char sep) {
        std::vector <std::string> tokens;
        std::size_t start = 0, end = 0;
        while ((end = text.find(sep, start)) != std::string::npos) {
            tokens.push_back(text.substr(start, end - start));
            start = end + 1;
        }
        tokens.push_back(text.substr(start));
        return tokens;
    }

    // should invoke stop() first
    CarreraConsumer::~CarreraConsumer() {
        if (!clientThreadMap.empty()) {
            for (std::map<pthread_t, SimpleCarreraClient *>::iterator it = clientThreadMap.begin();
                 it != clientThreadMap.end(); it++) {
                delete (it->second);
            }
            clientThreadMap.clear();
        }
    }

    CarreraConsumer::CarreraConsumer(std::string &_serverList, std::string &_groupName, ProcessorBase* _p,
                                 int _clientNumPerServer, int _retryInterval,
                                 int _submitMaxRetries, int _socketTimeout,
                                 int _maxLingerTime, int _maxBatchSize,
                                 const std::map<std::string, int> &_extraConcurrency) {
        if (_serverList.empty() || _groupName.empty()) {
            //std::cout << "CarreraConsumer::CarreraConsumer: Invalid params\n";
            mq_logger.Printf("Invalid params. [server list or group name]");
            return;
        }
        std::vector<std::string> sl = split(_serverList, ';');
        config_.SetServerList(sl);
        if (config_.GetServerList().empty()) {
            //std::cout << "CarreraConsumer::CarreraConsumer: serverList is empty\n";
            mq_logger.Printf("server list is empty.");
        }

        p = _p;
        config_.SetGroupName(_groupName);
        config_.SetClientNumPerServer(_clientNumPerServer);
        config_.SetRetryInterval(_retryInterval);
        config_.SetSocketTimeout(_socketTimeout);
        extraConcurrency = _extraConcurrency;
        config_.SetMaxBatchSize(_maxBatchSize);
        config_.SetMaxLingerTime(_maxLingerTime);
    }

     bool CarreraConsumer::validateParams() {
        if (config_.GetServerList().empty() || config_.GetGroupName().empty()) {
            //std::cout << __func__ << "： serverList/groupName is empty\n";
            mq_logger.Printf("%s: serverList/groupName is empty", __func__);
            return false;
        }
        if (config_.GetClientNumPerServer() == 0 && extraConcurrency.empty()) {
            mq_logger.Printf("%s: concurrency is 0", __func__);
            //std::cout << __func__ << "： concurrency is 0\n";
            return false;
        }
        return true;
    }

    bool CarreraConsumer::createSimpleClient(const std::string &host, int port, int clientNum, const std::string &topic) {
        if (clientNum == 0) {
            mq_logger.Printf("%s: clientNum is empty", __func__);
            //std::cout << __func__ << "： clientNum is empty\n";
            return true;
        }

        for (int i = 0; i < clientNum; i++) {
            SimpleCarreraClient *sc = new SimpleCarreraClient(host, port, config_.GetGroupName(), p, topic, config_.GetRetryInterval(),
                                                config_.GetSubmitMaxRetries(), config_.GetSocketTimeout(), config_.GetMaxLingerTime());
            if (sc == NULL) {
                mq_logger.Printf("%s: sc is nil", __func__);
                //std::cout << __func__ << "： sc is nil\n";
                return false;
            }

            pthread_t tid;
            int err = pthread_create(&tid, NULL, &SimpleCarreraClient::startConsume, (void *)sc);
            if (err != 0) {
                mq_logger.Printf("%s: failed to create thread", __func__);
                //std::cout << __func__ << ": failed to create thread\n";
                delete sc;
                return false;
            }
            clientThreadMap[tid] = sc;
            //pthread_join(tid, NULL);
        }
        //std::cout << __func__ << "： create simple client success\n";
        return true;
    }

    void CarreraConsumer::stop() {
        if (!clientThreadMap.empty()) {
            for (std::map<pthread_t, SimpleCarreraClient *>::iterator it = clientThreadMap.begin();
                 it != clientThreadMap.end(); it++) {
                if (!it->second->isStopped()) {
                    it->second->stop(); // will try to stop the client
                }
            }
        }
    }

    void CarreraConsumer::waitFinish() {
        if (!clientThreadMap.empty()) {
            for (std::map<pthread_t, SimpleCarreraClient *>::iterator it = clientThreadMap.begin();
                 it != clientThreadMap.end(); it++) {
                if (!it->second->isStopped()) {
                    pthread_join(it->first, NULL); // wait until the thread is finished
                }
            }
        }
    }

    // can only invoke once for each carrera client
    void CarreraConsumer::startConsume() {
        if (!validateParams() || p == NULL) {
            mq_logger.Printf("%s: failed to validate params", __func__);
            //std::cout << __func__ << ": failed to validate params\n";
            return;
        }
        int totalConcurrencyPerServer = config_.GetClientNumPerServer();
        if (!extraConcurrency.empty()) {
            for (std::map<std::string, int>::iterator it = extraConcurrency.begin();
                 it != extraConcurrency.end(); it++) {
                totalConcurrencyPerServer += it->second;
            }
        }
        if (totalConcurrencyPerServer == 0) {
            mq_logger.Printf("%s: totalConcurrencyPerServer is 0", __func__);
            //std::cout << __func__ << ": totalConcurrencyPerServer is 0\n";
            return;
        }

        // create simple client
        bool status = false;        
        for (std::vector<std::string>::iterator it = config_.GetServerList().begin(); it != config_.GetServerList().end(); it++) {
            std::vector <std::string> hostAndPort = split(*it, ':');
            if (hostAndPort.size() != 2) {
                mq_logger.Printf("%s: server name[%s] is invalid.", __func__, (*it).c_str());
                //std::cout << __func__ << ": server name is invalid, " << *it << "\n";
                return;
            }

            int port;
            std::stringstream ss;
            ss << hostAndPort[1];
            ss >> port;
            if (ss.fail()) {
                mq_logger.Printf("%s: port is invalid[%s]", __func__, hostAndPort[1].c_str());
                //std::cout << __func__ << ": port is invalid: " << hostAndPort[1] << "\n";
                return;
            }
            
            status = createSimpleClient(hostAndPort[0], port, config_.GetClientNumPerServer());
            if (!status) {
                mq_logger.Printf("%s: failed to createSimpleClient", __func__);
                //std::cout << __func__ << ": failed to createSimpleClient\n";
                // cout<<....
                stop();
                return;
            }
            if (!extraConcurrency.empty()) {
                for (std::map<std::string, int>::iterator it = extraConcurrency.begin();
                     it != extraConcurrency.end(); it++) {
                    status = createSimpleClient(hostAndPort[0], port, it->second, it->first);
                    if (!status) {
                        //std::cout << __func__ << ": failed to createSimpleClient\n";
                        mq_logger.Printf("%s: failed to createSimpleClient", __func__);
                        stop();
                        return;
                    }
                }
            }

        }
    }
    void CarreraSdCconsumer::StartConsume() noexcept(false) {
        apache::thrift::concurrency::Guard g(mutex_);
        buildClientMeta();
        auto ret = fetchMeta(this->sd_service_meta_);

        if (!ret && !config_.isset_.serverList_) {
            throw CarreraException("Fetch Meta failed and config.serverList is empty");
        }

        CarreraConfig newConfig;
        updateCarreraConfig(newConfig, this->sd_service_meta_, config_);
        bg_thread = std::thread(start_sd_pull_thread, std::ref(*this));
        this->client_ = std::make_shared<CarreraConsumer>(newConfig, p);

        client_->startConsume();
        state = STATE_START;
    }

    void CarreraSdCconsumer::Stop() {
        apache::thrift::concurrency::Guard g(mutex_);
        if (state != STATE_START) {
            throw CarreraException("state is not STATE_START");
        }

        {
            std::lock_guard<std::mutex> lock(bg_mutex);
            state = STATE_STOP;
        }

        client_->stop();
        bg_cond.notify_one();
        if (bg_thread.joinable()) {
            bg_thread.join();
        }
    }

    std::string join(std::vector<std::string> &v, const std::string &t){
          std::string result;
          std::vector<std::string>::iterator it;
          for (it = v.begin(); it != v.end(); it++) {
              if (!result.empty())
                  result.append(t);
             result.append(*it);
         }
         return result;
   }

   void CarreraSdCconsumer::buildClientMeta() {
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

        sd_client_meta_.config["serverList_"].userDefined = config_.isset_.serverList_;
        sd_client_meta_.config["serverList_"].value = join(config_.GetServerList(), ";");
        sd_client_meta_.config["groupName_"].userDefined = config_.isset_.groupName_;
        sd_client_meta_.config["groupName_"].value = config_.GetGroupName();
        sd_client_meta_.config["clientNumPerServer_"].userDefined = config_.isset_.clientNumPerServer_;
        sd_client_meta_.config["clientNumPerServer_"].value = std::to_string(config_.GetClientNumPerServer());
        sd_client_meta_.config["retryInterval_"].userDefined = config_.isset_.retryInterval_;
        sd_client_meta_.config["retryInterval_"].value = std::to_string(config_.GetRetryInterval());
        sd_client_meta_.config["submitMaxRetries_"].userDefined = config_.isset_.submitMaxRetries_;
        sd_client_meta_.config["submitMaxRetries_"].value = std::to_string(config_.GetSubmitMaxRetries());
        sd_client_meta_.config["socketTimeout_"].userDefined = config_.isset_.socketTimeout_;
        sd_client_meta_.config["socketTimeout_"].value = std::to_string(config_.GetSocketTimeout());
        sd_client_meta_.config["maxLingerTime_"].userDefined = config_.isset_.maxLingerTime_;
        sd_client_meta_.config["maxLingerTime_"].value = std::to_string(config_.GetMaxLingerTime());
        sd_client_meta_.config["maxBatchSize_"].userDefined = config_.isset_.maxBatchSize_;
        sd_client_meta_.config["maxBatchSize_"].value = std::to_string(config_.GetMaxBatchSize());
    }

    std::shared_ptr<ServiceDiscoveryServiceClient> CarreraSdCconsumer::createSdClient() {
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

        transport->open(); //This may throws exceptions.

        boost::shared_ptr<TProtocol> protocol(new TCompactProtocol(transport));

        return std::shared_ptr<ServiceDiscoveryServiceClient>(new ServiceDiscoveryServiceClient(protocol));
    }

    bool CarreraSdCconsumer::fetchMeta(ServiceMeta &meta) {
        int retries = 3;
        while (retries-- > 0) {
            try {
                if (NULL == this->sd_client_) {
                    this->sd_client_ = createSdClient();
                    if (NULL == this->sd_client_) {
                        return false;
                    }
                }
                this->sd_client_->discoverConsumerService(meta, groupName_, sd_client_meta_);             

                return true;
            } catch (TException &e) {
                logger_->error("fetchMeta failed: {}", e.what());
            }
        }
        return false;
    }

   void CarreraSdCconsumer::UpdateServiceMeta() {
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

                auto newConsumer = std::make_shared<CarreraConsumer>(newConfig,p);
                this->client_->stop();
                newConsumer->startConsume();
                this->sd_service_meta_ = newServiceMeta;

                updateClient(newConsumer);

            } catch (std::exception &e) {
                logger_->error("exception in CarreraSdCconsumer::UpdateServiceMeta, error = {}", e.what());
            } catch (...) {
                logger_->error("unknown exception in CarreraSdCconsumer::UpdateServiceMeta");
            }
        }
    }

   void CarreraSdCconsumer::updateCarreraConfig(CarreraConfig &newConfig, ServiceMeta &meta, CarreraConfig &userConfig) {
        newConfig.SetGroupName(userConfig.GetGroupName());
        newConfig.SetClientNumPerServer(userConfig.GetClientNumPerServer());
        newConfig.SetRetryInterval(userConfig.GetRetryInterval());
        newConfig.SetSubmitMaxRetries(userConfig.GetSubmitMaxRetries());
        newConfig.SetSocketTimeout(userConfig.GetSocketTimeout());
        newConfig.SetMaxLingerTime(userConfig.GetMaxLingerTime());
        newConfig.SetMaxBatchSize(userConfig.GetMaxBatchSize());
        newConfig.GetServerList().clear();
        for (auto &c_itr : meta.metaList) {
            for (auto &endpoint : c_itr.endpoints) {
                newConfig.GetServerList().push_back(endpoint.ip + ":" + std::to_string(endpoint.port));
            }
        }
        if (newConfig.GetServerList().empty()) {
            newConfig.SetServerList(userConfig.GetServerList());
        }
   }

    bool CarreraSdCconsumer::needUpdate(const ServiceMeta &oldServiceMeta, const ServiceMeta &newServiceMeta) {
            return oldServiceMeta.metaVersion < newServiceMeta.metaVersion
                   && oldServiceMeta.metaHash != newServiceMeta.metaHash;
    }
    void CarreraSdCconsumer::updateClient(std::shared_ptr<CarreraConsumer> &client) {
        std::lock_guard<std::mutex> g(mutex_client_);
        this->client_ = client;
    }
    void start_sd_pull_thread(CarreraSdCconsumer &sdClient) {
        sdClient.UpdateServiceMeta();
    }

} // namespace

