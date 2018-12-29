package pool

import (
	"errors"
	"fmt"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"sync"
	"sync/atomic"
	"time"
)

// If an attempt is made to return an object to the pool that is in any state
// other than allocated (i.e. borrowed).
// Attempting to return an object more than once or attempting to return an
// object that was never borrowed from the pool will trigger this error.
var errReturnInvalid = errors.New("DPool:object has already been returned to this pool or is invalid")
var errPoolClosed = errors.New("dpool has Closed")

type DPool struct {
	// Server address list, e.t. []string{"127.0.0.1:8080", "127.0.0.1:8081"}
	serverList []string

	// Function to create a new pooled client for server @serverAddr
	connFactory PooledConnFactory

	// Sharded pool by server address
	poolShards []*PoolShard

	// Pool configuration, e.t. maxIdle, maxActive, ...
	poolConfig PoolConfig

	// @atomic index to pick the next shard
	index uint32

	// Suspect shards, should be checked immediately
	suspectShards chan *PoolShard

	// Current available servers
	numAvailable int

	maxRetry int

	// Stopper signal to stop checker coroutine
	stopper chan struct{}

	// WaitGroup to wait health checker goroutine to stop
	wg sync.WaitGroup

	closed uint32
}

func NewDPool(servers []string, connFactory PooledConnFactory, poolConfig PoolConfig) *DPool {
	if connFactory == nil {
		panic("DPool:PooledConnFactory is nil")
	}

	numServers := len(servers)

	dp := &DPool{
		serverList:    servers,
		connFactory:   connFactory,
		poolConfig:    poolConfig,
		suspectShards: make(chan *PoolShard, 100),
		numAvailable:  numServers,
		maxRetry:      numServers,
		stopper:       make(chan struct{}),
		closed:        0,
	}
	poolShards := make([]*PoolShard, numServers)
	for i := 0; i < numServers; i++ {
		shard := NewPoolShard(servers[i], dp, poolConfig.MaxIdle, poolConfig.MaxActive, poolConfig.MaxFails)
		poolShards[i] = shard
	}
	dp.poolShards = poolShards
	if numServers < 5 {
		dp.maxRetry = 5
	}

	go dp.goCheckServer()

	return dp
}

func findPoolShard(shards []*PoolShard, server string) (int, *PoolShard) {
	if len(shards) == 0 || len(server) == 0 {
		return -1, nil
	}
	for idx, shard := range shards {
		if server == shard.server {
			return idx, shard
		}
	}

	return -1, nil
}

func (dp *DPool) Get(timeoutMill int64) (Poolable, error) {
	begin := time.Now().UnixNano()
	if atomic.LoadUint32(&dp.closed) == 1 {
		log.Info("[POOL] DPool has Closed, skip Get...")
		return nil, errPoolClosed
	}

	exitTimeout := time.After(time.Duration(timeoutMill) * time.Millisecond)
	var localIdx = atomic.AddUint32(&dp.index, 1)
	for tries := 0; ; tries++ {
		if atomic.LoadUint32(&dp.closed) == 1 {
			log.Info("[POOL] DPool has Closed, skip while true get...")
			return nil, errPoolClosed
		}
		if len(dp.serverList) == 0 || len(dp.poolShards) == 0 {
			log.Info("[POOL] DPool Server list is empty")
			return nil, fmt.Errorf("pool shard is empty")
		}
		select {
		case <-exitTimeout:
			log.Warningf("[POOL] DPool get connection timeout, cost=%v", time.Duration(time.Now().UnixNano()-begin))

			return nil, fmt.Errorf("get connection timeout")
		default:
		}

		if tries > dp.maxRetry {
			time.Sleep(10 * time.Millisecond)
		}

		idx := (localIdx + uint32(tries)) % uint32(len(dp.serverList))
		// The server shard selected may be down, continue to get the next one
		if !dp.poolShards[idx].isAvailable() {
			atomic.AddUint32(&dp.index, 1)
			log.Infof("[POOL] Server <%s> temporarily unavailable", dp.poolShards[idx].server)
			continue
		}

		c, err := dp.poolShards[idx].get()
		if err != nil {
			log.Infof("[POOL] Failed to get pooled connection from shard <%s>, tries: %d, err: %s", dp.poolShards[idx].server, tries, err.Error())
			if err.ErrNo == _ERROR_CODE_SHARD_HAS_CLOSED {
				break
			} else {
				atomic.AddUint32(&dp.index, 1)
				continue
			}
		}
		return c, nil
	}

	return nil, fmt.Errorf("[POOL] failed to get connection after %d retries", dp.maxRetry)
}

