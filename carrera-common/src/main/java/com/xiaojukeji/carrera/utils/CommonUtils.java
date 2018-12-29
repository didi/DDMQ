package com.xiaojukeji.carrera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class CommonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    public static String getHostAddress() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
                    if (ip.getHostAddress().equals("127.0.0.1")) {
                        continue;
                    }
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("get ip failed", e);
        }
        return null;
    }
}