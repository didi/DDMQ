package com.didi.carrera.console.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TopicConfCriteria {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer start;

    protected Integer end;

    public TopicConfCriteria() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        return new Criteria();
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public void setStart(Integer start) {
        this.start=start;
    }

    public Integer getStart() {
        return start;
    }

    public void setEnd(Integer end) {
        this.end=end;
    }

    public Integer getEnd() {
        return end;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andTopicIdIsNull() {
            addCriterion("topic_id is null");
            return (Criteria) this;
        }

        public Criteria andTopicIdIsNotNull() {
            addCriterion("topic_id is not null");
            return (Criteria) this;
        }

        public Criteria andTopicIdEqualTo(Long value) {
            addCriterion("topic_id =", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdNotEqualTo(Long value) {
            addCriterion("topic_id <>", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdGreaterThan(Long value) {
            addCriterion("topic_id >", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdGreaterThanOrEqualTo(Long value) {
            addCriterion("topic_id >=", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdLessThan(Long value) {
            addCriterion("topic_id <", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdLessThanOrEqualTo(Long value) {
            addCriterion("topic_id <=", value, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdIn(List<Long> values) {
            addCriterion("topic_id in", values, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdNotIn(List<Long> values) {
            addCriterion("topic_id not in", values, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdBetween(Long value1, Long value2) {
            addCriterion("topic_id between", value1, value2, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicIdNotBetween(Long value1, Long value2) {
            addCriterion("topic_id not between", value1, value2, "topicId");
            return (Criteria) this;
        }

        public Criteria andTopicNameIsNull() {
            addCriterion("topic_name is null");
            return (Criteria) this;
        }

        public Criteria andTopicNameIsNotNull() {
            addCriterion("topic_name is not null");
            return (Criteria) this;
        }

        public Criteria andTopicNameEqualTo(String value) {
            addCriterion("topic_name =", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameNotEqualTo(String value) {
            addCriterion("topic_name <>", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameGreaterThan(String value) {
            addCriterion("topic_name >", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameGreaterThanOrEqualTo(String value) {
            addCriterion("topic_name >=", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameLessThan(String value) {
            addCriterion("topic_name <", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameLessThanOrEqualTo(String value) {
            addCriterion("topic_name <=", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameLike(String value) {
            addCriterion("topic_name like", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameNotLike(String value) {
            addCriterion("topic_name not like", value, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameIn(List<String> values) {
            addCriterion("topic_name in", values, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameNotIn(List<String> values) {
            addCriterion("topic_name not in", values, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameBetween(String value1, String value2) {
            addCriterion("topic_name between", value1, value2, "topicName");
            return (Criteria) this;
        }

        public Criteria andTopicNameNotBetween(String value1, String value2) {
            addCriterion("topic_name not between", value1, value2, "topicName");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNull() {
            addCriterion("cluster_id is null");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNotNull() {
            addCriterion("cluster_id is not null");
            return (Criteria) this;
        }

        public Criteria andClusterIdEqualTo(Long value) {
            addCriterion("cluster_id =", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotEqualTo(Long value) {
            addCriterion("cluster_id <>", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThan(Long value) {
            addCriterion("cluster_id >", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThanOrEqualTo(Long value) {
            addCriterion("cluster_id >=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThan(Long value) {
            addCriterion("cluster_id <", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThanOrEqualTo(Long value) {
            addCriterion("cluster_id <=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIn(List<Long> values) {
            addCriterion("cluster_id in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotIn(List<Long> values) {
            addCriterion("cluster_id not in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdBetween(Long value1, Long value2) {
            addCriterion("cluster_id between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotBetween(Long value1, Long value2) {
            addCriterion("cluster_id not between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNull() {
            addCriterion("cluster_name is null");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNotNull() {
            addCriterion("cluster_name is not null");
            return (Criteria) this;
        }

        public Criteria andClusterNameEqualTo(String value) {
            addCriterion("cluster_name =", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotEqualTo(String value) {
            addCriterion("cluster_name <>", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThan(String value) {
            addCriterion("cluster_name >", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThanOrEqualTo(String value) {
            addCriterion("cluster_name >=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThan(String value) {
            addCriterion("cluster_name <", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThanOrEqualTo(String value) {
            addCriterion("cluster_name <=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLike(String value) {
            addCriterion("cluster_name like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotLike(String value) {
            addCriterion("cluster_name not like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameIn(List<String> values) {
            addCriterion("cluster_name in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotIn(List<String> values) {
            addCriterion("cluster_name not in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameBetween(String value1, String value2) {
            addCriterion("cluster_name between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotBetween(String value1, String value2) {
            addCriterion("cluster_name not between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andMqServerIdIsNull() {
            addCriterion("mq_server_id is null");
            return (Criteria) this;
        }

        public Criteria andMqServerIdIsNotNull() {
            addCriterion("mq_server_id is not null");
            return (Criteria) this;
        }

        public Criteria andMqServerIdEqualTo(Long value) {
            addCriterion("mq_server_id =", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdNotEqualTo(Long value) {
            addCriterion("mq_server_id <>", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdGreaterThan(Long value) {
            addCriterion("mq_server_id >", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdGreaterThanOrEqualTo(Long value) {
            addCriterion("mq_server_id >=", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdLessThan(Long value) {
            addCriterion("mq_server_id <", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdLessThanOrEqualTo(Long value) {
            addCriterion("mq_server_id <=", value, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdIn(List<Long> values) {
            addCriterion("mq_server_id in", values, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdNotIn(List<Long> values) {
            addCriterion("mq_server_id not in", values, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdBetween(Long value1, Long value2) {
            addCriterion("mq_server_id between", value1, value2, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerIdNotBetween(Long value1, Long value2) {
            addCriterion("mq_server_id not between", value1, value2, "mqServerId");
            return (Criteria) this;
        }

        public Criteria andMqServerNameIsNull() {
            addCriterion("mq_server_name is null");
            return (Criteria) this;
        }

        public Criteria andMqServerNameIsNotNull() {
            addCriterion("mq_server_name is not null");
            return (Criteria) this;
        }

        public Criteria andMqServerNameEqualTo(String value) {
            addCriterion("mq_server_name =", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameNotEqualTo(String value) {
            addCriterion("mq_server_name <>", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameGreaterThan(String value) {
            addCriterion("mq_server_name >", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameGreaterThanOrEqualTo(String value) {
            addCriterion("mq_server_name >=", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameLessThan(String value) {
            addCriterion("mq_server_name <", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameLessThanOrEqualTo(String value) {
            addCriterion("mq_server_name <=", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameLike(String value) {
            addCriterion("mq_server_name like", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameNotLike(String value) {
            addCriterion("mq_server_name not like", value, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameIn(List<String> values) {
            addCriterion("mq_server_name in", values, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameNotIn(List<String> values) {
            addCriterion("mq_server_name not in", values, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameBetween(String value1, String value2) {
            addCriterion("mq_server_name between", value1, value2, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andMqServerNameNotBetween(String value1, String value2) {
            addCriterion("mq_server_name not between", value1, value2, "mqServerName");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdIsNull() {
            addCriterion("server_idc_id is null");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdIsNotNull() {
            addCriterion("server_idc_id is not null");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdEqualTo(Long value) {
            addCriterion("server_idc_id =", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdNotEqualTo(Long value) {
            addCriterion("server_idc_id <>", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdGreaterThan(Long value) {
            addCriterion("server_idc_id >", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdGreaterThanOrEqualTo(Long value) {
            addCriterion("server_idc_id >=", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdLessThan(Long value) {
            addCriterion("server_idc_id <", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdLessThanOrEqualTo(Long value) {
            addCriterion("server_idc_id <=", value, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdIn(List<Long> values) {
            addCriterion("server_idc_id in", values, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdNotIn(List<Long> values) {
            addCriterion("server_idc_id not in", values, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdBetween(Long value1, Long value2) {
            addCriterion("server_idc_id between", value1, value2, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcIdNotBetween(Long value1, Long value2) {
            addCriterion("server_idc_id not between", value1, value2, "serverIdcId");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameIsNull() {
            addCriterion("server_idc_name is null");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameIsNotNull() {
            addCriterion("server_idc_name is not null");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameEqualTo(String value) {
            addCriterion("server_idc_name =", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameNotEqualTo(String value) {
            addCriterion("server_idc_name <>", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameGreaterThan(String value) {
            addCriterion("server_idc_name >", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameGreaterThanOrEqualTo(String value) {
            addCriterion("server_idc_name >=", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameLessThan(String value) {
            addCriterion("server_idc_name <", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameLessThanOrEqualTo(String value) {
            addCriterion("server_idc_name <=", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameLike(String value) {
            addCriterion("server_idc_name like", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameNotLike(String value) {
            addCriterion("server_idc_name not like", value, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameIn(List<String> values) {
            addCriterion("server_idc_name in", values, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameNotIn(List<String> values) {
            addCriterion("server_idc_name not in", values, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameBetween(String value1, String value2) {
            addCriterion("server_idc_name between", value1, value2, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andServerIdcNameNotBetween(String value1, String value2) {
            addCriterion("server_idc_name not between", value1, value2, "serverIdcName");
            return (Criteria) this;
        }

        public Criteria andClientIdcIsNull() {
            addCriterion("client_idc is null");
            return (Criteria) this;
        }

        public Criteria andClientIdcIsNotNull() {
            addCriterion("client_idc is not null");
            return (Criteria) this;
        }

        public Criteria andClientIdcEqualTo(String value) {
            addCriterion("client_idc =", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcNotEqualTo(String value) {
            addCriterion("client_idc <>", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcGreaterThan(String value) {
            addCriterion("client_idc >", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcGreaterThanOrEqualTo(String value) {
            addCriterion("client_idc >=", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcLessThan(String value) {
            addCriterion("client_idc <", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcLessThanOrEqualTo(String value) {
            addCriterion("client_idc <=", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcLike(String value) {
            addCriterion("client_idc like", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcNotLike(String value) {
            addCriterion("client_idc not like", value, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcIn(List<String> values) {
            addCriterion("client_idc in", values, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcNotIn(List<String> values) {
            addCriterion("client_idc not in", values, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcBetween(String value1, String value2) {
            addCriterion("client_idc between", value1, value2, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andClientIdcNotBetween(String value1, String value2) {
            addCriterion("client_idc not between", value1, value2, "clientIdc");
            return (Criteria) this;
        }

        public Criteria andProduceTpsIsNull() {
            addCriterion("produce_tps is null");
            return (Criteria) this;
        }

        public Criteria andProduceTpsIsNotNull() {
            addCriterion("produce_tps is not null");
            return (Criteria) this;
        }

        public Criteria andProduceTpsEqualTo(Integer value) {
            addCriterion("produce_tps =", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsNotEqualTo(Integer value) {
            addCriterion("produce_tps <>", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsGreaterThan(Integer value) {
            addCriterion("produce_tps >", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsGreaterThanOrEqualTo(Integer value) {
            addCriterion("produce_tps >=", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsLessThan(Integer value) {
            addCriterion("produce_tps <", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsLessThanOrEqualTo(Integer value) {
            addCriterion("produce_tps <=", value, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsIn(List<Integer> values) {
            addCriterion("produce_tps in", values, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsNotIn(List<Integer> values) {
            addCriterion("produce_tps not in", values, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsBetween(Integer value1, Integer value2) {
            addCriterion("produce_tps between", value1, value2, "produceTps");
            return (Criteria) this;
        }

        public Criteria andProduceTpsNotBetween(Integer value1, Integer value2) {
            addCriterion("produce_tps not between", value1, value2, "produceTps");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeIsNull() {
            addCriterion("msg_avg_size is null");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeIsNotNull() {
            addCriterion("msg_avg_size is not null");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeEqualTo(Integer value) {
            addCriterion("msg_avg_size =", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeNotEqualTo(Integer value) {
            addCriterion("msg_avg_size <>", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeGreaterThan(Integer value) {
            addCriterion("msg_avg_size >", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("msg_avg_size >=", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeLessThan(Integer value) {
            addCriterion("msg_avg_size <", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeLessThanOrEqualTo(Integer value) {
            addCriterion("msg_avg_size <=", value, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeIn(List<Integer> values) {
            addCriterion("msg_avg_size in", values, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeNotIn(List<Integer> values) {
            addCriterion("msg_avg_size not in", values, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeBetween(Integer value1, Integer value2) {
            addCriterion("msg_avg_size between", value1, value2, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgAvgSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("msg_avg_size not between", value1, value2, "msgAvgSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeIsNull() {
            addCriterion("msg_max_size is null");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeIsNotNull() {
            addCriterion("msg_max_size is not null");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeEqualTo(Integer value) {
            addCriterion("msg_max_size =", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeNotEqualTo(Integer value) {
            addCriterion("msg_max_size <>", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeGreaterThan(Integer value) {
            addCriterion("msg_max_size >", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("msg_max_size >=", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeLessThan(Integer value) {
            addCriterion("msg_max_size <", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeLessThanOrEqualTo(Integer value) {
            addCriterion("msg_max_size <=", value, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeIn(List<Integer> values) {
            addCriterion("msg_max_size in", values, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeNotIn(List<Integer> values) {
            addCriterion("msg_max_size not in", values, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeBetween(Integer value1, Integer value2) {
            addCriterion("msg_max_size between", value1, value2, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andMsgMaxSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("msg_max_size not between", value1, value2, "msgMaxSize");
            return (Criteria) this;
        }

        public Criteria andStateIsNull() {
            addCriterion("state is null");
            return (Criteria) this;
        }

        public Criteria andStateIsNotNull() {
            addCriterion("state is not null");
            return (Criteria) this;
        }

        public Criteria andStateEqualTo(Byte value) {
            addCriterion("state =", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotEqualTo(Byte value) {
            addCriterion("state <>", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateGreaterThan(Byte value) {
            addCriterion("state >", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateGreaterThanOrEqualTo(Byte value) {
            addCriterion("state >=", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateLessThan(Byte value) {
            addCriterion("state <", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateLessThanOrEqualTo(Byte value) {
            addCriterion("state <=", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateIn(List<Byte> values) {
            addCriterion("state in", values, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotIn(List<Byte> values) {
            addCriterion("state not in", values, "state");
            return (Criteria) this;
        }

        public Criteria andStateBetween(Byte value1, Byte value2) {
            addCriterion("state between", value1, value2, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotBetween(Byte value1, Byte value2) {
            addCriterion("state not between", value1, value2, "state");
            return (Criteria) this;
        }

        public Criteria andConfigIsNull() {
            addCriterion("config is null");
            return (Criteria) this;
        }

        public Criteria andConfigIsNotNull() {
            addCriterion("config is not null");
            return (Criteria) this;
        }

        public Criteria andConfigEqualTo(String value) {
            addCriterion("config =", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigNotEqualTo(String value) {
            addCriterion("config <>", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigGreaterThan(String value) {
            addCriterion("config >", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigGreaterThanOrEqualTo(String value) {
            addCriterion("config >=", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigLessThan(String value) {
            addCriterion("config <", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigLessThanOrEqualTo(String value) {
            addCriterion("config <=", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigLike(String value) {
            addCriterion("config like", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigNotLike(String value) {
            addCriterion("config not like", value, "config");
            return (Criteria) this;
        }

        public Criteria andConfigIn(List<String> values) {
            addCriterion("config in", values, "config");
            return (Criteria) this;
        }

        public Criteria andConfigNotIn(List<String> values) {
            addCriterion("config not in", values, "config");
            return (Criteria) this;
        }

        public Criteria andConfigBetween(String value1, String value2) {
            addCriterion("config between", value1, value2, "config");
            return (Criteria) this;
        }

        public Criteria andConfigNotBetween(String value1, String value2) {
            addCriterion("config not between", value1, value2, "config");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNull() {
            addCriterion("is_delete is null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNotNull() {
            addCriterion("is_delete is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteEqualTo(Byte value) {
            addCriterion("is_delete =", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotEqualTo(Byte value) {
            addCriterion("is_delete <>", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThan(Byte value) {
            addCriterion("is_delete >", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_delete >=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThan(Byte value) {
            addCriterion("is_delete <", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThanOrEqualTo(Byte value) {
            addCriterion("is_delete <=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIn(List<Byte> values) {
            addCriterion("is_delete in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotIn(List<Byte> values) {
            addCriterion("is_delete not in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteBetween(Byte value1, Byte value2) {
            addCriterion("is_delete between", value1, value2, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotBetween(Byte value1, Byte value2) {
            addCriterion("is_delete not between", value1, value2, "isDelete");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeIsNull() {
            addCriterion("modify_time is null");
            return (Criteria) this;
        }

        public Criteria andModifyTimeIsNotNull() {
            addCriterion("modify_time is not null");
            return (Criteria) this;
        }

        public Criteria andModifyTimeEqualTo(Date value) {
            addCriterion("modify_time =", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeNotEqualTo(Date value) {
            addCriterion("modify_time <>", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeGreaterThan(Date value) {
            addCriterion("modify_time >", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("modify_time >=", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeLessThan(Date value) {
            addCriterion("modify_time <", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeLessThanOrEqualTo(Date value) {
            addCriterion("modify_time <=", value, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeIn(List<Date> values) {
            addCriterion("modify_time in", values, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeNotIn(List<Date> values) {
            addCriterion("modify_time not in", values, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeBetween(Date value1, Date value2) {
            addCriterion("modify_time between", value1, value2, "modifyTime");
            return (Criteria) this;
        }

        public Criteria andModifyTimeNotBetween(Date value1, Date value2) {
            addCriterion("modify_time not between", value1, value2, "modifyTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}