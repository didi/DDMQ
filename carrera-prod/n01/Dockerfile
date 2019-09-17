FROM centos:7

## Ports
EXPOSE 9613
EXPOSE 9713
EXPOSE 8080
EXPOSE 2181

# Env
ENV HOME_DIR /root
ENV HOME /root

WORKDIR ${HOME_DIR}

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' >/etc/timezone

RUN yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel unzip gettext nmap-ncat openssl wget telnet\
  && yum clean all -y

RUN curl http://mirror.bit.edu.cn/apache/tomcat/tomcat-9/v9.0.24/bin/apache-tomcat-9.0.24.zip -o tomcat.zip \
  && unzip tomcat.zip \ 
  && mv apache-tomcat-9.0.24 tomcat \
  && rm tomcat.zip \ 
  && chmod +x tomcat/bin/*.sh

RUN curl http://mirrors.hust.edu.cn/apache/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz -o zookeeper-3.4.10.tar.gz \
  && tar xzvf zookeeper-3.4.10.tar.gz \
  && mv zookeeper-3.4.10 zookeeper \
  && rm zookeeper-3.4.10.tar.gz \
  && cp zookeeper/conf/zoo_sample.cfg zookeeper/conf/zoo.cfg \
  && echo "dataDir=/root/zookeeper/data" >> zookeeper/conf/zoo.cfg \
  && echo "server.1=172.18.0.3:2888:3888" >> zookeeper/conf/zoo.cfg \
  && echo "server.2=172.18.0.4:2888:3888" >> zookeeper/conf/zoo.cfg \
  && echo "server.3=172.18.0.5:2888:3888" >> zookeeper/conf/zoo.cfg \
  && mkdir zookeeper/data \
  && echo '1' > zookeeper/data/myid

RUN mkdir -p \
 /root/logs/rocketmqlogs \
 /root/chronos-storage/rocksdb \
 /root/chronos-storage/seektimestamp \
 /root/chronos-storage/rocksdb_backup \
 /root/chronos-storage/rocksdb_restore

# copy
COPY console ${HOME_DIR}/console
COPY consumer ${HOME_DIR}/consumer
COPY producer ${HOME_DIR}/producer
COPY chronos ${HOME_DIR}/chronos
COPY broker ${HOME_DIR}/broker
COPY namesvr ${HOME_DIR}/namesvr
COPY start.sh ${HOME_DIR}/start.sh
COPY stop.sh ${HOME_DIR}/stop.sh

# cmd
CMD bash -C '/root/start.sh';'bash'
