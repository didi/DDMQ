package carrera

import (
	"fmt"
	"carrera/common/qlog"
	"sync"
	"testing"
	"time"
)

func TestDpoolLocalMode(t *testing.T) {

	qlog.InitQLog(10, 50, qlog.LEVEL_DEBUG, qlog.LOG_PATH)

	fmt.Println(qlog.LOG_PATH)
	wg := new(sync.WaitGroup)
	wg.Add(1)

	list := []string{"127.0.0.1:9613"}
	conf := NewDefaultLocalCarreraConfig()
	conf.SetCarreraProxyList(list)
	conf.SetCarreraPoolSize(2)
	pool := newCarreraDPool(list, conf)

	fmt.Println("first get conn")
	conn, err := pool.Get(5000)
	if err != nil {
		panic(err)
	} else {
		fmt.Printf("get conn %+v, not return\n", conn.(*PooledConnection).addr)
	}
	//pool.Put(conn, false)

	time.Sleep(1000)
	fmt.Println("second get conn")
	if conn, err := pool.Get(5000); err != nil {
		panic(err.Error())
	} else {
		fmt.Printf("get conn %+v, not return\n", conn.(*PooledConnection).addr)
	}

	time.Sleep(1000)
	fmt.Println("third get conn")
	if conn, err := pool.Get(5000); err != nil {
		panic(err.Error())
	} else {
		fmt.Printf("get conn %+v, not return\n", conn.(*PooledConnection).addr)
	}

	wg.Wait()
}
