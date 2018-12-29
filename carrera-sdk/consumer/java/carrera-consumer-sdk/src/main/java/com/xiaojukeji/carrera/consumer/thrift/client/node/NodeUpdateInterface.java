package com.xiaojukeji.carrera.consumer.thrift.client.node;

import java.util.List;


public interface NodeUpdateInterface {
    boolean updateNodes(List<Node> nodes) throws Exception;
}