package com.xiaojukeji.chronos.benchmark;


public class LeakyLimiter {
    private long timeStamp;
    private int capacity;
    private int rate;
    private int water;

    public LeakyLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.water = 0;
        this.timeStamp = getNowTime();
    }

    public boolean tryLimiter() {
        long now = getNowTime();
        water = Math.max(0, water - (int) ((now - timeStamp) * 1.0 / 1000 * rate));
        timeStamp = now;
        if ((water + 1) < capacity) {
            water++;
            return true;
        } else {
            return false;
        }
    }

    private long getNowTime() {
        return System.currentTimeMillis();
    }
}