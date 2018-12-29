package com.xiaojukeji.carrera.config.v4.pproxy;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.TopicConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


public class TopicInfoConfiguration implements ConfigurationValidator, Cloneable {

    private List<TopicConfig> topics = Lists.newArrayList();

    public List<TopicConfig> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicConfig> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "TopicInfoConfiguration{" +
                "topics=" + topics +
                '}';
    }

    public boolean validate() throws ConfigException {
        if (CollectionUtils.isEmpty(topics)) {
            return false;
        }

        for (TopicConfig config : topics) {
            if (!config.validate()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicInfoConfiguration that = (TopicInfoConfiguration) o;

        return topics != null ? topics.equals(that.topics) : that.topics == null;
    }

    @Override
    public int hashCode() {
        return topics != null ? topics.hashCode() : 0;
    }
}