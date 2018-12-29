package util

import (
	"testing"
	"fmt"
	"time"
	"sync"
)

var wg sync.WaitGroup

func TestConcurrentRW(t *testing.T) {
	cm := NewConcurrentMap()

	wg.Add(1)
	go r(cm)

	wg.Add(1)
	go w(cm)

	wg.Add(1)
	go w(cm)

	wg.Wait()
}

func TestConcurrentWIter(t *testing.T) {
	cm := NewConcurrentMap()

	wg.Add(1)
	go iter(cm)

	wg.Add(1)
	go w(cm)

	wg.Add(1)
	go w(cm)

	wg.Wait()
}

func r(cm *ConcurrentMap) {
	for {
		val, ok := cm.Get("a")
		if ok {
			fmt.Println("a:", val)
		}
		time.Sleep(time.Millisecond * 3)
	}
	wg.Done()
}

func w(cm *ConcurrentMap) {
	for {
		cm.Set(fmt.Sprintf("%d", time.Now().Unix()), time.Now().Unix())
	}
	wg.Done()
}

func iter(cm *ConcurrentMap) {
	for {
		m := cm.Iter()
		for k, v := range m {
			fmt.Println(k, v)
		}
		fmt.Println("-------------------------------")
		time.Sleep(time.Millisecond * 5)
	}
	wg.Done()
}

