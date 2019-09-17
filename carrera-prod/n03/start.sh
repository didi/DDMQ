
sh /root/zookeeper/bin/zkServer.sh start
cd /root/console && sh ./control.sh start

while ! nc -z localhost 8080; do
  sleep 0.1
done

curl http://localhost:8080/carrera/api/odin/internal/v4/initZkPath
curl http://localhost:8080/carrera/api/odin/internal/v4/initAllZk

cd /root/consumer && sh ./control.sh start
cd /root/producer && sh ./control.sh start