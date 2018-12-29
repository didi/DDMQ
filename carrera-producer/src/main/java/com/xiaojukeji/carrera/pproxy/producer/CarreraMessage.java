package com.xiaojukeji.carrera.pproxy.producer;

import com.alibaba.fastjson.annotation.JSONField;
import com.xiaojukeji.carrera.thrift.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageQueue;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.apache.rocketmq.common.message.MessageClientIDSetter.createUniqID;
import static org.apache.thrift.TBaseHelper.wrapsFullArray;


public class CarreraMessage {

    protected Message message;

    protected int retries;

    @JSONField(serialize = false)
    protected MessageQueue messageQueue;

    @JSONField(serialize = false)
    public String lastBrokerName = null;

    public CarreraMessage() {
        this.message = new Message();
    }

    public CarreraMessage(Message message) {
        this.message = message;
        this.retries = 0;
    }

    public String getTopic() {
        return message.topic;
    }

    public void setTopic(String topic) {
        message.topic = topic;
    }

    public String getKey() {
        return message.key;
    }

    public void setKey(String key) {
        message.key = key;
    }

    public String getValue() {
        return message.value;
    }

    public byte[] getBody() {
        if (!message.isSetBody()) {
            return null;
        }
        if (!wrapsFullArray(message.body)) {
            message.setBody(message.body);
        }
        return message.body.array();
    }

    public void setBody(byte[] body) {
        message.body = body == null ? null : ByteBuffer.wrap(body);
    }

    public void setValue(String value) {
        message.value = value;
    }

    public long getHashId() {
        return message.hashId;
    }

    public void setHashId(long hashId) {
        message.hashId = hashId;
    }

    public String getTags() {
        return message.tags;
    }

    public void setTags(String tags) {
        message.tags = tags;
    }

    public int getPartitionId() {
        return message.partitionId;
    }

    public void setPartitionId(int partitionId) {
        message.partitionId = partitionId;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }


    public Map<String, String> getMsgProperties() {
        return this.message.getProperties();
    }

    public void setMsgProperties(Map<String, String> properties) {
        this.message.setProperties(properties);
    }

    @JSONField(serialize = false)
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @JSONField(serialize = false)
    public String getLastBrokerName() {
        return lastBrokerName;
    }

    public void setLastBrokerName(String lastBrokerName) {
        this.lastBrokerName = lastBrokerName;
    }

    public byte[] binary() {
        reformatMessage(message);
        return getBody();
    }

    public int binarySize() {
        reformatMessage(message);
        return message.body.remaining();
    }

    public ByteBuffer bodyAsByteBuffer() {
        reformatMessage(message);
        return message.body.slice();
    }

    private static final ByteBuffer EMPTY_BODY = ByteBuffer.wrap(new byte[0]);

    /**
     * ensure message body is in ByteBuffer.
     */
    public static void reformatMessage(Message message) {
        if (message.body == null) {
            if (message.value == null) {
                message.body = EMPTY_BODY;
            } else {
                message.body = ByteBuffer.wrap(message.value.getBytes());
                message.value = null;
            }
        }
    }

    @JSONField(serialize = false)
    public Map<String, String> getRmqProperties() {
        HashMap<String, String> propertyMap = new HashMap<>();
        if (StringUtils.isNotBlank(getKey()))
            propertyMap.put(MessageConst.PROPERTY_KEYS, getKey());
        if (StringUtils.isNotBlank(getTags()))
            propertyMap.put(MessageConst.PROPERTY_TAGS, getTags());
        propertyMap.put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, createUniqID());

        Map<String, String> msgProperties = this.getMsgProperties();
        if (msgProperties != null) {
            propertyMap.putAll(msgProperties);
        }

        // PROPERTY_WAIT_STORE_MSG_OK 这个属性没用。
        //propertyMap.put(MessageConst.PROPERTY_WAIT_STORE_MSG_OK, Boolean.toString(true));
        return propertyMap;
    }

    @Override
    public String toString() {
        return "CarreraMessage [topic=" + message.topic +
                ", key=" + message.key +
                ", value=" + message.value +
                ", hashId=" + message.hashId +
                ", tags=" + message.tags +
                ", partitionId=" + message.partitionId +
                ", retries=" + retries +
                ", version=" + message.getVersion() +
                "]";
    }

    public String toShortString() {
        return "[topic=" + message.topic +
                (message.key == null ? "" : ", key=" + message.key) +
                ", v.len=" + binarySize() +
                (message.hashId == 0 ? "" : ", hashId=" + message.hashId) +
                (message.tags == null ? "" : ", tags=" + message.tags) +
                (message.partitionId == -2 ? "" : ", pId=" + message.partitionId) +
                (retries == 0 ? "" : ", retries=" + retries) +
                (message.getVersion() == null ? "" : ", version=" + message.getVersion()) +
                "]";
    }

    public boolean isRetry() {
        return !Strings.isEmpty(lastBrokerName);
    }
}