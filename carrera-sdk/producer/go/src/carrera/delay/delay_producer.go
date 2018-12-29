package delay

import (
	"net/url"
	"encoding/json"
	"time"
	"fmt"
	"strconv"
	"net/http"
	"sync"
	"errors"
	"bytes"

	"go.intra.xiaojukeji.com/golang/commons/dhttp"
	"go.intra.xiaojukeji.com/golang/commons/util"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"go.intra.xiaojukeji.com/foundation/didi-standard-lib/scheduler/golang"
	"encoding/base64"
)

const (
	caller = "bridgeQ_sdk"
	callee = "bridgeQ"

	transform = 1
	untransform = 2

	type_delay = 2
)

type DelayProducer struct {
	config         *DelayConfig
	bridgeQHolder  *dhttp.HTTPClientHolder
	bridgeQHolders *util.ConcurrentMap
	sync.RWMutex
}

func NewDelayProducer(config *DelayConfig) *DelayProducer {
	return &DelayProducer{config: config, bridgeQHolders: util.NewConcurrentMap()}
}

// params不转换按原样发送给业务方, 业务方自己来处理, 强烈建议业务方使用该方法
func (p *DelayProducer) Send(t *Task, httpHeaders http.Header, product, bizKey string) *ErrInfo {
	return p.send(t, untransform, httpHeaders, product, bizKey)
}

// @deprecated, 该方法已经过时, 由于专快历史原因, params默认是转换的, 转换会把json变为k1=v1&k2=v2的结构, 不建议业务方再使用该方法
func (p *DelayProducer) SendWithParamsTransformed(t *Task, httpHeaders http.Header, product, bizKey string) *ErrInfo {
	return p.send(t, transform, httpHeaders, product, bizKey)
}

func (p *DelayProducer) send(t *Task, params_pattern int, httpHeaders http.Header, product, bizKey string) *ErrInfo {
	m := url.Values{}
	m.Set("topic", t.Topic)
	m.Set("url", t.Url)

	if (t.Type == 0) {
		t.Type = type_delay // default type is delay task
	}

	m.Set("type", strconv.Itoa(t.Type))
	m.Set("mode", strconv.Itoa(t.Mode))
	m.Set("retry", strconv.FormatInt(t.Retry, 10))
	m.Set("retry_interval", strconv.FormatInt(t.RetryInterval, 10))
	m.Set("retry_policy", strconv.FormatInt(t.RetryPolicy, 10))
	m.Set("timeout", strconv.Itoa(t.Timeout))
	m.Set("expire", strconv.FormatInt(t.Expire, 10))
	m.Set("timestamp", strconv.FormatInt(t.Timestamp, 10))

	// base64 encode to string if biz transfers thrift bytes.
	if len(t.Bin) != 0 {
		m.Set("params", base64.StdEncoding.EncodeToString(t.Bin))
	} else {
		m.Set("params", t.Params)
	}

	m.Set("params_pattern", strconv.Itoa(params_pattern))

	logid := time.Now().Unix()
	headerJson, err := json.Marshal(t.ParamHeaders)
	if err != nil {
		log.Errorf("%d, error while marshal paramHeaders, paramHeaders: %v, err: %v", logid, t.ParamHeaders, err)
		return BRIDGEQ_MARSHAL_PARAM_HEADERS_ERROR
	}
	m.Set("headers", string(headerJson))

	if len(httpHeaders) == 0 {
		httpHeaders = http.Header{}
	}
	httpHeaders.Set("Authorization", bizKey)

	url := fmt.Sprintf("%s?product=%s", "/bridgeq/addTask", product)
	return p.postData(logid, url, m.Encode(), httpHeaders)
}

func (p *DelayProducer) Cancel(taskid string, httpHeaders http.Header, product, bizKey string) *ErrInfo {
	logid := time.Now().Unix()
	m := url.Values{}
	m.Set("taskid", taskid)

	if len(httpHeaders) == 0 {
		httpHeaders = http.Header{}
	}
	httpHeaders.Set("Authorization", bizKey)

	url := fmt.Sprintf("%s?product=%s", "/bridgeq/cancelTask", product)
	return p.postData(logid, url, m.Encode(), httpHeaders)
}

