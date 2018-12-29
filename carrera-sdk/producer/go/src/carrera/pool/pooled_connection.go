package pool

import (
	"sync"
	"time"
)

// Poolable represents a connection to a server.
type Poolable interface {
	// @private
	lock()
	unlock()

	// @private
	setTime(t time.Time)
	getTime() time.Time

	// @private
	getDataSource() *PoolShard
	setDataSource(shard *PoolShard)

	// @private
	isBorrowed() bool
	setBorrowed(b bool)
}

// Implements Poolable interface
type PooledObject struct {
	dataSource *PoolShard
	t          time.Time
	borrowed   bool
	state      int
	mu         sync.Mutex
}

// @private
func (pc *PooledObject) lock() {
	pc.mu.Lock()
}

// @private
func (pc *PooledObject) unlock() {
	pc.mu.Unlock()
}

// @private
func (pc *PooledObject) setDataSource(shard *PoolShard) {
	pc.dataSource = shard
}

// @private
func (pc *PooledObject) getDataSource() (shard *PoolShard) {
	return pc.dataSource
}

// @private
func (pc *PooledObject) setTime(t time.Time) {
	pc.t = t
}

// @private
func (pc *PooledObject) getTime() (t time.Time) {
	t = pc.t
	return t
}

// @private
func (pc *PooledObject) isBorrowed() bool {
	return pc.borrowed
}

// @private
func (pc *PooledObject) setBorrowed(b bool) {
	pc.borrowed = b
}
