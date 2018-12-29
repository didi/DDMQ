#ifndef CARRERA_CONSUMER_CONFIG_H
#define CARRERA_CONSUMER_CONFIG_H

#include <vector>
#include <string>
#include <set>

/*******************Default Config***********************/
#define CONSUMER_DEFAULT_VERSION_UID 3661212922184986125L
#define CONSUMER_DEFAULT_TIMEOUT 5000 /* ms */
#define CONSUMER_DEFAULT_MIN_RETRY_INTERVAL 5 /* ms */
#define CONSUMER_DEFAULT_SUBMIT_MAX_RETRY 3
#define CONSUMER_DEFAULT_MAX_BATCH_SIZE 8
#define CONSUMER_DEFAULT_MAX_LINGER_TIME 50 /* ms */
#define CONSUMER_DEFAULT_AUTO_ACK_INTERVAL 0 /* ms */

namespace CarreraConsumer {

class ConsumerConfig {
  public:
    ConsumerConfig();
    ~ConsumerConfig() {
    }
    
    int SetCluster(const char *cluster);
    int SetGroup(const char *group);
    int SetTimeout(int timeout);
    int SetRetryInterval(int interval);
    int SetSubmitMaxRetry(int max_retry);
    int SetMaxBatchSize(int max_batch);
    int SetMaxLingerTime(int max_linger);
    int SetAutoAckInterval(int auto_ack_interval);
    int SetConnPerServer(int conn_num);
    int SetLogger(const char *conf_file);
    int AddServer(const std::string &server); 
    
    // Invoked after all settings finished.
    int Validate() const;
    std::string GetCluster() const { return cluster_; }
    std::string GetGroup() const { return group_; }
    int GetTimeout() const { return timeout_; }
    int GetRetryInterval() const { return retry_interval_; }
    int GetSubmitMaxRetry() const { return submit_max_retry_; }
    int GetMaxBatchSize() const { return max_batch_size_; }
    int GetMaxLingerTime() const { return max_linger_time_; }
    int GetAutoAckInterval() const { return auto_ack_interval_; }
    long GetSerialVersionUid() const { return serial_version_uid_; }
    int GetConnPerServer() const { return conn_per_server_; }
    int GetServerNum() const { return servers_.size(); } 
    void GetServerIpPort(std::vector<std::string> &ip, std::vector<int> &port) const;


  private:
    std::set<std::string> servers_; // required, [<ip:port>...]
    std::string cluster_; // required
    std::string group_; // required
    int conn_per_server_;
    int timeout_;
    int retry_interval_;
    int submit_max_retry_;
    int max_batch_size_;
    int max_linger_time_;
    int auto_ack_interval_;
    const long serial_version_uid_;
};
} // namespace CarreraConsumer

#endif // CARRERA_CONSUMER_CONFIG_H