func (dp *DPool) Put(c Poolable, broken bool) error {
	if atomic.LoadUint32(&dp.closed) == 1 {
		log.Info("[POOL] DPool has Closed, skip Put...")
		return errPoolClosed
	}

	shard := c.getDataSource()
	if shard == nil {
		dp.connFactory.Close(c)
		log.Error("[POOL] Return invalid object")
		return errReturnInvalid
	}

	if !dp.shardIsExist(shard) {
		shard.put(c, broken)
		shard.Close()
		log.Infof("[POOL] shard has removed, close it, server:%v", shard.server)
		return nil
	}
	if broken {
		log.Infof("[POOL] shard connection is broken, mark unavailable, server:%v", shard.server)
		shard.put(c, broken)
		dp.markAvailable(shard, false)
		return nil
	}
	return shard.put(c, broken)
}

func (dp *DPool) shardIsExist(shard *PoolShard) bool {
	if idx, _ := findPoolShard(dp.poolShards, shard.server); idx == -1 {
		return false
	} else {
		return true
	}
}

func (dp *DPool) markAvailable(shard *PoolShard, b bool) {
	if !dp.shardIsExist(shard) {
		log.Infof("[POOL] shard has removed, close it, server:%v", shard.server)
		shard.Close()
		return
	}

	if b {
		if shard.markAvailable(true) {
			dp.numAvailable++
			log.Infof("[POOL] Server <%s> recovered", shard.server)
		}
	} else {
		if !shard.isAvailable() {
			return
		}
		totalServers := len(dp.serverList)
		// Ensure that at most 2/3 servers can be marked as unavaialable
		if dp.numAvailable*3 > totalServers*1 {
			if shard.markAvailable(false) {
				dp.numAvailable--
				log.Infof("[POOL] Mark server <%s> unavailable", shard.server)
			}
		} else {
			log.Infof("[POOL] Server <%s> cannot be marked as unavailable due to too many failed shards, numAvailable: %d, totalShards: %d", shard.server, dp.numAvailable, totalServers)
		}
	}
}

func (dp *DPool) checkServer(server string) (ok bool) {
	for tries := 1; tries <= 2; tries++ {
		c, err := dp.connFactory.Create(server)
		if err != nil {
			log.Errorf("[POOL] Connect server <%s> failed, tries: %d, err: %s", server, tries, err.Error())
			continue
		}

		if err := dp.connFactory.Validate(c); err != nil {
			dp.connFactory.Close(c)
			log.Errorf("[POOL] Ping server <%s> failed, tries: %d, err: %s", server, tries, err.Error())
			continue
		}

		dp.connFactory.Close(c)
		return true
	}

	return false
}

// Check server availiability periodically
func (dp *DPool) goCheckServer() {
	defer dp.wg.Done()
	dp.wg.Add(1) // XXX
	var timer = time.NewTicker(3 * time.Second)
	defer timer.Stop()

	for {
		select {
		case <-timer.C:
			log.Infof("[POOL] start goCheckServer, poolshards len=%v", len(dp.poolShards))
			for _, shard := range dp.poolShards {
				// Healthy shards can be exampt from examination
				if !shard.suspectable() && shard.isAvailable() {
					continue
				}
				dp.doCheck(shard)
			}
		case shard := <-dp.suspectShards:
			log.Infof("[POOL] Check suspect server <%s> ...", shard.server)
			dp.doCheck(shard)
		case <-dp.stopper:
			return
		}
	}
}

func (dp *DPool) doCheck(shard *PoolShard) {
	checkRet := dp.checkServer(shard.server)
	dp.markAvailable(shard, checkRet)
}

func (dp *DPool) Shutdown() {
	log.Info("[POOL] Shutdown dpool...")
	atomic.CompareAndSwapUint32(&dp.closed, 0, 1)
	close(dp.stopper)
	dp.wg.Wait()
	for _, shard := range dp.poolShards {
		shard.Close()
	}
}

type PoolStats struct {
	Shard        string `json:"shard"`
	Available    bool   `json:"available"`
	NumActive    int    `json:"num_active"`
	NumGet       uint64 `json:"num_get"`
	NumPut       uint64 `json:"num_put"`
	NumBroken    uint64 `json:"num_broken"`
	NumDial      uint64 `json:"num_dial"`
	NumDialError uint64 `json:"num_dial_error"`
	NumEvict     uint64 `json:"num_evict"`
	NumClose     uint64 `json:"num_close"`
}

func (dp *DPool) GetPoolStats() ([]PoolStats, error) {
	if atomic.LoadUint32(&dp.closed) == 1 {
		log.Info("[POOL] DPool has Closed, skip GetPoolStats...")
		return nil, errPoolClosed
	}
	stats := make([]PoolStats, len(dp.serverList))
	for i, shard := range dp.poolShards {
		stats[i] = shard.getStats()
	}
	return stats, nil
}
