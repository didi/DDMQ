package com.xiaojukeji.carrera.pproxy.utils;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class RoundRobinPickerList<E> extends ArrayList<E> {
    private final AtomicInteger pickIdx = new AtomicInteger(0);

    public RoundRobinPickerList() {
    }

    public RoundRobinPickerList(int initialCapacity) {
        super(initialCapacity);
    }

    public E pick() {
        int idx = pickIdx.getAndIncrement();
        if (idx < 0) {
            pickIdx.set(0);
        }

        return get(idx % size());
    }
}