package pool
import (
	"time"
)

type PoolConfig struct {
	// Maximum idle connections per shard
	MaxIdle int

	// The maximum number of active connections that can be allocated from per pool shard at the same time.
	// The default value is 100
	MaxActive int

	// Timeout to evict idle connections
	IdleTimeout time.Duration

	// Test if connection broken on borrow
	// If set this flag, the "test" function should also provided.
	TestOnBorrow bool

	// Number of max fails threshold to triger health check
	MaxFails int
}

var DefaultPoolConfig = PoolConfig{
	MaxIdle:     50,
	MaxActive:   100,
	IdleTimeout: 300 * time.Second,
	MaxFails:    5,
}

