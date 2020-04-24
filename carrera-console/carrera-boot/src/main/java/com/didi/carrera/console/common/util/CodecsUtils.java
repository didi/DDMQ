package com.didi.carrera.console.common.util;


public class CodecsUtils {
    /**
     * This method is used to judge whether byte[] is utf-8 encoding principle
     * @param bytes
     * @return true for bytes is utf-8 encoding principle
     */
    public static boolean isUtf8(byte[] bytes) {
        int len = bytes.length;
        for (int i = 0; i < len; ++i) {
            if (((bytes[i] >>> 7) & 0x1) == 0) { // 0xxxxxxxx
                continue;
            } else if (((bytes[i] >>> 5) & 0x7) == 0x6) { // 110xxxxx
                for (int j = 0; j < 1; ++j) {
                    if (isRegular(bytes[i+1])) {
                        i += 1;
                    } else {
                        return false;
                    }
                }
                continue;
            } else if (((bytes[i] >>> 4) & 0xf) == 0xe) { // 1110xxxx
                for (int j = 0; j < 2; ++j) {
                    if (isRegular(bytes[i+1])) {
                        i += 1;
                    } else {
                        return false;
                    }
                }
                continue;
            } else if (((bytes[i] >>> 3) & 0x1f) == 0x1e) { // 11110xxx
                for (int j = 0; j < 3; ++j) {
                    if (isRegular(bytes[i+1])) {
                        i += 1;
                    } else {
                        return false;
                    }
                }
                continue;
            } else if (((bytes[i] >>> 2) & 0x3f) == 0x3e) { // 1110xxxx
                for (int j = 0; j < 4; ++j) {
                    if (isRegular(bytes[i+1])) {
                        i += 1;
                    } else {
                        return false;
                    }
                }
                continue;
            } else if (((bytes[i] >>> 1) & 0x7f) == 0x7e) { // 1110xxxx
                for (int j = 0; j < 5; ++j) {
                    if (isRegular(bytes[i+1])) {
                        i += 1;
                    } else {
                        return false;
                    }
                }
                continue;
            }
        }
        return true;
    }

    /**
     * This method supports isUtf8 method to judge whether byte b is 10xxxxxx
     * @param b
     * @return result of judgement
     */
    private static boolean isRegular(byte b) {
        return ((b >>> 6) & 0x3) == 0x2;
    }
}