#!/bin/bash

MAIN_CLASS=rocketmq.namesrv.NamesrvStartup

function get_pid() {
    PID=$(ps ax | grep -i ${MAIN_CLASS} |grep java | grep -v grep | awk '{print $1}')
}

function start() {
    get_pid
    if [ ! -z "$PID" ] ; then
        echo "Shutdown old process($MAIN_CLASS) first, pid=${PID}"
        exit 1
    fi

    nohup sh bin/mqnamesrv >> ns.log 2>&1 &
    echo "running: nohup sh bin/mqnamesrv >> ns.log 2>&1 &"

    get_pid
    t=0
    while [[ -z ${PID} && "$t" -lt 10 ]]; do
        echo "time=$t,starting process($MAIN_CLASS)"
        get_pid
        t=$(($t+1))
        sleep 0.5
    done

    if [ -z ${PID} ]; then
        echo "start process($MAIN_CLASS) failed!!!"
        exit 1
    else
        echo "start process($MAIN_CLASS) success, pid=${PID}"
    fi
}

function stop() {
    get_pid
    if [ -z "$PID" ] ; then
        echo "no process running..."
    else
        echo "killing process($MAIN_CLASS), pid=${PID}"
        kill ${PID}
    fi

    t=0
    while [[ ! -z ${PID} && "$t" -lt 60 ]]; do
        echo "time=$t,killing process($MAIN_CLASS), pid=${PID}"
        get_pid
        t=$(($t+1))
        sleep 1
    done

    if [ ! -z ${PID} ]; then
        echo "killing process($MAIN_CLASS), pid=${PID}"
        exit 1
    fi
}

workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}

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
