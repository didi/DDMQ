#!/bin/bash

MAIN_CLASS=com.xiaojukeji.chronos.ChronosMain
CHRONOS_VERSION="1.0.0-SNAPSHOT"

function start() {
    OLD_PID="`pgrep -f ${MAIN_CLASS}`"
    if [ "$OLD_PID" ]; then
        echo "Chronos is already running, pid=$OLD_PID. Stop it first!"
        exit 1
    fi

    BASEDIR=$(dirname "$0")
    cd "${BASEDIR}"
    CHRONOS_HOME=`pwd`
    CHRONOS_LOG=${CHRONOS_HOME}/logs
    mkdir -p ${CHRONOS_LOG}/old

    # set config
    LOG_CARRERA_LEVEL="INFO"
    CHRONOS_CONFIG="${CHRONOS_HOME}/conf/chronos.yaml"
    LOG_SAVE_SIZE="50G"
    LOG_SAVE_TIME="7d"
    MEM_OPTS="-Xms4G -Xmx4G -Xmn512m"

    export LOG_SAVE_SIZE
    export LOG_SAVE_TIME
    export LOG_CARRERA_LEVEL
    echo "MEM_OPTS:"${MEM_OPTS}
    echo "JAVA_OPTS:"${JAVA_OPTS}
    echo "LOG_SAVE_SIZE:"${LOG_SAVE_SIZE}", LOG_SAVE_TIME:"${LOG_SAVE_TIME}", LOG_CARRERA_LEVEL:"${LOG_CARRERA_LEVEL}

    if [ ! -f ${CHRONOS_CONFIG} ]; then
        date >> ${CHRONOS_LOG}/control.log
        echo "configure is not existed!" >> ${CHRONOS_LOG}/control.log
        exit 1
    fi
    echo "Use ${CHRONOS_CONFIG} configure"

    # backup log
    LOG_NAMES=(carrera.log error.log metric.log gc.log jstat.log)
    LOG_SUFFIX=$(date +%Y%m%d-%H%M%S)
    for var in ${LOG_NAMES[@]};
    do
        if [ -f "${CHRONOS_LOG}/${var}" ]; then
            mv "${CHRONOS_LOG}/${var}" "${CHRONOS_LOG}/old/${var}.${LOG_SUFFIX}"
        fi
    done

    JVM_OPTS="${MEM_OPTS} ${JVM_OPTS} "
    JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+UseCMSInitiatingOccupancyOnly"
    JVM_OPTS="${JVM_OPTS} -XX:+PreserveFramePointer -XX:-UseBiasedLocking -XX:-OmitStackTraceInFastThrow"
    JVM_OPTS="${JVM_OPTS} -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${CHRONOS_LOG}/gc.log"
    JVM_OPTS="${JVM_OPTS} -XX:+PrintSafepointStatistics"
    CLASSPATH="${CHRONOS_HOME}/chronos-${CHRONOS_VERSION}-jar-with-dependencies.jar":${CLASSPATH}

    JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configurationFile=file://${CHRONOS_HOME}/conf/log4j2.xml"
    JAVA_OPTS="${JAVA_OPTS} -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"

    CONSOLE_LOG=${CHRONOS_LOG}/console.`date +%Y-%m-%d`.log
    date >>  ${CONSOLE_LOG}

    java ${JVM_OPTS} ${JAVA_OPTS} -cp ${CLASSPATH} ${MAIN_CLASS} ${CHRONOS_CONFIG} >> ${CONSOLE_LOG} 2>&1 &
    sleep 2
    date >> ${CHRONOS_LOG}/control.log
    PID="`pgrep -f ${MAIN_CLASS}`"
    if [ "$PID" ]; then
        echo "NEW Chronos is running, pid=$PID"
        echo "New Chronos is running, pid=$PID" >> ${CHRONOS_LOG}/control.log
        jstat -gcutil -t $PID 30s >> ${CHRONOS_LOG}/gstat.log &
    else
        echo "Start Chronos Failed"
        echo "Start Chronos Failed" >> ${CHRONOS_LOG}/control.log
        exit 1
    fi
}

function stop() {
    BASEDIR=$(dirname "$0")
    cd "${BASEDIR}"
    CHRONOS_HOME=`pwd`
    CHRONOS_LOG=${CHRONOS_HOME}/logs
    mkdir -p ${CHRONOS_LOG}

    date >> ${CHRONOS_LOG}/control.log
    echo "Killing Chronos =`pgrep -f ${MAIN_CLASS}`" >> ${CHRONOS_LOG}/control.log
    kill -15 `pgrep -f ${MAIN_CLASS}`
    t=0
    while [[ `pgrep -f ${MAIN_CLASS}` && "$t" -lt 60 ]]; do
        echo "time=$t,killing `pgrep -f ${MAIN_CLASS}`"
        t=$(($t+1))
        sleep 1
    done
    if [ `pgrep -f ${MAIN_CLASS}` ]; then
        echo "stop timeout"
        echo "Stop Chronos Failed" >> ${CHRONOS_LOG}/control.log
        exit 1
    fi
    echo "KILLED" >> ${CHRONOS_LOG}/control.log
}

case "$1" in
    "start")
        start
        ;;
    "stop")
        stop
        ;;
    "reload")
        stop
        start
        ;;
    *)
        echo "supporting cmd: start/stop/reload"
        ;;
esac