package zerodown

import (
	"net"
	"time"

	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"go.intra.xiaojukeji.com/golang/goagain"
)

type ServeFun func(listener net.Listener) error

// The `Single` strategy (named because it calls `execve`(2) once) operates similarly to Nginx and Unicorn.  The parent forks a child, the child execs, and then the child kills the parent.  This is easy to understand but doesn't play nicely with Upstart and similar direct-supervision `init`(8) daemons.  It should play nicely with `systemd`.
func ZeroDown(logid int64, sf ServeFun, addr string) error {
	// Inherit a net.Listener from our parent process or listen a new.
	l, err := goagain.Listener()
	if nil != err {
		// Listen on a TCP or a UNIX domain socket (TCP here).
		l, err = net.Listen("tcp", addr)
		if err != nil {
			log.Errorf("%d, server went down due to: %v", logid, err)
			return err
		}
		log.Infof("%d, listening on: %v", logid, l.Addr())
		// Accept connections in a new goroutine.
		go sf(l)

	} else {
		// Resume accepting connections in a new goroutine.
		log.Infof("%d, resuming listening on: %v", logid, l.Addr())
		go sf(l)
		// Kill the parent, now that the child has started successfully.
		if err := goagain.Kill(); nil != err {
			log.Errorf("%d, error while kill the parent process: %v", logid, err)
			return err
		}

	}

	// Block the main goroutine awaiting signals.
	if _, err := goagain.Wait(l); nil != err {
		log.Errorf("%d, error while goagain.Wait: %v", logid, err)
		return err
	}

	// Do whatever's necessary to ensure a graceful exit like waiting for
	// goroutines to terminate or a channel to become closed.
	//
	// In this case, we'll simply stop listening and wait one second.
	// FIXME
	time.Sleep(1e9)
	log.Infof("Father process exit.")
	return nil
}
