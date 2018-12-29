package dlog

import (
	"fmt"
	"testing"
	"time"
)

func InfoHelperDepth(format string, args ...interface{}) {
	LogDepth(INFO, 0, format, args...)
}

func InfoHelper(format string, args ...interface{}) {
	Infof(format, args...)
}

func TestStdout(t *testing.T) {
	Info("stdout")
}

func TestDefaultFileBackend(t *testing.T) {
	var conf LogConfig
	conf.Type = "file"
	conf.Level = "DEBUG"
	conf.FileName = "/tmp/dlog-test/defaultFileBackend"
	conf.FileRotateSize = 1024 * 1024 * 1024
	conf.FileRotateCount = 20
	//conf.FileFlushDuration = time.Second * 1

	Init(conf)
	Info("Info")
	Infof("%s", "Infof")
	Warning("Warning")
	Warningf("%s", "Warningf")
	Debug("Debug")
	Debugf("%s", "Debugf")
	Error("Error")
	Errorf("%s", "Errorf")
	Close()
}

func TestFileBackend(t *testing.T) {
	var conf LogConfig
	conf.Type = "file"
	conf.Level = "DEBUG"
	conf.FileName = "/tmp/dlog-test/fileBackend"
	conf.FileRotateSize = 1024 * 1024 * 1024
	conf.FileRotateCount = 20
	//conf.FileFlushDuration = time.Second * 1

	log, err := NewLoggerFromConfig(conf)
	if err != nil {
		fmt.Println("err when call NewLoggerFromConfig: [%s]", err.Error())
		return
	}

	log.Info("Info")
	log.Infof("%s", "Infof")
	log.Warning("Warning")
	log.Warningf("%s", "Warningf")
	log.Debug("Debug")
	log.Debugf("%s", "Debugf")
	log.Error("Error")
	log.Errorf("%s", "Errorf")
	log.Close()
}

func TestBothFileBackend(t *testing.T) {
	var defaultConf LogConfig
	defaultConf.Type = "file"
	defaultConf.Level = "DEBUG"
	defaultConf.FileName = "/tmp/dlog-test/bothFileBackend/defaultFile"
	defaultConf.FileRotateSize = 1024 * 1024 * 1024
	defaultConf.FileRotateCount = 20
	Init(defaultConf)

	var conf1 LogConfig
	conf1.Type = "file"
	conf1.Level = "DEBUG"
	conf1.FileName = "/tmp/dlog-test/bothFileBackend/file1"
	conf1.FileRotateSize = 1024 * 1024 * 1024
	conf1.FileRotateCount = 20
	log1, err := NewLoggerFromConfig(conf1)
	if err != nil {
		fmt.Println("err when call NewLoggerFromConfig: [%s]", err.Error())
		return
	}

	var conf2 LogConfig
	conf2.Type = "file"
	conf2.Level = "DEBUG"
	conf2.FileName = "/tmp/dlog-test/bothFileBackend/file2"
	conf2.FileRotateSize = 1024 * 1024 * 1024
	conf2.FileRotateCount = 20
	log2, err := NewLoggerFromConfig(conf2)
	if err != nil {
		fmt.Println("err when call NewLoggerFromConfig: [%s]", err.Error())
		return
	}

	Info("Info")
	Infof("%s", "Infof")
	log1.Info("Info")
	log1.Infof("%s", "Infof")
	log2.Info("Info")
	log2.Infof("%s", "Infof")

	Warning("Warning")
	Warningf("%s", "Warningf")
	log1.Warning("Warning")
	log1.Warningf("%s", "Warningf")
	log2.Warning("Warning")
	log2.Warningf("%s", "Warningf")

	Debug("Debug")
	Debugf("%s", "Debugf")
	log1.Debug("Debug")
	log1.Debugf("%s", "Debugf")
	log2.Debug("Debug")
	log2.Debugf("%s", "Debugf")

	Error("Error")
	Errorf("%s", "Errorf")
	log1.Error("Error")
	log1.Errorf("%s", "Errorf")
	log2.Error("Error")
	log2.Errorf("%s", "Errorf")

	Close()
	log1.Close()
	log2.Close()
}

func TestSysLogBackend(t *testing.T) {
	b, err := NewSyslogBackend("local3", "mohawk")
	if err != nil {
		panic(err)
	}
	defer Close()
	SetLogging("DEBUG", b)
	Debug("Debug")
	Errorf("%d %s", 123, "error")
	Info("Info")
	time.Sleep(time.Second * 2)
}

/*
func TestMultiBackend(t *testing.T) {
	b1, err := NewFileBackend("/tmp/dlog-test")
	if err != nil {
		panic(err)
	}
	defer Close()
	b2, err := NewSyslogBackend(syslog.LOG_LOCAL3, "dlog-test")
	if err != nil {
		panic(err)
	}
	m, _ := NewMultiBackend(b1, b2)
	SetLogging(INFO, m)
	Info("test multi")
}
*/
