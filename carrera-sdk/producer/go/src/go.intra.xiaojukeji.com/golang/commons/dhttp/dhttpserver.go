package dhttp

import (
	"encoding/json"
	"net"
	"net/http"
	"strconv"
	"time"

	"go.intra.xiaojukeji.com/golang/commons/ac"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"go.intra.xiaojukeji.com/golang/commons/stats"
	"go.intra.xiaojukeji.com/golang/commons/util"
)

var (
	// general errno starts with 9
	RESP_INTERNAL_ERROR = Resp{Errno: 90001, Errmsg: "Internal error"}
	RESP_QPS_HIT        = Resp{Errno: 90002, Errmsg: "Hit QPS limit"}
	RESP_NO_AUTH        = Resp{Errno: 90003, Errmsg: "Unauthorized request"}

	WhiteIpList []string
)

var AccessControl = &ac.AccessControl{}

type Resp struct {
	Errno  int    `json:"errno"`
	Errmsg string `json:"errmsg"`
}

// TODO: COPIED from net/http/server
// tcpKeepAliveListener sets TCP keep-alive timeouts on accepted
// connections. It's used by ListenAndServe and ListenAndServeTLS so
// dead TCP connections (e.g. closing laptop mid-download) eventually
// go away.
type tcpKeepAliveListener struct {
	*net.TCPListener
}

func (ln tcpKeepAliveListener) Accept() (c net.Conn, err error) {
	tc, err := ln.AcceptTCP()
	if err != nil {
		return
	}
	tc.SetKeepAlive(true)
	tc.SetKeepAlivePeriod(3 * time.Minute)
	return tc, nil
}

// if return err, then http response code is internal error
type LogicHandler func(logid int64, r *http.Request, acParams *ac.AcParams) (statusCode int, result interface{}, err error)

func ServeLn(ln net.Listener, handlers map[string]LogicHandler, checkAuth bool) error {
	for path, handler := range handlers {
		// all paths are registered to one func
		http.Handle(path, http.HandlerFunc(delegate(path, handler, checkAuth)))
	}

	srv := &http.Server{Handler: nil}
	err := srv.Serve(tcpKeepAliveListener{ln.(*net.TCPListener)})
	log.Errorf("Error while server serve: %v", err)
	return err
}

func Serve(addr string, handlers map[string]LogicHandler, checkAuth bool) error {
	for path, handler := range handlers {
		// all paths are registered to one func
		http.Handle(path, http.HandlerFunc(delegate(path, handler, checkAuth)))
	}
	log.Infof("Server starts to listen on: %v", addr)
	err := http.ListenAndServe(addr, nil)
	log.Errorf("Error while server serve: %v", err)
	return err
}

func delegate(path string, handler LogicHandler, checkAuth bool) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		logid := time.Now().UnixNano()
		// recover the panic
		defer recoverIt(logid, w)

		var statusCode int
		var resp interface{}
		var bizKey string = "0"
		clientIP := getClientIps(r)
		// general logging
		defer func() {
			bytes, statusCode, _ := writeResponse(logid, w, statusCode, resp)
			latency := time.Duration(time.Now().UnixNano() - logid)
			stats.Rpc(&stats.RpcT{bizKey, path, latency, strconv.Itoa(statusCode)})
			ipBytes, _ := json.Marshal(clientIP)
			if bytes != nil && ipBytes != nil {
				log.Debugf("%d, path: %s, sc: %v, resp: %v, time: %dms, clientIP: %v, bizKey: %v",
					logid, path, statusCode, string(bytes), latency/1e6, string(ipBytes), bizKey)
			} else {
				log.Debugf("%d, path: %s, sc: %v, resp: <nil>, time: %dms, clientIP: %v, bizKey: %v",
					logid, path, statusCode, latency/1e6, clientIP, bizKey)
			}
		}()
		// check auth
		acParams, err := ac.CheckAuth(logid, WhiteIpList, checkAuth, r, clientIP)
		if err != nil {
			statusCode = http.StatusUnauthorized
			resp = RESP_NO_AUTH
			return
		}

		bizKey = strconv.FormatUint(uint64(acParams.BizKey), 10)

		// check qps limit
		if AccessControl.HitQps(logid) {
			log.Errorf("%d, qps hitted", logid)
			statusCode = http.StatusTooManyRequests
			resp = RESP_QPS_HIT
			return
		}
		if sc, result, err := handler(logid, r, acParams); err != nil {
			statusCode = http.StatusInternalServerError
			resp = RESP_INTERNAL_ERROR
		} else {
			statusCode = sc
			resp = result
		}

		// bizKey may be reset within handler
		b := strconv.FormatUint(uint64(acParams.BizKey), 10)
		if b != bizKey {
			bizKey = bizKey + "|" + b
		}
	}
}

func writeResponse(logid int64, w http.ResponseWriter, sc int, result interface{}) (bytes []byte, realSC int, err error) {
	realSC = sc
	if result == nil {
		log.Errorf("%d, result is nil", logid)
		realSC = http.StatusInternalServerError
		writeResponse(logid, w, http.StatusInternalServerError, RESP_INTERNAL_ERROR)
		return
	}
	w.WriteHeader(sc)
	if bytes, err = json.Marshal(result); err != nil {
		log.Errorf("%d, err while json marshal result: %v, err: %v", logid, result, err)
		realSC = http.StatusInternalServerError
		writeResponse(logid, w, http.StatusInternalServerError, RESP_INTERNAL_ERROR)
		return
	} else if _, err = w.Write(bytes); err != nil { // no need to rewrite
		log.Errorf("%d, err while jsonEncode result: %v, err: %v", logid, result, err)
		realSC = http.StatusInternalServerError
		return
	}
	return
}

func recoverIt(logid int64, w http.ResponseWriter) {
	if r := recover(); r != nil {
		log.Errorf("%d, panic occurred: %v, recovered it", logid, r)
		util.LogStack(logid, true)
		writeResponse(logid, w, http.StatusInternalServerError, RESP_INTERNAL_ERROR)
	}
	return
}

func getClientIps(r *http.Request) *ac.ClientIP {
	remoteIp, _, _ := net.SplitHostPort(r.RemoteAddr)
	xForwardFor := r.Header.Get("X-FORWARDED-FOR")
	xRealIp := r.Header.Get("X-REAL-IP")
	return &ac.ClientIP{remoteIp, xForwardFor, xRealIp}
}
