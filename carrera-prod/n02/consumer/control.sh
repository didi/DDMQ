#!/bin/bash
MAINCLASS=com.xiaojukeji.carrera.cproxy.proxy.ConsumerProxyMain
CARRERA_VERSION="1.0.0-SNAPSHOT"
CONTROL_LOG="logs/control.log"

function start() {
    OLD_PID="`pgrep -f ${MAINCLASS}`"
    if [ "$OLD_PID" ]; then
        echo "Proxy is already running, pid=$OLD_PID. Stop it first!"
        exit 1
    fi
    
    BASEDIR=$(dirname "$0")
    cd "${BASEDIR}"
    PROXY_HOME=`pwd`
    mkdir -p logs/old

    CARRERA_CONFIG="${PROXY_HOME}/conf/carrera.yaml"

    # backup log
    LOG_NAMES=(carrera.log drop.log error.log main.log metric.log gc.log jstat.log)
    LOG_SUFFIX=$(date +%Y%m%d-%H%M%S)
    for var in ${LOG_NAMES[@]};
    do
        if [ -f "${PROXY_HOME}/logs/${var}" ]; then
            mv "${PROXY_HOME}/logs/${var}" "${PROXY_HOME}/logs/old/${var}.${LOG_SUFFIX}"
        fi
    done

    # set config
    LOG_BASE_CONSUMER_LEVEL="INFO"
    MEM_OPTS="-Xms512m -Xmx512m"
    LOG_SAVE_SIZE="1GB"
    LOG_SAVE_TIME="7d"

    export LOG_SAVE_SIZE
    export LOG_SAVE_TIME
    export LOG_BASE_CONSUMER_LEVEL

    echo "MEM_OPTS: " ${MEM_OPTS}
    echo "LOG_SAVE_SIZE="$LOG_SAVE_SIZE", LOG_SAVE_TIME="$LOG_SAVE_TIME", LOG_BASE_CONSUMER_LEVEL="$LOG_BASE_CONSUMER_LEVEL

    JVM_OPTS="${MEM_OPTS} ${JVM_OPTS} "
    JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+UseCMSInitiatingOccupancyOnly"
    JVM_OPTS="${JVM_OPTS} -XX:+PreserveFramePointer -XX:-UseBiasedLocking -XX:-OmitStackTraceInFastThrow"
    JVM_OPTS="${JVM_OPTS} -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${PROXY_HOME}/logs/gc.log"

    JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configurationFile=file://${PROXY_HOME}/conf/log4j2.xml"
    JAVA_OPTS="${JAVA_OPTS} -Drocketmq.client.log.loadconfig=false"
    JAVA_OPTS="${JAVA_OPTS} -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
    JAVA_OPTS="${JAVA_OPTS} -Drocketmq.client.maxTimeConsumeContinuously=1000"

    CLASSPATH="${PROXY_HOME}/carrera-consumer-${CARRERA_VERSION}-jar-with-dependencies.jar":$CLASSPATH

    CONSOLE_LOG=${PROXY_HOME}/logs/console.`date +%Y-%m-%d`.log
    date >> ${CONSOLE_LOG}

    java ${JVM_OPTS} ${JAVA_OPTS} -cp ${CLASSPATH} ${MAINCLASS} ${CARRERA_CONFIG} >> ${CONSOLE_LOG} 2>&1 &
    sleep 2
    date >> ${CONTROL_LOG}
    PID="`pgrep -f ${MAINCLASS}`"
    if [ "$PID" ]; then
        echo "New Proxy is running, pid=$PID" >> ${CONTROL_LOG}
        jstat -gcutil -t $PID 30s >> ${PROXY_HOME}/logs/jstat.log 2>&1 &
    else
        echo "Start Proxy Failed" >> ${CONTROL_LOG}
        exit 1
    fi
}

function stop() {
    date >> ${CONTROL_LOG}
    PID=$(pgrep -f ${MAINCLASS})
    echo "Killing Proxy =$PID" >> ${CONTROL_LOG}
    pkill -f "jstat -gcutil -t $PID 30s"
    pkill -f hangAlarm.sh
    pkill -15 -f ${MAINCLASS}
    t=0
    while [[ `pgrep -f ${MAINCLASS}` && "$t" -lt 120 ]]; do
        echo "time=$t,killing `pgrep -f ${MAINCLASS}`"
        t=$(($t+1))
        sleep 1
    done
    if [ `pgrep -f ${MAINCLASS}` ]; then
        echo "stop timeout"
        echo "Stop Proxy Failed" >> ${CONTROL_LOG}
        exit 1
    fi
    echo "KILLED" >> ${CONTROL_LOG}
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

