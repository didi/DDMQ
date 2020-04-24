package com.didi.carrera.console.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;


public class HostUtils {
    private static final Logger logger = LoggerFactory.getLogger(HostUtils.class);

    public static final Pattern IP_PATTERN = Pattern.compile("^[\\d.:]+$");

    public static String getIp(String host) {
        if (IP_PATTERN.matcher(host).matches()) {
            return host;
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("get ip exception", e);
            return host;
        }
    }

    public static String getIpPortFromHost(String host, int defaultPort) {
        String[] hostArr = host.split(":");
        String hostIp = getIp(hostArr[0]);
        if (hostArr.length == 2) {
            hostIp = hostIp + ":" + hostArr[1];
        } else {
            hostIp = hostIp + ":" + defaultPort;
        }
        return hostIp;
    }
}