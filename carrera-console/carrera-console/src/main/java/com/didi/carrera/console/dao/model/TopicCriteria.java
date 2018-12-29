package com.didi.carrera.console.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TopicCriteria {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer start;

    protected Integer end;

    public TopicCriteria() {
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

        public Criteria andServiceIsNull() {
            addCriterion("service is null");
            return (Criteria) this;
        }

        public Criteria andServiceIsNotNull() {
            addCriterion("service is not null");
            return (Criteria) this;
        }

        public Criteria andServiceEqualTo(String value) {
            addCriterion("service =", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceNotEqualTo(String value) {
            addCriterion("service <>", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceGreaterThan(String value) {
            addCriterion("service >", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceGreaterThanOrEqualTo(String value) {
            addCriterion("service >=", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceLessThan(String value) {
            addCriterion("service <", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceLessThanOrEqualTo(String value) {
            addCriterion("service <=", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceLike(String value) {
            addCriterion("service like", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceNotLike(String value) {
            addCriterion("service not like", value, "service");
            return (Criteria) this;
        }

        public Criteria andServiceIn(List<String> values) {
            addCriterion("service in", values, "service");
            return (Criteria) this;
        }

        public Criteria andServiceNotIn(List<String> values) {
            addCriterion("service not in", values, "service");
            return (Criteria) this;
        }

        public Criteria andServiceBetween(String value1, String value2) {
            addCriterion("service between", value1, value2, "service");
            return (Criteria) this;
        }

        public Criteria andServiceNotBetween(String value1, String value2) {
            addCriterion("service not between", value1, value2, "service");
            return (Criteria) this;
        }

        public Criteria andDepartmentIsNull() {
            addCriterion("department is null");
            return (Criteria) this;
        }

        public Criteria andDepartmentIsNotNull() {
            addCriterion("department is not null");
            return (Criteria) this;
        }

        public Criteria andDepartmentEqualTo(String value) {
            addCriterion("department =", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentNotEqualTo(String value) {
            addCriterion("department <>", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentGreaterThan(String value) {
            addCriterion("department >", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentGreaterThanOrEqualTo(String value) {
            addCriterion("department >=", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentLessThan(String value) {
            addCriterion("department <", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentLessThanOrEqualTo(String value) {
            addCriterion("department <=", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentLike(String value) {
            addCriterion("department like", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentNotLike(String value) {
            addCriterion("department not like", value, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentIn(List<String> values) {
            addCriterion("department in", values, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentNotIn(List<String> values) {
            addCriterion("department not in", values, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentBetween(String value1, String value2) {
            addCriterion("department between", value1, value2, "department");
            return (Criteria) this;
        }

        public Criteria andDepartmentNotBetween(String value1, String value2) {
            addCriterion("department not between", value1, value2, "department");
            return (Criteria) this;
        }

        public Criteria andContactersIsNull() {
            addCriterion("contacters is null");
            return (Criteria) this;
        }

        public Criteria andContactersIsNotNull() {
            addCriterion("contacters is not null");
            return (Criteria) this;
        }

        public Criteria andContactersEqualTo(String value) {
            addCriterion("contacters =", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersNotEqualTo(String value) {
            addCriterion("contacters <>", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersGreaterThan(String value) {
            addCriterion("contacters >", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersGreaterThanOrEqualTo(String value) {
            addCriterion("contacters >=", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersLessThan(String value) {
            addCriterion("contacters <", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersLessThanOrEqualTo(String value) {
            addCriterion("contacters <=", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersLike(String value) {
            addCriterion("contacters like", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersNotLike(String value) {
            addCriterion("contacters not like", value, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersIn(List<String> values) {
            addCriterion("contacters in", values, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersNotIn(List<String> values) {
            addCriterion("contacters not in", values, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersBetween(String value1, String value2) {
            addCriterion("contacters between", value1, value2, "contacters");
            return (Criteria) this;
        }

        public Criteria andContactersNotBetween(String value1, String value2) {
            addCriterion("contacters not between", value1, value2, "contacters");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupIsNull() {
            addCriterion("alarm_group is null");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupIsNotNull() {
            addCriterion("alarm_group is not null");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupEqualTo(String value) {
            addCriterion("alarm_group =", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupNotEqualTo(String value) {
            addCriterion("alarm_group <>", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupGreaterThan(String value) {
            addCriterion("alarm_group >", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupGreaterThanOrEqualTo(String value) {
            addCriterion("alarm_group >=", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupLessThan(String value) {
            addCriterion("alarm_group <", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupLessThanOrEqualTo(String value) {
            addCriterion("alarm_group <=", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupLike(String value) {
            addCriterion("alarm_group like", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupNotLike(String value) {
            addCriterion("alarm_group not like", value, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupIn(List<String> values) {
            addCriterion("alarm_group in", values, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupNotIn(List<String> values) {
            addCriterion("alarm_group not in", values, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupBetween(String value1, String value2) {
            addCriterion("alarm_group between", value1, value2, "alarmGroup");
            return (Criteria) this;
        }

        public Criteria andAlarmGroupNotBetween(String value1, String value2) {
            addCriterion("alarm_group not between", value1, value2, "alarmGroup");
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

        public Criteria andDelayTopicIsNull() {
            addCriterion("delay_topic is null");
            return (Criteria) this;
        }

        public Criteria andDelayTopicIsNotNull() {
            addCriterion("delay_topic is not null");
            return (Criteria) this;
        }

        public Criteria andDelayTopicEqualTo(Byte value) {
            addCriterion("delay_topic =", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicNotEqualTo(Byte value) {
            addCriterion("delay_topic <>", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicGreaterThan(Byte value) {
            addCriterion("delay_topic >", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicGreaterThanOrEqualTo(Byte value) {
            addCriterion("delay_topic >=", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicLessThan(Byte value) {
            addCriterion("delay_topic <", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicLessThanOrEqualTo(Byte value) {
            addCriterion("delay_topic <=", value, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicIn(List<Byte> values) {
            addCriterion("delay_topic in", values, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicNotIn(List<Byte> values) {
            addCriterion("delay_topic not in", values, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicBetween(Byte value1, Byte value2) {
            addCriterion("delay_topic between", value1, value2, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andDelayTopicNotBetween(Byte value1, Byte value2) {
            addCriterion("delay_topic not between", value1, value2, "delayTopic");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoIsNull() {
            addCriterion("need_audit_subinfo is null");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoIsNotNull() {
            addCriterion("need_audit_subinfo is not null");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoEqualTo(Byte value) {
            addCriterion("need_audit_subinfo =", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoNotEqualTo(Byte value) {
            addCriterion("need_audit_subinfo <>", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoGreaterThan(Byte value) {
            addCriterion("need_audit_subinfo >", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoGreaterThanOrEqualTo(Byte value) {
            addCriterion("need_audit_subinfo >=", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoLessThan(Byte value) {
            addCriterion("need_audit_subinfo <", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoLessThanOrEqualTo(Byte value) {
            addCriterion("need_audit_subinfo <=", value, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoIn(List<Byte> values) {
            addCriterion("need_audit_subinfo in", values, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoNotIn(List<Byte> values) {
            addCriterion("need_audit_subinfo not in", values, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoBetween(Byte value1, Byte value2) {
            addCriterion("need_audit_subinfo between", value1, value2, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andNeedAuditSubinfoNotBetween(Byte value1, Byte value2) {
            addCriterion("need_audit_subinfo not between", value1, value2, "needAuditSubinfo");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyIsNull() {
            addCriterion("enable_schema_verify is null");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyIsNotNull() {
            addCriterion("enable_schema_verify is not null");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyEqualTo(Byte value) {
            addCriterion("enable_schema_verify =", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyNotEqualTo(Byte value) {
            addCriterion("enable_schema_verify <>", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyGreaterThan(Byte value) {
            addCriterion("enable_schema_verify >", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyGreaterThanOrEqualTo(Byte value) {
            addCriterion("enable_schema_verify >=", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyLessThan(Byte value) {
            addCriterion("enable_schema_verify <", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyLessThanOrEqualTo(Byte value) {
            addCriterion("enable_schema_verify <=", value, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyIn(List<Byte> values) {
            addCriterion("enable_schema_verify in", values, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyNotIn(List<Byte> values) {
            addCriterion("enable_schema_verify not in", values, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyBetween(Byte value1, Byte value2) {
            addCriterion("enable_schema_verify between", value1, value2, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andEnableSchemaVerifyNotBetween(Byte value1, Byte value2) {
            addCriterion("enable_schema_verify not between", value1, value2, "enableSchemaVerify");
            return (Criteria) this;
        }

        public Criteria andProduceModeIsNull() {
            addCriterion("produce_mode is null");
            return (Criteria) this;
        }

        public Criteria andProduceModeIsNotNull() {
            addCriterion("produce_mode is not null");
            return (Criteria) this;
        }

        public Criteria andProduceModeEqualTo(Byte value) {
            addCriterion("produce_mode =", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeNotEqualTo(Byte value) {
            addCriterion("produce_mode <>", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeGreaterThan(Byte value) {
            addCriterion("produce_mode >", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeGreaterThanOrEqualTo(Byte value) {
            addCriterion("produce_mode >=", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeLessThan(Byte value) {
            addCriterion("produce_mode <", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeLessThanOrEqualTo(Byte value) {
            addCriterion("produce_mode <=", value, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeIn(List<Byte> values) {
            addCriterion("produce_mode in", values, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeNotIn(List<Byte> values) {
            addCriterion("produce_mode not in", values, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeBetween(Byte value1, Byte value2) {
            addCriterion("produce_mode between", value1, value2, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeNotBetween(Byte value1, Byte value2) {
            addCriterion("produce_mode not between", value1, value2, "produceMode");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperIsNull() {
            addCriterion("produce_mode_mapper is null");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperIsNotNull() {
            addCriterion("produce_mode_mapper is not null");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperEqualTo(String value) {
            addCriterion("produce_mode_mapper =", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperNotEqualTo(String value) {
            addCriterion("produce_mode_mapper <>", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperGreaterThan(String value) {
            addCriterion("produce_mode_mapper >", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperGreaterThanOrEqualTo(String value) {
            addCriterion("produce_mode_mapper >=", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperLessThan(String value) {
            addCriterion("produce_mode_mapper <", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperLessThanOrEqualTo(String value) {
            addCriterion("produce_mode_mapper <=", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperLike(String value) {
            addCriterion("produce_mode_mapper like", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperNotLike(String value) {
            addCriterion("produce_mode_mapper not like", value, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperIn(List<String> values) {
            addCriterion("produce_mode_mapper in", values, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperNotIn(List<String> values) {
            addCriterion("produce_mode_mapper not in", values, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperBetween(String value1, String value2) {
            addCriterion("produce_mode_mapper between", value1, value2, "produceModeMapper");
            return (Criteria) this;
        }

        public Criteria andProduceModeMapperNotBetween(String value1, String value2) {
            addCriterion("produce_mode_mapper not between", value1, value2, "produceModeMapper");
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

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
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