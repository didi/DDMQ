package delay

type ErrInfo struct {
	Errno  uint   `json:"errno"`
	Errmsg string `json:"errmsg"`
	Taskid string `json:"taskid"`
}

//------------------------------- bridgeQ --------------------------------------------------------------------------------//
var BRIDGEQ_ERROR = &ErrInfo{Errno: 31001, Errmsg: "Error from bridgeQ"}
var BRIDGEQ_BAD_HTTP_STATUS_CODE = &ErrInfo{Errno: 31002, Errmsg: "Bad http response status code from bridgeQ"}
var BRIDGEQ_GET_HTTP_CLIENT_ERROR = &ErrInfo{Errno: 31003, Errmsg: "Error while get bridgeQ http client holder"}
var BRIDGEQ_READ_RESPONSE_ERROR = &ErrInfo{Errno: 31004, Errmsg: "Error while read bridgeQ http response"}
var BRIDGEQ_DECODE_RESPONSE_ERROR = &ErrInfo{Errno: 31005, Errmsg: "Error while decode bridgeQ http response"}
var BRIDGEQ_MARSHAL_PARAM_HEADERS_ERROR = &ErrInfo{Errno: 31006, Errmsg: "Error while marshal paramHeaders"}
