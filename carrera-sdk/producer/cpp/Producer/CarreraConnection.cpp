#include "CarreraConnection.h"
#include "CarreraDefine.h"

#include <vector>
#include <boost/shared_ptr.hpp>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <protocol/TCompactProtocol.h>
#include <boost/algorithm/string.hpp>
#include <exception>
#include <iostream>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using std::string;
using boost::shared_ptr;


namespace CarreraProducer {

    void CarreraConnection::Close() {
        apache::thrift::concurrency::Guard g(conn_mutex_);
        if (this->thrift_client_ != NULL) {
            (this->thrift_client_)->getInputProtocol()->getTransport()->close();
        }
        (this->thrift_client_).reset();
    }

    boost::shared_ptr<ProducerServiceClient> CarreraConnection::CreateThriftClient() {
        std::vector<std::string> ipport;
        boost::split(ipport, this->proxy_addr_, boost::is_any_of(":"));

        if (ipport.size() != 2) {
            return shared_ptr<ProducerServiceClient>();
        }
        std::string ip = ipport[0];
        int port = atoi(ipport[1].c_str());

        boost::shared_ptr<TSocket> socket(new TSocket(ip, port));
        boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));

        socket->setConnTimeout(this->client_timeout_);
        socket->setRecvTimeout(this->client_timeout_);
        socket->setSendTimeout(this->client_timeout_);
        try {
            transport->open();
        } catch (TException &tx) {
            state_ = CONNECTION_UNHEALTHY;
            return boost::shared_ptr<ProducerServiceClient>();
        }

        boost::shared_ptr<TProtocol> protocol(new TCompactProtocol(transport));

        return boost::shared_ptr<ProducerServiceClient>(new ProducerServiceClient(protocol));
    }

    Result CarreraConnection::SendBatchSync(const std::vector<Message> &msgs) {
        Result ret;
        try {
            if (NULL == this->thrift_client_) {
                this->thrift_client_ = CreateThriftClient();
                if (NULL == this->thrift_client_) {
                    ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
                    ret.__set_msg("Illegal ipport or open transport failed.");
                    return ret;
                }
            }
            this->thrift_client_->sendBatchSync(ret, msgs);
        } catch (TException &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (std::exception &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (...) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg("Unkown exception.");
            (this->thrift_client_).reset();
        }
        return ret;
    }

    Result CarreraConnection::Send(const Message &msg, int64_t proxy_timeout) {
        Result ret;
        try {
            if (NULL == this->thrift_client_) {
                this->thrift_client_ = CreateThriftClient();
                if (NULL == this->thrift_client_) {
                    ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
                    ret.__set_msg("Illegal ipport or open transport failed.");
                    return ret;
                }
            }
            this->thrift_client_->sendSync(ret, msg, proxy_timeout);
        } catch (TException &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (std::exception &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (...) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg("Unkown exception.");
            (this->thrift_client_).reset();
        }
        if (ret.key.empty()) {
            ret.__set_key(msg.key);
        }
        return ret;
    }

     DelayResult CarreraConnection::Send(const DelayMessage &msg, int64_t proxy_timeout) {
        DelayResult ret;
        try {
            if (NULL == this->thrift_client_) {
                this->thrift_client_ = CreateThriftClient();
                if (NULL == this->thrift_client_) {
                    ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
                    ret.__set_msg("Illegal ipport or open transport failed.");
                    return ret;
                }
            }
            this->thrift_client_->sendDelaySync(ret, msg, proxy_timeout);
        } catch (TException &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (std::exception &e) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg(e.what());
            (this->thrift_client_).reset();
        } catch (...) {
            state_ = CONNECTION_UNHEALTHY;
            ret.__set_code(FAIL_SERVER_CONNECTION_ERROR);
            ret.__set_msg("Unkown exception.");
            (this->thrift_client_).reset();
        }
        return ret;
    }

}

