package util

import (
	"bytes"
	"encoding/json"
	"net"
	"runtime"
	"time"

	log "go.intra.xiaojukeji.com/golang/commons/dlog"
)

/**
* Logging when time exceeds threshold.
 */
func TraceTime(logid int64, desc string, startTime time.Time, timeThreshold int64) {
	timeSpent := time.Since(startTime)
	TraceTimeD(logid, desc, timeSpent, timeThreshold)
}

func TraceTimeD(logid int64, desc string, duration time.Duration, timeThreshold int64) {
	timeSpent := duration.Nanoseconds() / int64(time.Millisecond)
	if timeSpent > timeThreshold {
		if timeThreshold > 0 {
			log.Warningf("%d, time spent: %d, for %s", logid, timeSpent, desc)
		} else {
			log.Debugf("%d, time spent: %d, for %s", logid, timeSpent, desc)
		}
	}
}

/**
* Always logging
* if time exceeds threshold, log detail; otherwise log it with desc
 */
func TraceTimeDetail(logid int64, desc string, detail string, startTime time.Time, timeThreshold int64) {
	timeSpent := time.Since(startTime).Nanoseconds() / int64(time.Millisecond)
	if timeSpent > timeThreshold {
		if timeThreshold > 0 {
			log.Warningf("%d, time spent: %d, for %s", logid, timeSpent, desc+detail)
		} else {
			log.Debugf("%d, time spent: %d, for %s", logid, timeSpent, desc+detail)
		}
	} else {
		log.Debugf("%d, time spent: %d, for %s", logid, timeSpent, desc)
	}
}

func StrategyLogTsSec(tsSec int64) string {
	return StrategyLogTs(time.Unix(tsSec, 0))
}

func StrategyLogTs(t time.Time) string {
	return t.Format("2006-01-02 15:04:05")
}

func LogStack(logid int64, debug bool) {
	defer func() {
		if r := recover(); r != nil {
			log.Errorf("%d, panic occurred when LogStack: %v", logid, r)
		}
	}()
	if debug {
		buf := make([]byte, 1<<16)
		stackSize := runtime.Stack(buf, true)
		log.Errorf("%d, %s\n", logid, string(buf[0:stackSize]))
	}
}

func IsNetErr(err error) bool {
	if err == nil {
		return false
	}
	/*if neterr, ok := err.(net.Error); ok && neterr.Timeout() { */
	if _, ok := err.(net.Error); ok {
		return true
	}
	return false
}

func DecodeJsonSafe(data []byte) (parameters map[string]interface{}) {
	buffer := bytes.NewBuffer(data)
	decoder := json.NewDecoder(buffer)
	decoder.UseNumber()
	var v interface{}
	if err := decoder.Decode(&v); err == nil {
		var ok bool
		if parameters, ok = v.(map[string]interface{}); ok {
			return
		}
	} else {
		log.Errorf("decode err %v %s", err, string(data))
	}
	return nil
}

// s is a ordered slice
// target is the value to be inserted to s, and return a slice ordered too
func OrderInsert(s []uint64, target uint64) []uint64 {
	return BinaryInsert(s, 0, len(s)-1, target)
}

// s is a ordered slice
// target is the value to be inserted to s, and return a slice ordered too
// start: start slice index
// end: end slice index
func BinaryInsert(s []uint64, start, end int, target uint64) []uint64 {
	//log.Infof("s: %v start: %d, end: %d, target: %d", s, start, end, target)
	if len(s) == 0 {
		return append(s, target)
	}
	if target > s[end] {
		return append(s, target)
	}
	if target < s[start] {
		return append([]uint64{target}, s...)
	}
	if start >= end {
		log.Errorf("Should not come here start: %d, end: %d, target: %d", start, end, target)
		return append(s, target)
	}
	if end == start+1 { //
		return append(s[:end], append([]uint64{target}, s[end:]...)...)
	}
	m := (start + end) / 2
	if target < s[m] {
		end = m
	}
	if target > s[m] {
		start = m
	}
	return BinaryInsert(s, start, end, target)
}
