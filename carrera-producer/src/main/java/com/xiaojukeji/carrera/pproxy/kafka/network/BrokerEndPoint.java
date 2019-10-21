package com.xiaojukeji.carrera.pproxy.kafka.network;

public class BrokerEndPoint {

    public BrokerEndPoint() {
        //todo 用正则校验
    }

    public static Address parseHostPort(String hostPortStr) {
        String[] array = hostPortStr.split(":");
        Address address = new Address();
        address.setHost(array[0]);
        address.setPort(Integer.valueOf(array[1]));
        return address;
    }
}
