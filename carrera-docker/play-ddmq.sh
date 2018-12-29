# build mysql image
if [[ "$(docker images -q mysql:1.0 2> /dev/null)" == "" ]]; then
  docker build -t mysql:1.0 ./mysql
fi

# build ddmq image
if [[ "$(docker images -q ddmq:1.0 2> /dev/null)" == "" ]]; then
  docker build -t ddmq:1.0 .
fi

# run mysql container
echo 'start mysql container...'
docker run -d --rm --name mysql -p 127.0.0.1:3306:3306 -t mysql:1.0

# run ddmq container
echo 'start ddmq container.'
docker run -i --rm -t -p 127.0.0.1:8080:8080 -p 127.0.0.1:9613:9613 -p 127.0.0.1:9713:9713 --name ddmq --link mysql ddmq:1.0
