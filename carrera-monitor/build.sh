#!/bin/bash


BASEDIR=$(dirname "$0")
cd ${BASEDIR}
WKDIR=`pwd`
cd ${BASEDIR}

mvn -U clean assembly:assembly -Dmaven.test.skip=true
ret=$?
if [ $ret -ne 0 ];then
    echo "===== maven build failure ====="
    exit $ret
else
    echo -n "===== maven build successfully! ====="
fi

OUTPATH=${WKDIR}/output
mkdir -p ${OUTPATH}
mkdir -p ${OUTPATH}/conf
mkdir -p ${OUTPATH}/target

cp control.sh ${OUTPATH}/
cp src/main/resources/monitor.yaml ${OUTPATH}/conf/
cp src/main/resources/logback.xml ${OUTPATH}/conf/
cp src/main/resources/notice.properties ${OUTPATH}/conf/
cp target/carrera-monitor-1.0.0-SNAPSHOT-jar-with-dependencies.jar ${OUTPATH}/target