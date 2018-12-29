package util

import "time"

func DateTimeToString(dateTime time.Time) (dateTimeStr string) {
	return dateTime.Format("2006-01-02 15:04:05")
}

func DateToString(dateTime time.Time) (dateStr string) {
	return dateTime.Format("2006-01-02")
}

func NowInS() int64 {
	return time.Now().Unix()
}

func NowInNs() int64 {
	return time.Now().UnixNano()
}

func NowInMs() int64 {
	return time.Now().UnixNano() / int64(time.Millisecond)
}
