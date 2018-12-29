package util

import "sync"

// A "thread" safe map of type string:Anything.
type ConcurrentMap struct {
	m map[string]interface{}
	sync.RWMutex // Read Write mutex, guards access to internal map.
}

// Creates a new concurrent map.
func NewConcurrentMap() *ConcurrentMap {
	return &ConcurrentMap{m: make(map[string]interface{})}
}

// Sets the given value under the specified key.
func (cmap *ConcurrentMap) Set(key string, value interface{}) {
	cmap.Lock()
	defer cmap.Unlock()
	cmap.m[key] = value
}

// Retrieves an element from map under given key.
func (cmap *ConcurrentMap) Get(key string) (interface{}, bool) {
	cmap.RLock()
	defer cmap.RUnlock()
	val, ok := cmap.m[key]
	return val, ok
}

// Looks up an key
func (cmap *ConcurrentMap) HasKey(key string) bool {
	cmap.RLock()
	defer cmap.Unlock()
	_, ok := cmap.m[key]
	return ok
}

// Returns a copy of concurrent map.
func (cmap *ConcurrentMap) Iter() (m map[string]interface{}) {
	m = make(map[string]interface{})
	cmap.RLock()
	for key, val := range cmap.m {
		m[key] = val
	}
	cmap.RUnlock()
	return m
}


