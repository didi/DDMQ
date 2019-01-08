#!/bin/bash

(cd ../carrera-common && mvn clean install -DskipTests)
(cd ../carrera-sdk/consumer/java/carrera-consumer-sdk && mvn clean install -DskipTests)
(cd ../carrera-sdk/producer/java/carrera-producer-sdk && mvn clean install -DskipTests)

../carrera-producer/build.sh && cp ../carrera-producer/target/carrera-producer-1.0.0-SNAPSHOT-jar-with-dependencies.jar producer/
../carrera-consumer/build.sh && cp ../carrera-consumer/target/carrera-consumer-1.0.0-SNAPSHOT-jar-with-dependencies.jar consumer/
../carrera-chronos/build.sh && cp ../carrera-chronos/target/chronos-1.0.0-SNAPSHOT-jar-with-dependencies.jar chronos/
../carrera-console/build.sh && cp ../carrera-console/carrera-console/target/carrera.war console/
