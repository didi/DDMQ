package consumer

import (
	carrera "carrera/consumer/CarreraConsumer"
	"net"
	"time"
	"go.intra.xiaojukeji.com/golang/thrift-lib/0.9.2"
)

type client interface {
	pull(request *carrera.PullRequest) (r *carrera.PullResponse, err error)
	submit(result *carrera.ConsumeResult_) (r bool, err error)
	getConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error)
	open(inet string, timeout int32) (err error)
	close()
}
type thriftClient struct {
	c *carrera.ConsumerServiceClient
}

func newThriftClient() client {
	return &thriftClient{}
}

func (client *thriftClient) pull(request *carrera.PullRequest) (r *carrera.PullResponse, err error) {
	return client.c.Pull(request)
}

func (client *thriftClient) submit(result *carrera.ConsumeResult_) (r bool, err error) {
	return client.c.Submit(result)
}

func (client *thriftClient) getConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error) {
	return client.c.GetConsumeStats(request)
}

func (client *thriftClient) open(inet string, timeout int32) error {
	dialer := net.Dialer{
		Timeout:   2 * time.Second,
		KeepAlive: 5 * time.Second,
	}

	conn, err := dialer.Dial("tcp", inet)
	if err != nil {
		return err
	}
	socket := thrift.NewTSocketFromConnTimeout(conn, time.Duration(timeout)*time.Millisecond)
	transport := thrift.NewTFramedTransportFactory(thrift.NewTTransportFactory()).GetTransport(socket)
	client.c = carrera.NewConsumerServiceClientFactory(transport, thrift.NewTCompactProtocolFactory())
	return nil
}

func (client *thriftClient) close() {
	client.c.Transport.Close()
}
