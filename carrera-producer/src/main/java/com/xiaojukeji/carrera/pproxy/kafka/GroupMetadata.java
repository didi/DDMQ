package com.xiaojukeji.carrera.pproxy.kafka;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupMetadata {
    private Map<String,MemberMetadata> members = new ConcurrentHashMap<>();//这些结合cproxy来定，可以直接查看cproxy的ip
    private Map<TopicPartition, OffsetAndMetadata> offsets = new ConcurrentHashMap<>();

    public Map<String, MemberMetadata> getMembers() {
        return members;
    }

    public void setMembers(Map<String, MemberMetadata> members) {
        this.members = members;
    }

    public Map<TopicPartition, OffsetAndMetadata> getOffsets() {
        return offsets;
    }

    public void setOffsets(Map<TopicPartition, OffsetAndMetadata> offsets) {
        this.offsets = offsets;
    }
}
