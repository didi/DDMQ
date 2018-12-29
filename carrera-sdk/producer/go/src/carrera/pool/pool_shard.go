package pool

import (
	"errors"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"sync/atomic"
	"time"
	"carrera/common/errinfo"
)

type PoolShard struct {
	// Maximum number of idle connections in the pool.
	// @const
	maxIdle int

	// Maximum number of connections allocated by the pool at a given time.
	// When zero, there is no limit on the number of connections in the pool.
	// @const
	maxActive int32

	// Current number of active connections
	// @atomic
	active int32

	// Close connections after remaining idle for this duration. If the value
	// is zero, then idle connections are not closed. Applications should set
	// the timeout to a value less than the server's timeout.
	idleTimeout time.Duration

	// If wait is true and the pool is at the maxIdle limit, then Get() waits
	// for a connection to be returned to the pool before returning.
	wait bool

	// @atomic
	closed uint32

	// Stack of idle Poolable with most recently used at the front.
	idle chan Poolable

	// Server address, e.g. "127.0.0.1:8080"
	// @const
	server string

	dpool *DPool

	// If marked as unavailable, then the checking goroutine will check it availability periodically.
	// A server is "available" if we can connnect to it, and respond to Ping() request of client.
	// Since no atomic boolean provided in Golang, we use uint32 instead.
	// @atomic
	available uint32

	// The failure count in succession. If the fails reached the threshold of "unavailable",
	// then this server should be marked as "unavailable", and we will not get connection
	// from it until recovered.
	// The idea of "fails" & "maxFails" is borrowed from Nginx.
	// @atomic
	fails uint32

	// The idea of "fails" & "maxFails" is borrowed from Nginx.
	// @const
	maxFails uint32

	stats PoolStats
}

const (
	_ERROR_CODE_SHARD_HAS_CLOSED       = 10001
	_ERROR_CODE_SHARD_POOL_EXHAUSTED   = 10002
	_ERROR_CODE_SHARD_CREATE_EXCEPTION = 10003
)

// NewPoolShard creates a new pool shard.
func NewPoolShard(server string, parent *DPool, maxIdle, maxActive, maxFails int) *PoolShard {
	return &PoolShard{
		server:    server,
		dpool:     parent,
		maxIdle:   maxIdle,
		maxActive: int32(maxActive),
		idle:      make(chan Poolable, maxIdle),
		wait:      false, // TODO timed wait
		available: 1,
		closed:    0,
		maxFails:  uint32(maxFails),
	}
}

// If state changed, return true
func (p *PoolShard) markAvailable(b bool) (changed bool) {
	if b {
		return atomic.CompareAndSwapUint32(&p.available, 0, 1)
	}
	p.empty()
	return atomic.CompareAndSwapUint32(&p.available, 1, 0)
}

func (p *PoolShard) isAvailable() bool {
	if atomic.LoadUint32(&p.available) != 0 {
		return true
	}
	return false
}

// markFailed mark this shard as "suspect" or not
func (p *PoolShard) markFailed(failed bool) {
	if failed {
		s := atomic.AddUint32(&p.fails, 1)
		if s == p.maxFails {
			select {
			case p.dpool.suspectShards <- p:
			default: /*do nothing*/
			}
		}
	} else {
		atomic.StoreUint32(&p.fails, 0)
	}
}

func (p *PoolShard) suspectable() bool {
	if atomic.LoadUint32(&p.fails) >= p.maxFails {
		return true
	}
	return false
}

// Close releases the resources used by the pool shard.
func (p *PoolShard) Close() error {
	if !atomic.CompareAndSwapUint32(&p.closed, 0, 1) {
		return errors.New("dpool: close pool shard already closed")
	}
	p.empty()
	return nil
}

