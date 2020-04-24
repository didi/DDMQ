package com.didi.carrera.console.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ConsumeSubscriptionCriteria {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer start;

    protected Integer end;

    public ConsumeSubscriptionCriteria() {
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

        public Criteria andGroupIdIsNull() {
            addCriterion("group_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupIdIsNotNull() {
            addCriterion("group_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupIdEqualTo(Long value) {
            addCriterion("group_id =", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotEqualTo(Long value) {
            addCriterion("group_id <>", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThan(Long value) {
            addCriterion("group_id >", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThanOrEqualTo(Long value) {
            addCriterion("group_id >=", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThan(Long value) {
            addCriterion("group_id <", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThanOrEqualTo(Long value) {
            addCriterion("group_id <=", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdIn(List<Long> values) {
            addCriterion("group_id in", values, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotIn(List<Long> values) {
            addCriterion("group_id not in", values, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdBetween(Long value1, Long value2) {
            addCriterion("group_id between", value1, value2, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotBetween(Long value1, Long value2) {
            addCriterion("group_id not between", value1, value2, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupNameIsNull() {
            addCriterion("group_name is null");
            return (Criteria) this;
        }

        public Criteria andGroupNameIsNotNull() {
            addCriterion("group_name is not null");
            return (Criteria) this;
        }

        public Criteria andGroupNameEqualTo(String value) {
            addCriterion("group_name =", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotEqualTo(String value) {
            addCriterion("group_name <>", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameGreaterThan(String value) {
            addCriterion("group_name >", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameGreaterThanOrEqualTo(String value) {
            addCriterion("group_name >=", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLessThan(String value) {
            addCriterion("group_name <", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLessThanOrEqualTo(String value) {
            addCriterion("group_name <=", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLike(String value) {
            addCriterion("group_name like", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotLike(String value) {
            addCriterion("group_name not like", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameIn(List<String> values) {
            addCriterion("group_name in", values, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotIn(List<String> values) {
            addCriterion("group_name not in", values, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameBetween(String value1, String value2) {
            addCriterion("group_name between", value1, value2, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotBetween(String value1, String value2) {
            addCriterion("group_name not between", value1, value2, "groupName");
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

        public Criteria andPressureTrafficIsNull() {
            addCriterion("pressure_traffic is null");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficIsNotNull() {
            addCriterion("pressure_traffic is not null");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficEqualTo(Byte value) {
            addCriterion("pressure_traffic =", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficNotEqualTo(Byte value) {
            addCriterion("pressure_traffic <>", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficGreaterThan(Byte value) {
            addCriterion("pressure_traffic >", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficGreaterThanOrEqualTo(Byte value) {
            addCriterion("pressure_traffic >=", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficLessThan(Byte value) {
            addCriterion("pressure_traffic <", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficLessThanOrEqualTo(Byte value) {
            addCriterion("pressure_traffic <=", value, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficIn(List<Byte> values) {
            addCriterion("pressure_traffic in", values, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficNotIn(List<Byte> values) {
            addCriterion("pressure_traffic not in", values, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficBetween(Byte value1, Byte value2) {
            addCriterion("pressure_traffic between", value1, value2, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andPressureTrafficNotBetween(Byte value1, Byte value2) {
            addCriterion("pressure_traffic not between", value1, value2, "pressureTraffic");
            return (Criteria) this;
        }

        public Criteria andMaxTpsIsNull() {
            addCriterion("max_tps is null");
            return (Criteria) this;
        }

        public Criteria andMaxTpsIsNotNull() {
            addCriterion("max_tps is not null");
            return (Criteria) this;
        }

        public Criteria andMaxTpsEqualTo(Double value) {
            addCriterion("max_tps =", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsNotEqualTo(Double value) {
            addCriterion("max_tps <>", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsGreaterThan(Double value) {
            addCriterion("max_tps >", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsGreaterThanOrEqualTo(Double value) {
            addCriterion("max_tps >=", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsLessThan(Double value) {
            addCriterion("max_tps <", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsLessThanOrEqualTo(Double value) {
            addCriterion("max_tps <=", value, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsIn(List<Double> values) {
            addCriterion("max_tps in", values, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsNotIn(List<Double> values) {
            addCriterion("max_tps not in", values, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsBetween(Double value1, Double value2) {
            addCriterion("max_tps between", value1, value2, "maxTps");
            return (Criteria) this;
        }

        public Criteria andMaxTpsNotBetween(Double value1, Double value2) {
            addCriterion("max_tps not between", value1, value2, "maxTps");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeIsNull() {
            addCriterion("alarm_type is null");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeIsNotNull() {
            addCriterion("alarm_type is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeEqualTo(Byte value) {
            addCriterion("alarm_type =", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeNotEqualTo(Byte value) {
            addCriterion("alarm_type <>", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeGreaterThan(Byte value) {
            addCriterion("alarm_type >", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("alarm_type >=", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeLessThan(Byte value) {
            addCriterion("alarm_type <", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeLessThanOrEqualTo(Byte value) {
            addCriterion("alarm_type <=", value, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeIn(List<Byte> values) {
            addCriterion("alarm_type in", values, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeNotIn(List<Byte> values) {
            addCriterion("alarm_type not in", values, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeBetween(Byte value1, Byte value2) {
            addCriterion("alarm_type between", value1, value2, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("alarm_type not between", value1, value2, "alarmType");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableIsNull() {
            addCriterion("alarm_is_enable is null");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableIsNotNull() {
            addCriterion("alarm_is_enable is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableEqualTo(Byte value) {
            addCriterion("alarm_is_enable =", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableNotEqualTo(Byte value) {
            addCriterion("alarm_is_enable <>", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableGreaterThan(Byte value) {
            addCriterion("alarm_is_enable >", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableGreaterThanOrEqualTo(Byte value) {
            addCriterion("alarm_is_enable >=", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableLessThan(Byte value) {
            addCriterion("alarm_is_enable <", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableLessThanOrEqualTo(Byte value) {
            addCriterion("alarm_is_enable <=", value, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableIn(List<Byte> values) {
            addCriterion("alarm_is_enable in", values, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableNotIn(List<Byte> values) {
            addCriterion("alarm_is_enable not in", values, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableBetween(Byte value1, Byte value2) {
            addCriterion("alarm_is_enable between", value1, value2, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmIsEnableNotBetween(Byte value1, Byte value2) {
            addCriterion("alarm_is_enable not between", value1, value2, "alarmIsEnable");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelIsNull() {
            addCriterion("alarm_level is null");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelIsNotNull() {
            addCriterion("alarm_level is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelEqualTo(Byte value) {
            addCriterion("alarm_level =", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelNotEqualTo(Byte value) {
            addCriterion("alarm_level <>", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelGreaterThan(Byte value) {
            addCriterion("alarm_level >", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelGreaterThanOrEqualTo(Byte value) {
            addCriterion("alarm_level >=", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelLessThan(Byte value) {
            addCriterion("alarm_level <", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelLessThanOrEqualTo(Byte value) {
            addCriterion("alarm_level <=", value, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelIn(List<Byte> values) {
            addCriterion("alarm_level in", values, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelNotIn(List<Byte> values) {
            addCriterion("alarm_level not in", values, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelBetween(Byte value1, Byte value2) {
            addCriterion("alarm_level between", value1, value2, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmLevelNotBetween(Byte value1, Byte value2) {
            addCriterion("alarm_level not between", value1, value2, "alarmLevel");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagIsNull() {
            addCriterion("alarm_msg_lag is null");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagIsNotNull() {
            addCriterion("alarm_msg_lag is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagEqualTo(Integer value) {
            addCriterion("alarm_msg_lag =", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagNotEqualTo(Integer value) {
            addCriterion("alarm_msg_lag <>", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagGreaterThan(Integer value) {
            addCriterion("alarm_msg_lag >", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagGreaterThanOrEqualTo(Integer value) {
            addCriterion("alarm_msg_lag >=", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagLessThan(Integer value) {
            addCriterion("alarm_msg_lag <", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagLessThanOrEqualTo(Integer value) {
            addCriterion("alarm_msg_lag <=", value, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagIn(List<Integer> values) {
            addCriterion("alarm_msg_lag in", values, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagNotIn(List<Integer> values) {
            addCriterion("alarm_msg_lag not in", values, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagBetween(Integer value1, Integer value2) {
            addCriterion("alarm_msg_lag between", value1, value2, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmMsgLagNotBetween(Integer value1, Integer value2) {
            addCriterion("alarm_msg_lag not between", value1, value2, "alarmMsgLag");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeIsNull() {
            addCriterion("alarm_delay_time is null");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeIsNotNull() {
            addCriterion("alarm_delay_time is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeEqualTo(Integer value) {
            addCriterion("alarm_delay_time =", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeNotEqualTo(Integer value) {
            addCriterion("alarm_delay_time <>", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeGreaterThan(Integer value) {
            addCriterion("alarm_delay_time >", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("alarm_delay_time >=", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeLessThan(Integer value) {
            addCriterion("alarm_delay_time <", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeLessThanOrEqualTo(Integer value) {
            addCriterion("alarm_delay_time <=", value, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeIn(List<Integer> values) {
            addCriterion("alarm_delay_time in", values, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeNotIn(List<Integer> values) {
            addCriterion("alarm_delay_time not in", values, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeBetween(Integer value1, Integer value2) {
            addCriterion("alarm_delay_time between", value1, value2, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andAlarmDelayTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("alarm_delay_time not between", value1, value2, "alarmDelayTime");
            return (Criteria) this;
        }

        public Criteria andApiTypeIsNull() {
            addCriterion("api_type is null");
            return (Criteria) this;
        }

        public Criteria andApiTypeIsNotNull() {
            addCriterion("api_type is not null");
            return (Criteria) this;
        }

        public Criteria andApiTypeEqualTo(Byte value) {
            addCriterion("api_type =", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotEqualTo(Byte value) {
            addCriterion("api_type <>", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeGreaterThan(Byte value) {
            addCriterion("api_type >", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("api_type >=", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeLessThan(Byte value) {
            addCriterion("api_type <", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeLessThanOrEqualTo(Byte value) {
            addCriterion("api_type <=", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeIn(List<Byte> values) {
            addCriterion("api_type in", values, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotIn(List<Byte> values) {
            addCriterion("api_type not in", values, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeBetween(Byte value1, Byte value2) {
            addCriterion("api_type between", value1, value2, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("api_type not between", value1, value2, "apiType");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutIsNull() {
            addCriterion("consume_timeout is null");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutIsNotNull() {
            addCriterion("consume_timeout is not null");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutEqualTo(Integer value) {
            addCriterion("consume_timeout =", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutNotEqualTo(Integer value) {
            addCriterion("consume_timeout <>", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutGreaterThan(Integer value) {
            addCriterion("consume_timeout >", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutGreaterThanOrEqualTo(Integer value) {
            addCriterion("consume_timeout >=", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutLessThan(Integer value) {
            addCriterion("consume_timeout <", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutLessThanOrEqualTo(Integer value) {
            addCriterion("consume_timeout <=", value, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutIn(List<Integer> values) {
            addCriterion("consume_timeout in", values, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutNotIn(List<Integer> values) {
            addCriterion("consume_timeout not in", values, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutBetween(Integer value1, Integer value2) {
            addCriterion("consume_timeout between", value1, value2, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andConsumeTimeoutNotBetween(Integer value1, Integer value2) {
            addCriterion("consume_timeout not between", value1, value2, "consumeTimeout");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesIsNull() {
            addCriterion("error_retry_times is null");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesIsNotNull() {
            addCriterion("error_retry_times is not null");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesEqualTo(Integer value) {
            addCriterion("error_retry_times =", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesNotEqualTo(Integer value) {
            addCriterion("error_retry_times <>", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesGreaterThan(Integer value) {
            addCriterion("error_retry_times >", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("error_retry_times >=", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesLessThan(Integer value) {
            addCriterion("error_retry_times <", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesLessThanOrEqualTo(Integer value) {
            addCriterion("error_retry_times <=", value, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesIn(List<Integer> values) {
            addCriterion("error_retry_times in", values, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesNotIn(List<Integer> values) {
            addCriterion("error_retry_times not in", values, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesBetween(Integer value1, Integer value2) {
            addCriterion("error_retry_times between", value1, value2, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andErrorRetryTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("error_retry_times not between", value1, value2, "errorRetryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsIsNull() {
            addCriterion("retry_intervals is null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsIsNotNull() {
            addCriterion("retry_intervals is not null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsEqualTo(String value) {
            addCriterion("retry_intervals =", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsNotEqualTo(String value) {
            addCriterion("retry_intervals <>", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsGreaterThan(String value) {
            addCriterion("retry_intervals >", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsGreaterThanOrEqualTo(String value) {
            addCriterion("retry_intervals >=", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsLessThan(String value) {
            addCriterion("retry_intervals <", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsLessThanOrEqualTo(String value) {
            addCriterion("retry_intervals <=", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsLike(String value) {
            addCriterion("retry_intervals like", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsNotLike(String value) {
            addCriterion("retry_intervals not like", value, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsIn(List<String> values) {
            addCriterion("retry_intervals in", values, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsNotIn(List<String> values) {
            addCriterion("retry_intervals not in", values, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsBetween(String value1, String value2) {
            addCriterion("retry_intervals between", value1, value2, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalsNotBetween(String value1, String value2) {
            addCriterion("retry_intervals not between", value1, value2, "retryIntervals");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNull() {
            addCriterion("msg_type is null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNotNull() {
            addCriterion("msg_type is not null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeEqualTo(Byte value) {
            addCriterion("msg_type =", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotEqualTo(Byte value) {
            addCriterion("msg_type <>", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThan(Byte value) {
            addCriterion("msg_type >", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("msg_type >=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThan(Byte value) {
            addCriterion("msg_type <", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThanOrEqualTo(Byte value) {
            addCriterion("msg_type <=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIn(List<Byte> values) {
            addCriterion("msg_type in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotIn(List<Byte> values) {
            addCriterion("msg_type not in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeBetween(Byte value1, Byte value2) {
            addCriterion("msg_type between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("msg_type not between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyIsNull() {
            addCriterion("enable_groovy is null");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyIsNotNull() {
            addCriterion("enable_groovy is not null");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyEqualTo(Byte value) {
            addCriterion("enable_groovy =", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyNotEqualTo(Byte value) {
            addCriterion("enable_groovy <>", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyGreaterThan(Byte value) {
            addCriterion("enable_groovy >", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyGreaterThanOrEqualTo(Byte value) {
            addCriterion("enable_groovy >=", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyLessThan(Byte value) {
            addCriterion("enable_groovy <", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyLessThanOrEqualTo(Byte value) {
            addCriterion("enable_groovy <=", value, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyIn(List<Byte> values) {
            addCriterion("enable_groovy in", values, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyNotIn(List<Byte> values) {
            addCriterion("enable_groovy not in", values, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyBetween(Byte value1, Byte value2) {
            addCriterion("enable_groovy between", value1, value2, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableGroovyNotBetween(Byte value1, Byte value2) {
            addCriterion("enable_groovy not between", value1, value2, "enableGroovy");
            return (Criteria) this;
        }

        public Criteria andEnableTransitIsNull() {
            addCriterion("enable_transit is null");
            return (Criteria) this;
        }

        public Criteria andEnableTransitIsNotNull() {
            addCriterion("enable_transit is not null");
            return (Criteria) this;
        }

        public Criteria andEnableTransitEqualTo(Byte value) {
            addCriterion("enable_transit =", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitNotEqualTo(Byte value) {
            addCriterion("enable_transit <>", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitGreaterThan(Byte value) {
            addCriterion("enable_transit >", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitGreaterThanOrEqualTo(Byte value) {
            addCriterion("enable_transit >=", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitLessThan(Byte value) {
            addCriterion("enable_transit <", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitLessThanOrEqualTo(Byte value) {
            addCriterion("enable_transit <=", value, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitIn(List<Byte> values) {
            addCriterion("enable_transit in", values, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitNotIn(List<Byte> values) {
            addCriterion("enable_transit not in", values, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitBetween(Byte value1, Byte value2) {
            addCriterion("enable_transit between", value1, value2, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableTransitNotBetween(Byte value1, Byte value2) {
            addCriterion("enable_transit not between", value1, value2, "enableTransit");
            return (Criteria) this;
        }

        public Criteria andEnableOrderIsNull() {
            addCriterion("enable_order is null");
            return (Criteria) this;
        }

        public Criteria andEnableOrderIsNotNull() {
            addCriterion("enable_order is not null");
            return (Criteria) this;
        }

        public Criteria andEnableOrderEqualTo(Byte value) {
            addCriterion("enable_order =", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderNotEqualTo(Byte value) {
            addCriterion("enable_order <>", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderGreaterThan(Byte value) {
            addCriterion("enable_order >", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderGreaterThanOrEqualTo(Byte value) {
            addCriterion("enable_order >=", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderLessThan(Byte value) {
            addCriterion("enable_order <", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderLessThanOrEqualTo(Byte value) {
            addCriterion("enable_order <=", value, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderIn(List<Byte> values) {
            addCriterion("enable_order in", values, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderNotIn(List<Byte> values) {
            addCriterion("enable_order not in", values, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderBetween(Byte value1, Byte value2) {
            addCriterion("enable_order between", value1, value2, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andEnableOrderNotBetween(Byte value1, Byte value2) {
            addCriterion("enable_order not between", value1, value2, "enableOrder");
            return (Criteria) this;
        }

        public Criteria andOrderKeyIsNull() {
            addCriterion("order_key is null");
            return (Criteria) this;
        }

        public Criteria andOrderKeyIsNotNull() {
            addCriterion("order_key is not null");
            return (Criteria) this;
        }

        public Criteria andOrderKeyEqualTo(String value) {
            addCriterion("order_key =", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyNotEqualTo(String value) {
            addCriterion("order_key <>", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyGreaterThan(String value) {
            addCriterion("order_key >", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyGreaterThanOrEqualTo(String value) {
            addCriterion("order_key >=", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyLessThan(String value) {
            addCriterion("order_key <", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyLessThanOrEqualTo(String value) {
            addCriterion("order_key <=", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyLike(String value) {
            addCriterion("order_key like", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyNotLike(String value) {
            addCriterion("order_key not like", value, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyIn(List<String> values) {
            addCriterion("order_key in", values, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyNotIn(List<String> values) {
            addCriterion("order_key not in", values, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyBetween(String value1, String value2) {
            addCriterion("order_key between", value1, value2, "orderKey");
            return (Criteria) this;
        }

        public Criteria andOrderKeyNotBetween(String value1, String value2) {
            addCriterion("order_key not between", value1, value2, "orderKey");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeIsNull() {
            addCriterion("consume_type is null");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeIsNotNull() {
            addCriterion("consume_type is not null");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeEqualTo(Byte value) {
            addCriterion("consume_type =", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeNotEqualTo(Byte value) {
            addCriterion("consume_type <>", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeGreaterThan(Byte value) {
            addCriterion("consume_type >", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("consume_type >=", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeLessThan(Byte value) {
            addCriterion("consume_type <", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeLessThanOrEqualTo(Byte value) {
            addCriterion("consume_type <=", value, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeIn(List<Byte> values) {
            addCriterion("consume_type in", values, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeNotIn(List<Byte> values) {
            addCriterion("consume_type not in", values, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeBetween(Byte value1, Byte value2) {
            addCriterion("consume_type between", value1, value2, "consumeType");
            return (Criteria) this;
        }

        public Criteria andConsumeTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("consume_type not between", value1, value2, "consumeType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeIsNull() {
            addCriterion("big_data_type is null");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeIsNotNull() {
            addCriterion("big_data_type is not null");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeEqualTo(Byte value) {
            addCriterion("big_data_type =", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeNotEqualTo(Byte value) {
            addCriterion("big_data_type <>", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeGreaterThan(Byte value) {
            addCriterion("big_data_type >", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("big_data_type >=", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeLessThan(Byte value) {
            addCriterion("big_data_type <", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeLessThanOrEqualTo(Byte value) {
            addCriterion("big_data_type <=", value, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeIn(List<Byte> values) {
            addCriterion("big_data_type in", values, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeNotIn(List<Byte> values) {
            addCriterion("big_data_type not in", values, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeBetween(Byte value1, Byte value2) {
            addCriterion("big_data_type between", value1, value2, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("big_data_type not between", value1, value2, "bigDataType");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigIsNull() {
            addCriterion("big_data_config is null");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigIsNotNull() {
            addCriterion("big_data_config is not null");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigEqualTo(String value) {
            addCriterion("big_data_config =", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigNotEqualTo(String value) {
            addCriterion("big_data_config <>", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigGreaterThan(String value) {
            addCriterion("big_data_config >", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigGreaterThanOrEqualTo(String value) {
            addCriterion("big_data_config >=", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigLessThan(String value) {
            addCriterion("big_data_config <", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigLessThanOrEqualTo(String value) {
            addCriterion("big_data_config <=", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigLike(String value) {
            addCriterion("big_data_config like", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigNotLike(String value) {
            addCriterion("big_data_config not like", value, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigIn(List<String> values) {
            addCriterion("big_data_config in", values, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigNotIn(List<String> values) {
            addCriterion("big_data_config not in", values, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigBetween(String value1, String value2) {
            addCriterion("big_data_config between", value1, value2, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andBigDataConfigNotBetween(String value1, String value2) {
            addCriterion("big_data_config not between", value1, value2, "bigDataConfig");
            return (Criteria) this;
        }

        public Criteria andUrlsIsNull() {
            addCriterion("urls is null");
            return (Criteria) this;
        }

        public Criteria andUrlsIsNotNull() {
            addCriterion("urls is not null");
            return (Criteria) this;
        }

        public Criteria andUrlsEqualTo(String value) {
            addCriterion("urls =", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsNotEqualTo(String value) {
            addCriterion("urls <>", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsGreaterThan(String value) {
            addCriterion("urls >", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsGreaterThanOrEqualTo(String value) {
            addCriterion("urls >=", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsLessThan(String value) {
            addCriterion("urls <", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsLessThanOrEqualTo(String value) {
            addCriterion("urls <=", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsLike(String value) {
            addCriterion("urls like", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsNotLike(String value) {
            addCriterion("urls not like", value, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsIn(List<String> values) {
            addCriterion("urls in", values, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsNotIn(List<String> values) {
            addCriterion("urls not in", values, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsBetween(String value1, String value2) {
            addCriterion("urls between", value1, value2, "urls");
            return (Criteria) this;
        }

        public Criteria andUrlsNotBetween(String value1, String value2) {
            addCriterion("urls not between", value1, value2, "urls");
            return (Criteria) this;
        }

        public Criteria andHttpMethodIsNull() {
            addCriterion("http_method is null");
            return (Criteria) this;
        }

        public Criteria andHttpMethodIsNotNull() {
            addCriterion("http_method is not null");
            return (Criteria) this;
        }

        public Criteria andHttpMethodEqualTo(Byte value) {
            addCriterion("http_method =", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodNotEqualTo(Byte value) {
            addCriterion("http_method <>", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodGreaterThan(Byte value) {
            addCriterion("http_method >", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodGreaterThanOrEqualTo(Byte value) {
            addCriterion("http_method >=", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodLessThan(Byte value) {
            addCriterion("http_method <", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodLessThanOrEqualTo(Byte value) {
            addCriterion("http_method <=", value, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodIn(List<Byte> values) {
            addCriterion("http_method in", values, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodNotIn(List<Byte> values) {
            addCriterion("http_method not in", values, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodBetween(Byte value1, Byte value2) {
            addCriterion("http_method between", value1, value2, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpMethodNotBetween(Byte value1, Byte value2) {
            addCriterion("http_method not between", value1, value2, "httpMethod");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersIsNull() {
            addCriterion("http_headers is null");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersIsNotNull() {
            addCriterion("http_headers is not null");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersEqualTo(String value) {
            addCriterion("http_headers =", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersNotEqualTo(String value) {
            addCriterion("http_headers <>", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersGreaterThan(String value) {
            addCriterion("http_headers >", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersGreaterThanOrEqualTo(String value) {
            addCriterion("http_headers >=", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersLessThan(String value) {
            addCriterion("http_headers <", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersLessThanOrEqualTo(String value) {
            addCriterion("http_headers <=", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersLike(String value) {
            addCriterion("http_headers like", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersNotLike(String value) {
            addCriterion("http_headers not like", value, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersIn(List<String> values) {
            addCriterion("http_headers in", values, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersNotIn(List<String> values) {
            addCriterion("http_headers not in", values, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersBetween(String value1, String value2) {
            addCriterion("http_headers between", value1, value2, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpHeadersNotBetween(String value1, String value2) {
            addCriterion("http_headers not between", value1, value2, "httpHeaders");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsIsNull() {
            addCriterion("http_query_params is null");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsIsNotNull() {
            addCriterion("http_query_params is not null");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsEqualTo(String value) {
            addCriterion("http_query_params =", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsNotEqualTo(String value) {
            addCriterion("http_query_params <>", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsGreaterThan(String value) {
            addCriterion("http_query_params >", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsGreaterThanOrEqualTo(String value) {
            addCriterion("http_query_params >=", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsLessThan(String value) {
            addCriterion("http_query_params <", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsLessThanOrEqualTo(String value) {
            addCriterion("http_query_params <=", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsLike(String value) {
            addCriterion("http_query_params like", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsNotLike(String value) {
            addCriterion("http_query_params not like", value, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsIn(List<String> values) {
            addCriterion("http_query_params in", values, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsNotIn(List<String> values) {
            addCriterion("http_query_params not in", values, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsBetween(String value1, String value2) {
            addCriterion("http_query_params between", value1, value2, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andHttpQueryParamsNotBetween(String value1, String value2) {
            addCriterion("http_query_params not between", value1, value2, "httpQueryParams");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeIsNull() {
            addCriterion("msg_push_type is null");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeIsNotNull() {
            addCriterion("msg_push_type is not null");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeEqualTo(Byte value) {
            addCriterion("msg_push_type =", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeNotEqualTo(Byte value) {
            addCriterion("msg_push_type <>", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeGreaterThan(Byte value) {
            addCriterion("msg_push_type >", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("msg_push_type >=", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeLessThan(Byte value) {
            addCriterion("msg_push_type <", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeLessThanOrEqualTo(Byte value) {
            addCriterion("msg_push_type <=", value, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeIn(List<Byte> values) {
            addCriterion("msg_push_type in", values, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeNotIn(List<Byte> values) {
            addCriterion("msg_push_type not in", values, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeBetween(Byte value1, Byte value2) {
            addCriterion("msg_push_type between", value1, value2, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andMsgPushTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("msg_push_type not between", value1, value2, "msgPushType");
            return (Criteria) this;
        }

        public Criteria andHttpTokenIsNull() {
            addCriterion("http_token is null");
            return (Criteria) this;
        }

        public Criteria andHttpTokenIsNotNull() {
            addCriterion("http_token is not null");
            return (Criteria) this;
        }

        public Criteria andHttpTokenEqualTo(String value) {
            addCriterion("http_token =", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenNotEqualTo(String value) {
            addCriterion("http_token <>", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenGreaterThan(String value) {
            addCriterion("http_token >", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenGreaterThanOrEqualTo(String value) {
            addCriterion("http_token >=", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenLessThan(String value) {
            addCriterion("http_token <", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenLessThanOrEqualTo(String value) {
            addCriterion("http_token <=", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenLike(String value) {
            addCriterion("http_token like", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenNotLike(String value) {
            addCriterion("http_token not like", value, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenIn(List<String> values) {
            addCriterion("http_token in", values, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenNotIn(List<String> values) {
            addCriterion("http_token not in", values, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenBetween(String value1, String value2) {
            addCriterion("http_token between", value1, value2, "httpToken");
            return (Criteria) this;
        }

        public Criteria andHttpTokenNotBetween(String value1, String value2) {
            addCriterion("http_token not between", value1, value2, "httpToken");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyIsNull() {
            addCriterion("push_max_concurrency is null");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyIsNotNull() {
            addCriterion("push_max_concurrency is not null");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyEqualTo(Integer value) {
            addCriterion("push_max_concurrency =", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyNotEqualTo(Integer value) {
            addCriterion("push_max_concurrency <>", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyGreaterThan(Integer value) {
            addCriterion("push_max_concurrency >", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_max_concurrency >=", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyLessThan(Integer value) {
            addCriterion("push_max_concurrency <", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyLessThanOrEqualTo(Integer value) {
            addCriterion("push_max_concurrency <=", value, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyIn(List<Integer> values) {
            addCriterion("push_max_concurrency in", values, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyNotIn(List<Integer> values) {
            addCriterion("push_max_concurrency not in", values, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyBetween(Integer value1, Integer value2) {
            addCriterion("push_max_concurrency between", value1, value2, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andPushMaxConcurrencyNotBetween(Integer value1, Integer value2) {
            addCriterion("push_max_concurrency not between", value1, value2, "pushMaxConcurrency");
            return (Criteria) this;
        }

        public Criteria andActionsIsNull() {
            addCriterion("actions is null");
            return (Criteria) this;
        }

        public Criteria andActionsIsNotNull() {
            addCriterion("actions is not null");
            return (Criteria) this;
        }

        public Criteria andActionsEqualTo(String value) {
            addCriterion("actions =", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsNotEqualTo(String value) {
            addCriterion("actions <>", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsGreaterThan(String value) {
            addCriterion("actions >", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsGreaterThanOrEqualTo(String value) {
            addCriterion("actions >=", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsLessThan(String value) {
            addCriterion("actions <", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsLessThanOrEqualTo(String value) {
            addCriterion("actions <=", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsLike(String value) {
            addCriterion("actions like", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsNotLike(String value) {
            addCriterion("actions not like", value, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsIn(List<String> values) {
            addCriterion("actions in", values, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsNotIn(List<String> values) {
            addCriterion("actions not in", values, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsBetween(String value1, String value2) {
            addCriterion("actions between", value1, value2, "actions");
            return (Criteria) this;
        }

        public Criteria andActionsNotBetween(String value1, String value2) {
            addCriterion("actions not between", value1, value2, "actions");
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

        public Criteria andExtraParamsIsNull() {
            addCriterion("extra_params is null");
            return (Criteria) this;
        }

        public Criteria andExtraParamsIsNotNull() {
            addCriterion("extra_params is not null");
            return (Criteria) this;
        }

        public Criteria andExtraParamsEqualTo(String value) {
            addCriterion("extra_params =", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsNotEqualTo(String value) {
            addCriterion("extra_params <>", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsGreaterThan(String value) {
            addCriterion("extra_params >", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsGreaterThanOrEqualTo(String value) {
            addCriterion("extra_params >=", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsLessThan(String value) {
            addCriterion("extra_params <", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsLessThanOrEqualTo(String value) {
            addCriterion("extra_params <=", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsLike(String value) {
            addCriterion("extra_params like", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsNotLike(String value) {
            addCriterion("extra_params not like", value, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsIn(List<String> values) {
            addCriterion("extra_params in", values, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsNotIn(List<String> values) {
            addCriterion("extra_params not in", values, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsBetween(String value1, String value2) {
            addCriterion("extra_params between", value1, value2, "extraParams");
            return (Criteria) this;
        }

        public Criteria andExtraParamsNotBetween(String value1, String value2) {
            addCriterion("extra_params not between", value1, value2, "extraParams");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
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