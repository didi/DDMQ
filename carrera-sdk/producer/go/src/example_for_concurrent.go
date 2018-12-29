package main

import (
	"fmt"
	"runtime"
	"sync"
	"time"

	"carrera"
	"carrera/common/qlog"
)

func main() {
	//-------------- 配置描述参见example.go ----------------
	runtime.GOMAXPROCS(runtime.NumCPU())
	// set max log file size in GB for mq log & recovery file.
	// recovery file is used to hold message data when mq service is broken.
	qlog.InitQLog(10,50, qlog.LEVEL_INFO, qlog.LOG_PATH)
	config := carrera.NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613"})
	config.SetCarreraPoolSize(100)
	config.SetRecoverFromDropLog(true)

	config.SetRecoverFromDropLogInterval(30 * time.Second)

	producerPool := carrera.NewCarreraPooledProducer(config)
	producerPool.Start()

	start := time.Now().UnixNano()
	wg := new(sync.WaitGroup)
	for idx := 0; idx < 10; idx++ {
		wg.Add(1)
		go func() {
			for count := 0; count < 10; count++ {
				producerPool.Send("test", "msg")
			}
			wg.Done()
		}()
	}
	wg.Wait()
	producerPool.Shutdown()
	fmt.Println("used:", (time.Now().UnixNano()-start)/1000000)
}
