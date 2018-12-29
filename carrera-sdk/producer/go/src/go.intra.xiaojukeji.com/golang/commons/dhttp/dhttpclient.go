package dhttp

import (
	"compress/gzip"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"net"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"

	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	rpc "go.intra.xiaojukeji.com/golang/commons/rpcclient"
	"go.intra.xiaojukeji.com/golang/commons/util"
)

type HTTPClientHolder struct {
	Ch *rpc.ClientHolder
}

func NewHTTPClientHolder(caller, callee string, urls []string, connTimeout, timeout,
	keepAlive time.Duration, maxIdleConnsPerHost, retries int, rtThresholdToLog int64) (*HTTPClientHolder, error) {
	if len(urls) == 0 {
		log.Errorf("No urls found to new HTTPClientHolder for callee: %s", callee)
		return nil, errors.New("urls is empty")
	}
	ch := rpc.NewClientHolder(caller, callee, urls, rtThresholdToLog, retries)
	for _, url := range urls {
		//TODO: http transport can be shared by multiple urls, create a new one for
		// each backend or share a common one?
		transport := &http.Transport{
			//Proxy: http.ProxyFromEnvironment,
			Dial: (&net.Dialer{
				Timeout:   connTimeout,
				KeepAlive: keepAlive,
			}).Dial,
			MaxIdleConnsPerHost: maxIdleConnsPerHost,
			// same as DefaultTransport.IdleConnTimeout
			IdleConnTimeout:     90 * time.Second,
		}

		hc := &http.Client{
			Transport: transport,
			Timeout:   timeout,
		}
		rpcClient := rpc.NewRpcClient(url, hc)
		ch.AddClient(rpcClient)
	}
	return &HTTPClientHolder{Ch: ch}, nil
}

// Post is used to invoke HTTP Post request
func (hch *HTTPClientHolder) Post(logid int64, path, body string, headers map[string][]string) (*Response, error) {
	resp, err := hch.Ch.Exec(logid, func(c *rpc.RpcClient) (interface{}, string, error) {
		url := fmt.Sprintf("%s%s", c.Addr, path)
		log.Debugf("%d, %s post req, url: %s, body: %v", logid, hch.Ch.Callee, url, body)
		req, err := http.NewRequest("POST", url, strings.NewReader(body))
		if err != nil {
			log.Errorf("%d, %s failed to new http post request, url: %s, body: %s, err: %v",
				logid, hch.Ch.Callee, url, body, err)
			return nil, rpc.MetricsCodeInternalError, err
		}
		for key, values := range headers {
			for _, v := range values {
				req.Header.Add(key, v)
			}
		}
		resp, err := c.RealClient.(*http.Client).Do(req)
		if err != nil {
			log.Errorf("%d, %s post failed, url: %s, body: %s, err: %v", logid, hch.Ch.Callee, url, body, err)
			return nil, rpc.MetricsCodeInternalError, err
		}
		log.Debugf("%d, response status code: %d", logid, resp.StatusCode)
		return &Response{Resp: resp, StatusCode: resp.StatusCode},
			strconv.Itoa(resp.StatusCode), needRetry(logid, resp.StatusCode)
	})

	return processResp(logid, resp, err)
}

func processResp(logid int64, resp interface{}, err error) (*Response, error) {
	if err != nil {
		return nil, err
	} else if r, ok := resp.(*Response); ok {
		return r, nil
	} else {
		log.Errorf("%d, should not go here", logid)
		return nil, errors.New("Should not go here")
	}
}

func needRetry(logid int64, statusCode int) error {
	if statusCode >= http.StatusInternalServerError { // need retry when sc >= 500
		log.Errorf("%d, got a retry status code: %d, retry it.", logid, statusCode)
		return &rpc.RetryError{}
	}
	return nil
}

// PostForm is used to invoke HTTP Post request in form content
func (hch *HTTPClientHolder) PostForm(logid int64, path string, data url.Values) (*Response, error) {
	resp, err := hch.Ch.Exec(logid, func(c *rpc.RpcClient) (interface{}, string, error) {
		urlStr := fmt.Sprintf("%s%s", c.Addr, path)
		log.Debugf("%d, %s post req, url: %s, data: %v", logid, hch.Ch.Callee, urlStr, data)
		resp, err := c.RealClient.(*http.Client).PostForm(urlStr, data)
		if err != nil {
			log.Errorf("%d, %s post failed, url: %s, err: %v", logid, hch.Ch.Callee, urlStr, err)
			return nil, rpc.MetricsCodeInternalError, err
		}
		log.Debugf("%d, response status code: %d", logid, resp.StatusCode)
		return &Response{Resp: resp, StatusCode: resp.StatusCode},
			strconv.Itoa(resp.StatusCode), needRetry(logid, resp.StatusCode)
	})
	return processResp(logid, resp, err)
}

