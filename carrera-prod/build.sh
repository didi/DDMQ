#!/bin/bash

(cd ../carrera-common && mvn clean install -DskipTests)
(cd ../carrera-sdk/consumer/java/carrera-consumer-sdk && mvn clean install -DskipTests)
(cd ../carrera-sdk/producer/java/carrera-producer-sdk && mvn clean install -DskipTests)

../carrera-producer/build.sh && cp ../carrera-producer/target/carrera-producer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n01/producer/ && cp ../carrera-producer/target/carrera-producer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n02/producer/ && cp ../carrera-producer/target/carrera-producer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n03/producer/
../carrera-consumer/build.sh && cp ../carrera-consumer/target/carrera-consumer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n01/consumer/ && cp ../carrera-consumer/target/carrera-consumer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n02/consumer/ && cp ../carrera-consumer/target/carrera-consumer-1.0.0-SNAPSHOT-jar-with-dependencies.jar n03/consumer/
../carrera-chronos/build.sh && cp ../carrera-chronos/target/chronos-1.0.0-SNAPSHOT-jar-with-dependencies.jar n01/chronos/ && cp ../carrera-chronos/target/chronos-1.0.0-SNAPSHOT-jar-with-dependencies.jar n02/chronos/
../carrera-console/build.sh && cp ../carrera-console/carrera-console/target/carrera.war n01/console/ && cp ../carrera-console/carrera-console/target/carrera.war n02/console/ && cp ../carrera-console/carrera-console/target/carrera.war n03/console/
