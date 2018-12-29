#!/bin/bash

BASEDIR=$(dirname "$0")
CURDIR=`pwd`
cd ${BASEDIR}/..

mvn -Prelease-all -DskipTests clean install -U  
ret=$?
if [ $ret -ne 0 ];then
    echo "===== maven build failure ====="
    exit $ret
else
    echo -n "===== maven build successfully! ====="
fi

OUTPUT_PATH=${CURDIR}/output/
mkdir -p ${OUTPUT_PATH}
cp -r distribution/target/apache-rocketmq/* ${OUTPUT_PATH}
cp ${CURDIR}/control.sh ${OUTPUT_PATH}/control.sh

