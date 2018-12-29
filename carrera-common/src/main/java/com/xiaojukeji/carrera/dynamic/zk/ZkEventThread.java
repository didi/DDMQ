package com.xiaojukeji.carrera.dynamic.zk;

/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


class ZkEventThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ZkEventThread.class);

    private BlockingQueue<ZkEvent> _events = new LinkedBlockingQueue<ZkEvent>();

    private static AtomicInteger _eventId = new AtomicInteger(0);

    static abstract class ZkEvent {

        private String _description;

        public ZkEvent(String description) {
            _description = description;
        }

        public abstract void run() throws Exception;

        @Override
        public String toString() {
            return "ZkEvent[" + _description + "]";
        }
    }

    ZkEventThread(String name) {
        setDaemon(true);
        setName("ZkClient-EventThread-" + getId() + "-" + name);
    }

    @Override
    public void run() {
        LOG.info("Starting ZkClient event thread.");
        try {
            while (!isInterrupted()) {
                ZkEventThread.ZkEvent zkEvent = _events.take();
                int eventId = _eventId.incrementAndGet();
                LOG.debug("Delivering event #" + eventId + " " + zkEvent);
                try {
                    zkEvent.run();
                } catch (InterruptedException e) {
                    interrupt();
                } catch (ZkInterruptedException e) {
                    interrupt();
                } catch (Throwable e) {
                    LOG.error("Error handling event " + zkEvent, e);
                }
                LOG.debug("Delivering event #" + eventId + " done");
            }
        } catch (InterruptedException e) {
            LOG.info("Terminate ZkClient event thread.");
        }
    }

    public void send(ZkEventThread.ZkEvent event) {
        if (!isInterrupted()) {
            LOG.debug("New event: " + event);
            _events.add(event);
        }
    }
}