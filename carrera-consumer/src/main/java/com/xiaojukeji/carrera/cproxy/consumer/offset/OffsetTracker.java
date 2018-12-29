package com.xiaojukeji.carrera.cproxy.consumer.offset;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;


public class OffsetTracker {
    private AtomicLong start;
    private AtomicLong finish;
    private AtomicLong committedOffset;
    private ConcurrentSkipListSet<Long> marker;

    OffsetTracker(boolean async) {
        if (async) {
            marker = new ConcurrentSkipListSet<>();
        }
        start = new AtomicLong(-1);
        finish = new AtomicLong(-1);
        committedOffset = new AtomicLong(-1);
    }

    boolean markStart(long offset) {
        updateOnlyIncrease(start, offset);
        committedOffset.compareAndSet(-1, offset);
        return marker == null || marker.add(offset);
    }

    boolean markFinish(long offset) {
        updateOnlyIncrease(finish, offset);
        return marker == null || marker.remove(offset);
    }

    public long getMaxStart() {
        return start.get();
    }

    public long getMaxFinish() {
        return finish.get();
    }

    public long getMaxCommittableFinish() {
        if (finish.get() < 0) {
            return -1;
        }
        if (marker != null) {
            try {
                return marker.first();
            } catch (NoSuchElementException ignored) {
            }
        }
        return finish.get() + 1;
    }

    public long getCommittedOffset() {
        return committedOffset.get();
    }

    public void setCommittedOffset(long committedOffset) {
        this.committedOffset.set(committedOffset);
    }

    private static void updateOnlyIncrease(AtomicLong al, long offset) {
        long oldOffset;
        do {
            oldOffset = al.get();
        } while (oldOffset < offset && !al.compareAndSet(oldOffset, offset));
    }

    @Override
    public String toString() {
        return "OffsetTracker{" +
                "start=" + start +
                ", finish=" + finish +
                ", committedOffset=" + committedOffset +
                ", marker.size=" + (marker == null ? null : marker.size()) +
                '}';
    }
}