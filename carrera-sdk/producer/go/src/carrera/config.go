package carrera

import (
	"time"
	"fmt"
)

type CarreraConfig struct {
	//proxy列表,本地配置proxy时必要参数，使用CSD服务发现后,如果从CSD server获取IP list失败，则会使用此list作为默认配置，务必填写
	carreraProxyList []string

	//Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK
	carreraProxyTimeout int64

	//客户端失败重试次数
	carreraClientRetry int

	//client和proxy server的超时时间，一般不建议设太小。必须大于carreraProxyTimeout的值，建议设置2倍的比例
	carreraClientTimeout int64

	//实例池中启动的producer实例数
	carreraPoolSize int

	//是否从本地drop log恢复消息，默认false
	recoverFromDropLog bool

	//从本地drop log恢复周期，默认30分钟
	recoverFromDropLogInterval time.Duration
}

const (
	PARTITION_HASH int32 = -1
	PARTITION_RAND int32 = -2

	TAG_ALL = "*"

	DEFAULT_RECOVER_FROM_DROP_LOG_INTERVAL = 30 * time.Minute //30分钟
)

func NewDefaultLocalCarreraConfig() *CarreraConfig {
	return &CarreraConfig{
		carreraProxyList:           []string{}, //测试环境proxy地址
		carreraProxyTimeout:        50,
		carreraClientRetry:         2,
		carreraClientTimeout:       100,
		carreraPoolSize:            50,
		recoverFromDropLog:         false,
		recoverFromDropLogInterval: DEFAULT_RECOVER_FROM_DROP_LOG_INTERVAL,
	}
}

func NewLocalCarreraConfig(carreraProxyList []string, carreraProxyTimeout int64, carreraClientRetry int, carreraClientTimeout int64) *CarreraConfig {
	return &CarreraConfig{
		carreraProxyList:           carreraProxyList,
		carreraProxyTimeout:        carreraProxyTimeout,
		carreraClientRetry:         carreraClientRetry,
		carreraClientTimeout:       carreraClientTimeout,
		carreraPoolSize:            50,
		recoverFromDropLog:         false,
		recoverFromDropLogInterval: DEFAULT_RECOVER_FROM_DROP_LOG_INTERVAL,
	}
}

/**
本地配置proxy列表时，必须参数
*/
func (conf *CarreraConfig) SetCarreraProxyList(proxyList []string) {
	conf.carreraProxyList = proxyList
}

/**
每个proxy列表，最多可连的数据
*/
func (conf *CarreraConfig) SetCarreraPoolSize(poolSize int) {
	conf.carreraPoolSize = poolSize
}

func (conf *CarreraConfig) SetCarreraProxyTimeout(proxyTimeout int64) {
	conf.carreraProxyTimeout = proxyTimeout
}

func (conf *CarreraConfig) SetCarreraClientTimeout(clientTimeout int64) {
	conf.carreraClientTimeout = clientTimeout
}

func (conf *CarreraConfig) SetCarreraClientRetry(clientRetry int) {
	conf.carreraClientRetry = clientRetry
}

func (conf *CarreraConfig) SetRecoverFromDropLog(isRecover bool) {
	conf.recoverFromDropLog = isRecover
}

func (conf *CarreraConfig) SetRecoverFromDropLogInterval(interval time.Duration) {
	conf.recoverFromDropLogInterval = interval
}

func (conf *CarreraConfig) String() string {
	if conf == nil {
		return "<nil>"
	}
	return fmt.Sprintf("CarreraConfig(%+v)", *conf)
}
