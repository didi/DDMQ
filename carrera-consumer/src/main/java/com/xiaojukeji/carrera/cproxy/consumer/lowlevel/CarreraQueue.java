package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;


public class CarreraQueue {

    private String qid;

    private String topic;

    public CarreraQueue(String qid, String topic) {
        super();
        this.qid = qid;
        this.topic = topic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qid == null) ? 0 : qid.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CarreraQueue other = (CarreraQueue) obj;
        if (qid == null) {
            if (other.qid != null)
                return false;
        } else if (!qid.equals(other.qid))
            return false;
        if (topic == null) {
            return other.topic == null;
        } else return topic.equals(other.topic);
    }

    public String getQid() {
        return qid;
    }

    public String getTopic() {
        return topic;
    }

}