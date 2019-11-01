package com.xiaojukeji.carrera.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class CommonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    public static String getHostNameWithIpDefault() {
        //host
        String host = "unknown_host";
        try {
            String hostGet = InetAddress.getLocalHost().getHostName();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(hostGet)) {
                host = hostGet;
            }
        } catch (Exception ex) {
            LOGGER.error("get host name failed", ex);
        }

        if ("unknown_host".equals(host) || host.toLowerCase().equals("localhost")) {
            try {
                String ip = getHostAddress();
                if (StringUtils.isNotEmpty(ip)) {
                    host = ip;
                }
            } catch (Exception ex) {
                LOGGER.error("get ip failed");
            }
        }
        return host;
    }
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