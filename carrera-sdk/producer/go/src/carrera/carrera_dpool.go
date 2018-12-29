package carrera

import (
	"time"
	"carrera/pool"
	"go.intra.xiaojukeji.com/golang/thrift-lib/0.9.2"
	"math"
)

func newCarreraDPool(servers []string, config *CarreraConfig) *pool.DPool {
	connFactory := &DPooledConnFactory{
		config:           config,
		transportFactory: thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory()),
		protocolFactory:  thrift.NewTCompactProtocolFactory(),
	}
	poolConfig := pool.PoolConfig{
		MaxIdle:     int(math.Ceil(float64(config.carreraPoolSize)/2)),
		MaxActive:   config.carreraPoolSize,
		IdleTimeout: 300 * time.Second,
		MaxFails:    5,
	}

	return pool.NewDPool(servers, connFactory, poolConfig)
}
