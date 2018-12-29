package com.xiaojukeji.chronos.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ByteUtils {

    public static List<byte[]> divideArray(byte[] source, int chunkSize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunkSize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunkSize;
        }

        return result;
    }
}