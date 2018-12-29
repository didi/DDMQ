package errinfo

import (
	"fmt"
	"strconv"
)

const (
	HTTP_ERROR = 400
)

type ErrorInfo struct {
	ErrNo  int
	ErrMsg string
}

func (e *ErrorInfo) Error() string {
	return e.String()
}

func (e *ErrorInfo) String() string {
	if e == nil {
		return "[errno:-1, errmsg:nil]"
	}
	return "[errno:" + strconv.Itoa(e.ErrNo) + "errmsg:" + e.ErrMsg + "]"
}

func New(errno int, errmsg string) *ErrorInfo {
	return newError(errno, errmsg)
}

func Errorf(errno int, format string, a ...interface{}) *ErrorInfo {
	return newError(errno, fmt.Sprintf(format, a...))
}

func newError(errno int, errmsg string) *ErrorInfo {
	return &ErrorInfo{errno, errmsg}
}
