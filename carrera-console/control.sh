#!/bin/bash

WEBAPPS="/home/xiaoju/tomcat/webapps"
TOMCATBIN="/home/xiaoju/tomcat/bin"

function start() {
	OLD_PID="`pgrep -f org.apache.catalina.startup.Bootstrap`"
    if [ "$OLD_PID" ]; then
        echo "tomcat is already running, pid=$OLD_PID. Stop it first!" >> control.log
        exit 1
    fi

    rm -rf $WEBAPPS/carrera
    rm $WEBAPPS/carrera-console.war
    cp carrera-console.war $WEBAPPS
    $TOMCATBIN/startup.sh
    echo "tomcat started" >> control.log
}

function stop() {
	if [[ `pgrep -f org.apache.catalina.startup.Bootstrap` ]]; then
		pkill -9 -f org.apache.catalina.startup.Bootstrap
	fi
	t=0
	while [[ `pgrep -f org.apache.catalina.startup.Bootstrap` && "$t" -lt 30 ]]; do
		sleep 1
	done
	if [[ `pgrep -f org.apache.catalina.startup.Bootstrap` ]]; then
		echo "stop tomcat failed" >> control.log
		exit 1
	fi
	echo "tomcat stopped" >> control.log
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