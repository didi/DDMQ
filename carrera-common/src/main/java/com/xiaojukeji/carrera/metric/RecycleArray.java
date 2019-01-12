package com.xiaojukeji.carrera.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RecycleArray {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecycleArray.class);
    private static final int INIT_LENGTH = 1024;
    private static final int MAX_LENGTH = 100 * 1024;
    private static final int STEP = 512;
    private static final double RESIZE_PERCENT = 0.2;
    private static final int WRITE_DATA_RECORD_COUNT_MAX = 3;
    private long array[];
    private int index = -1;

    private int writeDataCount[] = new int[WRITE_DATA_RECORD_COUNT_MAX];
    private int writeDataCountIndex = -1;
    private int cap = 0;

    private long max = Long.MIN_VALUE;
    private long min = Long.MAX_VALUE;

    public RecycleArray(int cap) {
        if (cap > 0) {
            this.array = new long[cap];
            this.cap = cap;
        } else {
            array = new long[INIT_LENGTH];
            this.cap = INIT_LENGTH;
        }

        for (int i = 0; i < WRITE_DATA_RECORD_COUNT_MAX; i++) {
            writeDataCount[i] = this.cap;
        }
    }

    public RecycleArray() {
        this(0);
    }

    public synchronized void put(long value) {
        array[(++index) % cap] = value;

        if (value > max) {
            max = value;
        }

        if (value < min) {
            min = value;
        }
    }

    public synchronized void reset() {
        writeDataCount[(++writeDataCountIndex) % WRITE_DATA_RECORD_COUNT_MAX] = index;
        index = -1;
        resize();
        max = Long.MIN_VALUE;
        min = Long.MAX_VALUE;
    }

    //处理完过程中，不能有reset
    public long[] getData() {
        return array;
    }

    public int getLength() {
        if (index >= cap) {
            return cap;
        } else {
            return index;
        }
    }

    public long getMax() {
        return max;
    }

    public long getMin() {
        return min;
    }

    public int getCap() {
        return cap;
    }

    private void resize() {
        int max = writeDataCount[0], min = writeDataCount[0];

        for (int writeCount : writeDataCount) {
            if (max < writeCount) {
                max = writeCount;
            }
            if (min > writeCount) {
                min = writeCount;
            }
        }

        //resize, 尽量避免频繁抖动
        if (max < cap - 2 * STEP && cap - max > cap * RESIZE_PERCENT) {
            int newCap = max - (max % STEP) + STEP;
            LOGGER.info("resize, shrink old={}, new={}", cap, newCap);
            array = new long[newCap];
            cap = newCap;
        } else if (min > cap && cap < MAX_LENGTH) {
            int newCap = min - (min % STEP) + STEP;
            newCap = newCap > MAX_LENGTH ? MAX_LENGTH : newCap;
            LOGGER.info("resize, expand old={}, new={}", cap, newCap);
            array = new long[newCap];
            cap = newCap;
        }
    }
}