// get prunes stale connections and returns a connection from the idle channel or
// creates a new connection.
// The application must return the borrowed connection.
func (p *PoolShard) get() (c Poolable, err *errinfo.ErrorInfo) {
	// Check for pool closed before creating a new connection.
	if atomic.LoadUint32(&p.closed) == 1 {
		return nil, errinfo.New(_ERROR_CODE_SHARD_HAS_CLOSED, "dpool: get on closed pool shard")
	}

	atomic.AddUint64(&p.stats.NumGet, 1)

	select {
	case c = <-p.idle:
		c.setBorrowed(true)
		return c, nil
	default:
		if p.maxActive != 0 && atomic.LoadInt32(&p.active) >= p.maxActive {
			return nil, errinfo.New(_ERROR_CODE_SHARD_POOL_EXHAUSTED, "dpool: connection pool exhausted")
		}

		// Dial new connection if under limit.
		atomic.AddInt32(&p.active, 1)

		//log.Debug("DEBUG -- create new connection: %d, %d, %d", p.active, p.maxActive, tmp)
		atomic.AddUint64(&p.stats.NumDial, 1)
		if conn, errCreate := p.dpool.connFactory.Create(p.server); errCreate != nil {
			p.markFailed(true)
			log.Errorf("[POOL] Failed to create new connection: %s", errCreate.Error())
			atomic.AddUint64(&p.stats.NumDialError, 1)
			atomic.AddInt32(&p.active, -1)
			return nil, errinfo.New(_ERROR_CODE_SHARD_CREATE_EXCEPTION, errCreate.Error())
		} else {
			c = conn
		}

		// Setup pooled connection
		p.markFailed(false)
		c.setDataSource(p)
		c.setBorrowed(true)
		return c, nil
	}
}

func (p *PoolShard) put(c Poolable, broken bool) error {
	// XXX: Check if this object is borrowed and mark returning MUST be atomic
	c.lock()
	if !c.isBorrowed() {
		c.unlock()
		return errReturnInvalid
	}
	c.setBorrowed(false)
	c.unlock()

	p.markFailed(broken)
	atomic.AddUint64(&p.stats.NumPut, 1)
	if broken {
		atomic.AddUint64(&p.stats.NumBroken, 1)
	}

	if broken || atomic.LoadUint32(&p.closed) == 1 {
		atomic.AddUint64(&p.stats.NumClose, 1)
		atomic.AddInt32(&p.active, -1)
		return p.dpool.connFactory.Close(c)
	}

	select {
	case p.idle <- c:
	default:
		atomic.AddUint64(&p.stats.NumEvict, 1)
		atomic.AddUint64(&p.stats.NumClose, 1)
		atomic.AddInt32(&p.active, -1)
		return p.dpool.connFactory.Close(c)
	}

	return nil
}

// Empty removes and calls Close() on all the connections currently in the pool.
// Assuming there are no other connections waiting to be Put back this method
// effectively closes and cleans up the pool.
func (p *PoolShard) empty() {
	for {
		select {
		case c := <-p.idle:
			p.dpool.connFactory.Close(c)
			atomic.AddInt32(&p.active, -1)
			atomic.AddUint64(&p.stats.NumClose, 1)
		default:
			return
		}
	}
}

// Get statistics for this pool shard
func (p *PoolShard) getStats() (stats PoolStats) {
	stats.Shard = p.server
	stats.NumActive = int(atomic.LoadInt32(&p.active))
	if atomic.LoadUint32(&p.available) == 1 {
		stats.Available = true
	} else {
		stats.Available = false
	}

	stats.NumGet = atomic.SwapUint64(&p.stats.NumGet, 0)
	stats.NumPut = atomic.SwapUint64(&p.stats.NumPut, 0)
	stats.NumBroken = atomic.SwapUint64(&p.stats.NumBroken, 0)
	stats.NumClose = atomic.SwapUint64(&p.stats.NumClose, 0)
	stats.NumDial = atomic.SwapUint64(&p.stats.NumDial, 0)
	stats.NumDialError = atomic.SwapUint64(&p.stats.NumDialError, 0)
	stats.NumEvict = atomic.SwapUint64(&p.stats.NumEvict, 0)
	return
}
