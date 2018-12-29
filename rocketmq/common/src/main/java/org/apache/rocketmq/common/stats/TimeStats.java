/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.stats;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;



public class TimeStats {
    final private int sampleNumber;
    final private long[] data;

    private volatile double sampleRate = 0;
    private double[] percents = new double[]{0.25, 0.5, 0.75, 0.90, 0.95, 0.99};

    private AtomicInteger cnt = new AtomicInteger(0);
    private AtomicInteger maxIdx = new AtomicInteger(0);
    private AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    private AtomicLong max = new AtomicLong(Long.MIN_VALUE);
    private AtomicLong sum = new AtomicLong(0);
    StringBuilder resultBuilder = new StringBuilder(512);

    public TimeStats(int sampleNumber) {
        this.sampleNumber = sampleNumber;
        data = new long[sampleNumber];
        sampleRate = 1;

    }

    public void add(long value) {
        cnt.incrementAndGet();
        sum.getAndAdd(value);
        long tmp;
        while (value < (tmp = min.get())) min.compareAndSet(tmp, value);
        while (value > (tmp = max.get())) max.compareAndSet(tmp, value);
        int idx;
        if (sampleRate < 1) {
            idx = (int) (cnt.get() * sampleRate) % sampleNumber;
        } else {
            idx = cnt.get() % sampleNumber;
        }
        int t;
        while (idx > (t = maxIdx.get())) maxIdx.compareAndSet(t, idx);
        data[idx] = value;
    }


    public String reportAndReset(double rate) {
        resultBuilder.setLength(0);
        if (cnt.get() == 0) {
            return "cnt:0";
        }
        resultBuilder.append(String.format("cnt:%6d|avg:%6.2f|max:%6.2f|min:%6.3f|",
            cnt.get(), rate * sum.get() / cnt.get(),
            rate * max.get(),
            rate * min.get()
        ));

        int dataLen = maxIdx.get() + 1;
        long start = System.currentTimeMillis();
        Arrays.sort(data, 0, dataLen);
        long elapse = System.currentTimeMillis() - start;
        for (double percent : percents) {
            int idx = (int) (dataLen * percent);
            resultBuilder.append(String.format("p%2.0f:%6.2f|", percent * 100, rate * data[idx]));
        }
        resultBuilder.append(String.format("sort:%2d|", elapse));

        maxIdx.set(0);
        min.set(Long.MAX_VALUE);
        max.set(Long.MIN_VALUE);
        sum.set(0);
        cnt.set(0);
        return resultBuilder.toString();
    }

    private void updateSampleRate(AtomicInteger lastCnt) {
        sampleRate = 1.0 * sampleNumber / lastCnt.get();
    }


    public static class RolloverTimeStats {
        private TimeStats[] stats = new TimeStats[2];
        volatile int idx;

        public RolloverTimeStats(int sampleNumber) {
            stats[0] = new TimeStats(sampleNumber);
            stats[1] = new TimeStats(sampleNumber);
            idx = 0;
        }

        public void add(long value) {
            stats[idx].add(value);
        }

        public synchronized String reportAndReset() {
            return reportAndReset(1.0f);
        }

        public synchronized String reportAndReset(double rate) {
            stats[1 - idx].updateSampleRate(stats[idx].cnt);
            idx = 1 - idx;
            return stats[1 - idx].reportAndReset(rate);
        }
    }


    public static void main(String[] args) {
        RolloverTimeStats stats = new RolloverTimeStats(10000);
        long[] data = new long[]{1, 2, 3, 4, 5};
        for (long datum : data) {
            stats.add(datum);
        }
        System.out.printf("%s\n", stats.reportAndReset());

        for (long datum : data) stats.add(datum);
        System.out.printf("%s\n", stats.reportAndReset());
        for (int i = 0; i < 1000; i++) stats.add(i + 1);
        System.out.printf("%s\n", stats.reportAndReset());
        for (int i = 0; i < 100000; i++) stats.add((long) (Math.random() * 100));
        System.out.printf("%s\n", stats.reportAndReset());

        for (int i = 0; i < 100000; i++) stats.add((long) (Math.random() * 100));
        System.out.printf("%s\n", stats.reportAndReset());

        for (int i = 0; i < 1000; i++) stats.add(i + 1);
        System.out.printf("%s\n", stats.reportAndReset());

        for (int i = 0; i < 1000; i++) stats.add(i + 1);
        System.out.printf("%s\n", stats.reportAndReset());
    }
}
