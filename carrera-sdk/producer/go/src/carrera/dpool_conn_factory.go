package carrera

import (
	"net"
	"time"
	"crypto/md5"
	"hash"

	"go.intra.xiaojukeji.com/golang/thrift-lib/0.9.2"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"carrera/pool"
	"carrera/CarreraProducer"
)

type PooledConnection struct {
	*pool.PooledObject
	addr   string
	client *CarreraProducer.ProducerServiceClient
	dp     *pool.DPool
	cmd5   hash.Hash
}

type DPooledConnFactory struct {
	config           *CarreraConfig
	transportFactory thrift.TTransportFactory
	protocolFactory  *thrift.TCompactProtocolFactory
}

func (f *DPooledConnFactory) open(addr string) (net.Conn, error) {
	dialer := net.Dialer{
		Timeout:   time.Duration(time.Duration(500)*time.Millisecond),
		KeepAlive: time.Duration(24*1) * time.Hour,
	}
	conn, err := dialer.Dial("tcp", addr)
	if err != nil {
		return nil, err
	}
	return conn, nil
}

func (f *DPooledConnFactory) createClient(addr string) (*CarreraProducer.ProducerServiceClient, error) {
	log.Debugf("Create A New Connection:%s", addr)
	if conn, err := f.open(addr); err != nil {
		log.Warningf("%s Create Connection Error: %v", addr, err)
		return nil, err
	} else {
		socket := thrift.NewTSocketFromConnTimeout(conn, time.Duration(f.config.carreraClientTimeout)*time.Millisecond)
		transport := f.transportFactory.GetTransport(socket)
		client := CarreraProducer.NewProducerServiceClientFactory(transport, f.protocolFactory)
		return client, nil
	}
}

// Factory to create new connection
func (f *DPooledConnFactory) Create(addr string) (pc pool.Poolable, err error) {
	c, err := f.createClient(addr)
	if err != nil {
		return nil, err
	}

	pc = &PooledConnection{
		PooledObject: &pool.PooledObject{},
		client:       c,
		addr:         addr,
		cmd5:         md5.New(),
	}

	return pc, nil
}

func (f *DPooledConnFactory) Validate(pc pool.Poolable) (err error) {
	return nil
}

func (f *DPooledConnFactory) Close(c pool.Poolable) error {
	return c.(*PooledConnection).client.Transport.Close()
}
