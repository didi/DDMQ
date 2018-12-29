package delay

import "time"

type DelayConfig struct {
	bridgeQList         []string
	timeout             time.Duration
	keepAlive           time.Duration
	maxIdleConnsPerHost int
	retries             int
	rtThresholdToLog    int64
	serviceName         string
}

func NewDefaultDelayConfig() *DelayConfig {
	timeout, _ := time.ParseDuration("50ms")
	keepAlive, _ := time.ParseDuration("30s")

	return &DelayConfig{
		bridgeQList:            []string{},
		timeout:                timeout,
		keepAlive:              keepAlive,
		maxIdleConnsPerHost:    300,
		retries:                1,
		rtThresholdToLog:       50,
	}
}

func NewDelayConfig(bridgeQList []string, timeout, keepAlive time.Duration, maxIdleConnsPerHost, retries int, rtThresholdToLog int64, serviceName string) *DelayConfig {
	return &DelayConfig{
		bridgeQList:            bridgeQList,
		timeout:                timeout,
		keepAlive:              keepAlive,
		maxIdleConnsPerHost:    maxIdleConnsPerHost,
		retries:                retries,
		rtThresholdToLog:       rtThresholdToLog,
		serviceName:            serviceName,
	}
}

func (config *DelayConfig) SetBridgeQList(bridgeQList []string) {
	config.bridgeQList = bridgeQList
}

func (config *DelayConfig) SetTimeout(timeout time.Duration) {
	config.timeout = timeout
}

func (config *DelayConfig) SetKeepAlive(keepAlive time.Duration) {
	config.keepAlive = keepAlive
}

func (config *DelayConfig) SetMaxIdleConnsPerHost(maxIdleConnsPerHost int) {
	config.maxIdleConnsPerHost = maxIdleConnsPerHost
}

func (config *DelayConfig) SetRetries(retries int) {
	config.retries = retries
}

func (config *DelayConfig) SetRtThresholdToLog(rtThresholdToLog int64) {
	config.rtThresholdToLog = rtThresholdToLog
}

func (config *DelayConfig) SetServiceName(serviceName string) {
	config.serviceName = serviceName
}


