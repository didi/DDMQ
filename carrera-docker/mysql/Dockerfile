FROM mysql:5.7

ENV MYSQL_ALLOW_EMPTY_PASSWORD yes
ENV MYSQL_DATABASE=carrera_open_source
ENV MYSQL_ROOT_PASSWORD=123456

ADD ddmq.sql /docker-entrypoint-initdb.d

EXPOSE 3306
