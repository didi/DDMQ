package dlog

import (
	"errors"
	"testing"
	"time"

	"go.intra.xiaojukeji.com/golang/logrus"
)

type TestStruct struct {
	A int
	B string
	C map[string]interface{}
}

func TestLocalFileLog(t *testing.T) {
	logger1 := CreateLocalLog("./test.log", "debug", 1, 10, "")
	logger1.SetDisableQuoting(true)
	logger1.SetDisableAutoAddKey(true)
	key1 := time.Now().UnixNano()
	key2 := "string"
	key3 := &TestStruct{}
	key4 := []interface{}{1, "xxx"}
	err := errors.New("Test error")
	for i := 0; i < 3; i++ {
		curSec := time.Now().Unix()
		for curSec+1 > time.Now().Unix() {
			logger1.Infof("%d, msg=test logging||key1=%v||key2=%v", time.Now().UnixNano(), "value1", "value2")

		}
	}
	logger1.Close()
	logger2 := CreateLocalLog("./test.log", "debug", 1, 10, "")
	logger2.SetKeySeparator("||")
	for i := 0; i < 3; i++ {
		curSec := time.Now().Unix()
		for curSec+1 > time.Now().Unix() {
			logger2.WithFields(logrus.Fields{
				"key1": key1,
				"key2": key2,
				"key3": key3,
				"key4": key4,
			}).WithError(err).Error("Test msg")
		}
	}
	logger2.Close()
	time.Sleep(3 * time.Second)
}
