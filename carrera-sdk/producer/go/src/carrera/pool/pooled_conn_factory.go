package pool

// This idea is borrowed from the famous Apache Commons Pool
type PooledConnFactory interface {
	// Function to create a new pooled client for server @serverAddr
	Create(addr string) (Poolable, error)

	// testOnBorrow is an optional application supplied function for checking
	// the health of an idle connection before the connection is used again by
	// the application. Argument t is the time that the connection was returned
	// to the pool. If the function returns an error, then the connection is
	// closed.
	Validate(c Poolable) error

	// Function to destroy a connection
	Close(c Poolable) error
}