func (hch *HTTPClientHolder) Get(logid int64, path, param string, headers map[string][]string) (res *Response, err error) {
	resp, err := hch.Ch.Exec(logid, func(c *rpc.RpcClient) (interface{}, string, error) {
		var url string
		if len(param) == 0 {
			url = fmt.Sprintf("%s%s", c.Addr, path)
		} else {
			url = fmt.Sprintf("%s%s?%s", c.Addr, path, param)
		}
		log.Debugf("%d, %s get req, url: %s", logid, hch.Ch.Callee, url)
		req, err := http.NewRequest("GET", url, nil)
		if err != nil {
			log.Errorf("%d, %s failed to new http request, url: %s, err: %v", logid, hch.Ch.Callee, url, err)
			return nil, rpc.MetricsCodeInternalError, err
		}
		for key, values := range headers {
			for _, v := range values {
				req.Header.Add(key, v)
			}
		}
		resp, err := c.RealClient.(*http.Client).Do(req)
		if err != nil {
			log.Errorf("%d, %s get failed, url: %s, err: %v", logid, hch.Ch.Callee, url, err)
			return nil, rpc.MetricsCodeInternalError, err
		}
		log.Debugf("%d, response status code: %d", logid, resp.StatusCode)
		return &Response{Resp: resp, StatusCode: resp.StatusCode},
			strconv.Itoa(resp.StatusCode), needRetry(logid, resp.StatusCode)
	})
	return processResp(logid, resp, err)
}

type Response struct {
	Resp       *http.Response
	StatusCode int
}

// Read response body into a byte slice.
func (this *Response) ReadAll(logid int64) (bytes []byte, err error) {
	defer func() {
		if err != nil {
			log.Errorf("%d, read resp err: %v", logid, err)
		} else {
			log.Debugf("%d, http SC: %d, resp: %s", logid, this.StatusCode, string(bytes))
		}
	}()
	bytes, err = this.ReadBody(logid)
	return
}

func (this *Response) ReadBody(logid int64) (bytes []byte, err error) {
	var reader io.ReadCloser
	defer func() {
		if reader != nil {
			err = reader.Close()
		}
	}()
	switch this.Resp.Header.Get("Content-Encoding") {
	case "gzip":
		reader, err = gzip.NewReader(this.Resp.Body)
		if err != nil {
			return nil, err
		}
	default:
		reader = this.Resp.Body
	}
	bytes, err = ioutil.ReadAll(reader)
	return
}

func (this *Response) ReadHeader(logid int64, keys []string) (ret map[string]string) {
	ret = make(map[string]string)
	for _, key := range keys {
		ret[key] = this.Resp.Header.Get(key)
	}
	return
}

func (this *Response) ToString(logid int64) (string, error) {
	bytes, err := this.ReadAll(logid)
	if err != nil {
		return "", err
	}
	return string(bytes), nil
}

/**
 * @description convert to map
 *
 * @param  logid int64
 *
 * @return ret map
 * @return err error
 */
func (this *Response) ToMap(logid int64) (ret map[string]interface{}, err error) {
	bytes, err := this.ReadAll(logid)
	if err != nil {
		log.Errorf("%d, response read failed, err:%v", logid, err)
		return nil, err
	}
	ret = make(map[string]interface{})
	err = json.Unmarshal(bytes, &ret)
	if err != nil {
		log.Errorf("%d, response unmarshal failed, err:%v", logid, err)
		return nil, err
	}

	return
}

/**
 * @description convert to map
 *
 * @param  logid int64
 *
 * @return ret map
 * @return err error
 */
func (this *Response) DecodeUseNumber(logid int64) (ret map[string]interface{}, err error) {
	bytes, err := this.ReadAll(logid)
	if err != nil {
		log.Errorf("%d, response read failed, err:%v", logid, err)
		return nil, err
	}

	if len(bytes) == 0 {
		return nil, errors.New("decode fail for no body in response")
	}

	ret = make(map[string]interface{})
	ret = util.DecodeJsonSafe(bytes)
	if ret == nil {
		err = errors.New("decode fail")
	}
	return
}
