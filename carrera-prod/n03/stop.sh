
sh /root/zookeeper/bin/zkServer.sh stop
cd /root/console && sh ./control.sh stop

cd /root/consumer && sh ./control.sh stop
cd /root/producer && sh ./control.sh stop