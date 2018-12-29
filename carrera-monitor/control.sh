#!/bin/bash
MAINCLASS=com.xiaojukeji.carrera.monitor.app.CarreraMonitor
CARRERA_VERSION="1.0.0-SNAPSHOT"
function start() {
    OLD_PID="`pgrep -f ${MAINCLASS}`"
    if [ "$OLD_PID" ]; then
        echo "Monitor is already running, pid=$OLD_PID. Stop it first!"
        exit 1
    fi

    BASEDIR=$(dirname "$0")
    cd "${BASEDIR}"
    MONITOR_HOME=`pwd`
    mkdir -p logs
    MONITOR_CONFIG="${MONITOR_HOME}/conf/monitor.yaml"
    CLASSPATH="${MONITOR_HOME}/target/carrera-monitor-${CARRERA_VERSION}-jar-with-dependencies.jar":"${MONITOR_HOME}/conf":$CLASSPATH
    date >> ${MONITOR_HOME}/logs/console_out.log

    LOG_OPTS="-Dlogback.configurationFile=${MONITOR_HOME}/conf/logback.xml"
    RMQ_OPTS="-Drocketmq.client.maxTimeConsumeContinuously=1000"
    GC_FILE=${MONITOR_HOME}/logs/gc.log
    JSTAT_FILE=${MONITOR_HOME}/logs/jstat.log

    JVM_OPTS="-Xms4G -Xmx4G -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+UseCMSInitiatingOccupancyOnly -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:-OmitStackTraceInFastThrow -Xloggc:${GC_FILE}"

    if [ -f ${GC_FILE} ]; then
        echo "back up... gc.log"
        mv ${GC_FILE} ${MONITOR_HOME}/logs/gc.old.log
    fi

    if [ -f ${JSTAT_FILE} ]; then
        echo "back up... jstat.log"
        mv ${JSTAT_FILE} ${MONITOR_HOME}/logs/jstat.old.log
    fi

    java $LOG_OPTS $RMQ_OPTS $JVM_OPTS  -cp $CLASSPATH $MAINCLASS $MONITOR_CONFIG >> ${MONITOR_HOME}/logs/console_out.log 2>&1 &
    sleep 2
    date >> logs/control.log
    PID="`pgrep -f ${MAINCLASS}`"
    if [ "$PID" ]; then
        echo "Monitor is running, pid=$PID" >> logs/control.log
        jstat -gcutil -t $PID 30s >> ${JSTAT_FILE} &
    else
        echo "Start Monitor Failed" >> logs/control.log
        exit 1
    fi
}

function stop() {
    date >> logs/control.log
    echo "Killing Monitor =`pgrep -f ${MAINCLASS}`" >> logs/control.log
    pkill -15 -f ${MAINCLASS}
    t=0
    while [[ `pgrep -f ${MAINCLASS}` && "$t" -lt 60 ]]; do
        echo "time=$t,killing `pgrep -f ${MAINCLASS}`"
        t=$(($t+1))
        sleep 1
    done
    if [ `pgrep -f ${MAINCLASS}` ]; then
        echo "stop timeout"
        echo "Stop Proxy Failed" >> control.log
        exit 1
    fi
    echo "KILLED" >> control.log
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
