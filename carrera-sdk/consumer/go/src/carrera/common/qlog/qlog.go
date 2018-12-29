package qlog

import (
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"path/filepath"
)

// Level type
type LogLevel uint8

// Convert the Level to a string. E.g. PanicLevel becomes "panic".
func (level LogLevel) String() string {
	switch level {
	case LEVEL_DEBUG:
		return "DEBUG"
	case LEVEL_INFO:
		return "INFO"
	case LEVEL_WARN:
		return "WARNING"
	case LEVEL_ERROR:
		return "ERROR"
	case LEVEL_FATAL:
		return "FATAL"
	case LEVEL_PANIC:
		return "PANIC"
	}

	return "INFO"
}

const (
	LEVEL_PANIC LogLevel = iota
	LEVEL_FATAL
	LEVEL_ERROR
	LEVEL_WARN
	LEVEL_INFO
	LEVEL_DEBUG
)

const (
	LOG_PATH = "./log/mq/"

	DROP_LOG_FILE_NAME = "drop.log"
)

var carreraLogPath = LOG_PATH
var DROP_LOGGER *log.DLogger

func initDropLogger(recoveryFileSizeGB int, level string, logPath string) {
	// create other logger first
	maxRecoverFileSize := 50 // MB
	maxRecoverFileBackups := 1024 * recoveryFileSizeGB / maxRecoverFileSize
	DROP_LOGGER = log.CreateLocalLog(filepath.Join(logPath, "drop/", DROP_LOG_FILE_NAME), level, maxRecoverFileSize, maxRecoverFileBackups, "text")
	DROP_LOGGER.SetDisableQuoting(true)
	DROP_LOGGER.SetDisableAutoAddKey(true)
}

/**
* logFileSizeGB: max log size in GB for general MQ SDK
* recoveryFileSizeGB: max file size to hold recovery data, which is used to store msg in local
* file when MQ service is broken
 */
func initLocalLog(logFileSizeGB int, level string, logPath string, fileName string) {
	// create the default log at last
	maxLogFileSize := 1024 // MB
	maxLogFileBackups := 1024 * logFileSizeGB / maxLogFileSize
	l := log.CreateLocalLog(filepath.Join(logPath, fileName), level, maxLogFileSize, maxLogFileBackups, "text")
	l.SetDisableQuoting(true)
	l.SetDisableAutoAddKey(true)
}

func InitLog(logFileSizeGB, recoveryFileSizeGB int) {
	initDropLogger(recoveryFileSizeGB, "INFO", carreraLogPath)
	initLocalLog(logFileSizeGB, "INFO", carreraLogPath, "producer.log")
}


func InitConsumerLog(logFileSizeGB int, level LogLevel, logPath string) {
	if len(logPath) == 0 {
		logPath = carreraLogPath
	} else {
		carreraLogPath = logPath
	}

	initLocalLog(logFileSizeGB, level.String(), logPath, "consumer.log")
}

func InitQLog(logFileSizeGB, recoveryFileSizeGB int, level LogLevel, logPath string) {
	if len(logPath) == 0 {
		logPath = carreraLogPath
	} else {
		carreraLogPath = logPath
	}

	initDropLogger(recoveryFileSizeGB, level.String(), logPath)
	initLocalLog(logFileSizeGB, level.String(), logPath, "producer.log")
}

func GetLogPath() string {
	return carreraLogPath
}