func (p *DelayProducer) getHTTPClientHolder(logid int64) (*dhttp.HTTPClientHolder, error) {
	if p.useDiscovery() {
		var node string
		endPoint, _ := scheduler.GetServiceEndpoint(p.config.serviceName, "", "")
		if endPoint != nil {
			node = fmt.Sprintf("http://%s:%d", endPoint.Ip, endPoint.Port)
		} else {
			log.Errorf("%d, error while scheduler.GetServiceEndpoint, serviceName: %v", logid, p.config.serviceName)
			return nil, errors.New("error while scheduler.GetServiceEndpoint")
		}

		var holder *dhttp.HTTPClientHolder
		var ok bool
		var val interface{}
		var err error

		if val, ok = p.bridgeQHolders.Get(node); !ok {
			p.Lock()
			defer p.Unlock()
			if val, ok = p.bridgeQHolders.Get(node); !ok {
				urls := []string{node}
				log.Infof("%d, new http client holder for bridgeQ, urls: %v, timeout: %v, " +
					"maxIdleConnsPerHost: %d, retries: %v, rtThresholdToLog: %v, useDiscovery: %v",
					logid, urls, p.config.timeout, p.config.maxIdleConnsPerHost, 0,
					p.config.rtThresholdToLog, p.useDiscovery())
				holder, err = dhttp.NewHTTPClientHolder(caller, callee, urls, p.config.timeout,
					p.config.timeout, p.config.keepAlive, p.config.maxIdleConnsPerHost,
					p.config.retries, p.config.rtThresholdToLog)

				if err != nil {
					return nil, err
				}
				p.bridgeQHolders.Set(node, holder)
				return holder, nil
			}
		}
		if holder, ok = val.(*dhttp.HTTPClientHolder); !ok {
			log.Errorf("%d, type assertion error, val: %v, type: %s", logid, val, "*dhttp.HTTPClientHolder")
			return nil, errors.New("type assertion error")
		}
		return holder, nil
	} else {
		var err error
		if p.bridgeQHolder == nil {
			log.Infof("%d, new http client holder for bridgeQ, urls: %v, timeout: %v, " +
				"maxIdleConnsPerHost: %d, retries: %v, rtThresholdToLog: %v, useDiscovery: %v",
				logid, p.config.bridgeQList, p.config.timeout, p.config.maxIdleConnsPerHost,
				p.config.retries, p.config.rtThresholdToLog, p.useDiscovery())
			p.bridgeQHolder, err = dhttp.NewHTTPClientHolder(caller, callee, p.config.bridgeQList,
				p.config.timeout, p.config.timeout, p.config.keepAlive, p.config.maxIdleConnsPerHost,
				p.config.retries, p.config.rtThresholdToLog)
			if err != nil {
				log.Errorf("%d, error while init bridgeQHolder, err: %v", logid, err)
				return nil, err
			}
		}
		return p.bridgeQHolder, nil
	}
}

func (p *DelayProducer) postData(logid int64, url, body string, headers map[string][]string) *ErrInfo {
	bridgeQHolder, err := p.getHTTPClientHolder(logid)
	if err != nil {
		log.Errorf("%d, error while get bridgeQ http client holder, err: %v", logid, err)
		return BRIDGEQ_GET_HTTP_CLIENT_ERROR
	}

	var bqResp *dhttp.Response
	currRetries := 0
	for {
		currRetries++

		bqResp, err = bridgeQHolder.Post(logid, url, body, headers)

		// 如果没有使用服务发现, 则不在这里进行重试, 使用dhttp.NewHTTPClientHolder的重试机制
		// 如果使用了服务发现且已达到最大的重试次数, 也不再进行重试
		if err != nil {
			log.Errorf("%d, error while post data to bridgeQ, maxRetries: %v, currRetries: %v, err: %v",
				logid, p.config.retries, currRetries, err)

			if !p.useDiscovery() || currRetries > p.config.retries {
				return &ErrInfo{Errno: BRIDGEQ_ERROR.Errno, Errmsg: fmt.Sprintf("Error from bridgeQ, err: %v", err)}
			}
		} else {
			if bqResp.StatusCode == http.StatusInternalServerError {
				log.Errorf("%d, bad http response status code from bridgeQ, statusCode: %d, maxRetries: %v, currRetries: %v",
					logid, bqResp.StatusCode, p.config.retries, currRetries)

				if !p.useDiscovery() || currRetries > p.config.retries {
					return &ErrInfo{Errno: BRIDGEQ_BAD_HTTP_STATUS_CODE.Errno, Errmsg: fmt.Sprintf("Bad http response status code %d from bridgeQ", bqResp.StatusCode)}
				}
			} else {
				break
			}
		}
	}

	if bqResp.StatusCode != http.StatusOK {
		log.Errorf("%d, bad http response status code from bridgeQ, statusCode: %d", logid, bqResp.StatusCode)
		return &ErrInfo{Errno: BRIDGEQ_BAD_HTTP_STATUS_CODE.Errno, Errmsg: fmt.Sprintf("Bad http response status code %d from bridgeQ", bqResp.StatusCode)}
	} else {
		b, err := bqResp.ReadAll(logid)
		if err != nil {
			log.Errorf("%d, error while read bridgeQ http response, err: %v", logid, err)
			return BRIDGEQ_READ_RESPONSE_ERROR
		}

		bytesReader := bytes.NewReader(b)
		decoder := json.NewDecoder(bytesReader)
		decoder.UseNumber()

		errInfo := &ErrInfo{}
		if err := decoder.Decode(errInfo); err != nil {
			log.Errorf("%d, error while decode bridgeQ http response, err: %v", logid, err)
			return BRIDGEQ_DECODE_RESPONSE_ERROR
		}
		return errInfo
	}
}

func (p *DelayProducer) useDiscovery() bool {
	if (p.config.bridgeQList == nil || len(p.config.bridgeQList) == 0) && p.config.serviceName != "" {
		return true
	}
	return false
}

type Task struct {
	Topic         string

	Url           string

	Type	      int

	Mode          int

	Retry         int64

	RetryPolicy   int64

	RetryInterval int64

	Timeout       int

	Expire        int64

	Timestamp     int64

	Params        string  // SHOULD only choose either Params or Bin.

	Bin           []byte  // If you want to transfer thrift binary, just use Bin.

	ParamHeaders  map[string]interface{}
}
