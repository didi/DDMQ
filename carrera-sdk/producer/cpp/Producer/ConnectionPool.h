#ifndef CARRERA_CONNECTION_POOL_H
#define CARRERA_CONNECTION_POOL_H

#include <list>
#include <exception>
#include <boost/shared_ptr.hpp>
#include <pthread.h>
#include <map>

using boost::shared_ptr;


namespace CarreraProducer{

template<class T> class ConnectionPool{
public:

    ConnectionPool(){
        overload_iter_ = conn_map_.begin();
        pthread_mutex_init(&mutex_, NULL);
    }

    void AddConnection(const std::string& ipport, boost::shared_ptr<T> conn){
        conn_map_[ipport].push_back(conn);
    }

    void FreeConnection(){
        typename std::map<std::string, std::list<boost::shared_ptr<T> > >::iterator s_iter;
        typename std::list<boost::shared_ptr<T> >::iterator c_iter;
        for(s_iter = conn_map_.begin(); s_iter != conn_map_.end(); ++s_iter){
            for(c_iter = (s_iter->second).begin(); c_iter != (s_iter->second).end(); ++c_iter){
                (*c_iter)->Close();
            }
        }
        conn_map_.clear();
        risk_avoid_.clear();
    }

    boost::shared_ptr<T> FetchConnection(){
        pthread_mutex_lock(&mutex_);   
        pthread_t pid = pthread_self();
        boost::shared_ptr<T> conn;
        for(size_t i=0; i<conn_map_.size(); ++i){
            if(conn_map_.end() == overload_iter_){
               overload_iter_ = conn_map_.begin();
            }
            if((overload_iter_->second).empty()){
               overload_iter_++;
            }else if(risk_avoid_.find(pid) != risk_avoid_.end() && overload_iter_->first == risk_avoid_[pid] && conn_map_.size()>1){
               overload_iter_++;
            }else{
               conn = (overload_iter_->second).front();
               (overload_iter_->second).pop_front();
               overload_iter_++;
               break;
            }
        }

        pthread_mutex_unlock(&mutex_);
        return conn; 
    }

    void ReleaseConnection(boost::shared_ptr<T> conn){
        pthread_mutex_lock(&mutex_);
        pthread_t tid = pthread_self();
        if(conn->GetState() != 0){
            risk_avoid_[tid] = conn->GetProxyAddr();
        }else{
            risk_avoid_.erase(tid);
        }
        conn_map_[conn->GetProxyAddr()].push_back(conn);
        pthread_mutex_unlock(&mutex_);
    }

private:
    pthread_mutex_t mutex_;
    std::map<std::string, std::list<boost::shared_ptr<T> > > conn_map_;
    typename std::map<std::string, std::list<boost::shared_ptr<T> > >::iterator overload_iter_;
    std::map<pthread_t, std::string> risk_avoid_;
};


}


#endif
