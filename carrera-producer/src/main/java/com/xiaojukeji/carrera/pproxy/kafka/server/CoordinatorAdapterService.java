package com.xiaojukeji.carrera.pproxy.kafka.server;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.requests.FindCoordinatorResponse;

import java.util.function.Consumer;

public class CoordinatorAdapterService {

    public FindCoordinatorResponse createFindCoordinatorResponse() {
        //mock 消费分区信息,kafka是找到分区所在的leader信息，这里直接传pproxy信息就好
        Node node = new Node(0,"127.0.0.1", 9092);
        return new FindCoordinatorResponse(Errors.NONE, node);
    }

}
