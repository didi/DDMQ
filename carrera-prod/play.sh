# build mysql image
#if [[ "$(docker images -q mysql:1.1 2> /dev/null)" == "" ]]; then
#  docker build -t mysql:1.1 ./mysql
#fi

# build ddmq image
if [[ "$(docker images -q ddmq:n01 2> /dev/null)" == "" ]]; then
  docker build -t ddmq:n01 ./n01
fi

# build ddmq image
if [[ "$(docker images -q ddmq:n02 2> /dev/null)" == "" ]]; then
  docker build -t ddmq:n02 ./n02
fi

# build ddmq image
if [[ "$(docker images -q ddmq:n03 2> /dev/null)" == "" ]]; then
  docker build -t ddmq:n03 ./n03
fi

# run mysql container
#echo 'start mysql container...'
#docker run -td --rm --name mysql --network ddmq-net --ip 172.18.0.2 -p 127.0.0.1:3307:3306 mysql:1.1

# run ddmq container
echo 'start ddmq container n01.'
docker run -td --rm --name ddmqn01 --network ddmq-net --ip 172.18.0.3 -p 127.0.0.1:8080:8080 -p 127.0.0.1:9613:9613 -p 127.0.0.1:9713:9713 -v /usr/local/var/rocketmq:/root/rocketmq -v /usr/local/var/log/ddmq/n01:/root/logs --add-host=mysql:192.168.11.247 ddmq:n01

# run ddmq container
echo 'start ddmq container n02.'
docker run -td --rm --name ddmqn02 --network ddmq-net --ip 172.18.0.4 -p 127.0.0.1:8081:8080 -p 127.0.0.1:9614:9613 -p 127.0.0.1:9714:9713 -v /usr/local/var/rocketmq:/root/rocketmq -v /usr/local/var/log/ddmq/n02:/root/logs --add-host=mysql:192.168.11.247 ddmq:n02

# run ddmq container
echo 'start ddmq container n03.'
docker run -td --rm --name ddmqn03 --network ddmq-net --ip 172.18.0.5 -p 127.0.0.1:8082:8080 -p 127.0.0.1:9615:9613 -p 127.0.0.1:9715:9713 --add-host=mysql:192.168.11.247 ddmq:n03
