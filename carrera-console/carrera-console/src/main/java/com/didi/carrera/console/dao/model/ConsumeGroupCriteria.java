package com.didi.carrera.console.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ConsumeGroupCriteria {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer start;

    protected Integer end;

    public ConsumeGroupCriteria() {
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

        public Criteria andBroadcastConsumeIsNull() {
            addCriterion("broadcast_consume is null");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeIsNotNull() {
            addCriterion("broadcast_consume is not null");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeEqualTo(Byte value) {
            addCriterion("broadcast_consume =", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeNotEqualTo(Byte value) {
            addCriterion("broadcast_consume <>", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeGreaterThan(Byte value) {
            addCriterion("broadcast_consume >", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeGreaterThanOrEqualTo(Byte value) {
            addCriterion("broadcast_consume >=", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeLessThan(Byte value) {
            addCriterion("broadcast_consume <", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeLessThanOrEqualTo(Byte value) {
            addCriterion("broadcast_consume <=", value, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeIn(List<Byte> values) {
            addCriterion("broadcast_consume in", values, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeNotIn(List<Byte> values) {
            addCriterion("broadcast_consume not in", values, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeBetween(Byte value1, Byte value2) {
            addCriterion("broadcast_consume between", value1, value2, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andBroadcastConsumeNotBetween(Byte value1, Byte value2) {
            addCriterion("broadcast_consume not between", value1, value2, "broadcastConsume");
            return (Criteria) this;
        }

        public Criteria andConsumeModeIsNull() {
            addCriterion("consume_mode is null");
            return (Criteria) this;
        }

        public Criteria andConsumeModeIsNotNull() {
            addCriterion("consume_mode is not null");
            return (Criteria) this;
        }

        public Criteria andConsumeModeEqualTo(Byte value) {
            addCriterion("consume_mode =", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeNotEqualTo(Byte value) {
            addCriterion("consume_mode <>", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeGreaterThan(Byte value) {
            addCriterion("consume_mode >", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeGreaterThanOrEqualTo(Byte value) {
            addCriterion("consume_mode >=", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeLessThan(Byte value) {
            addCriterion("consume_mode <", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeLessThanOrEqualTo(Byte value) {
            addCriterion("consume_mode <=", value, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeIn(List<Byte> values) {
            addCriterion("consume_mode in", values, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeNotIn(List<Byte> values) {
            addCriterion("consume_mode not in", values, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeBetween(Byte value1, Byte value2) {
            addCriterion("consume_mode between", value1, value2, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeNotBetween(Byte value1, Byte value2) {
            addCriterion("consume_mode not between", value1, value2, "consumeMode");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperIsNull() {
            addCriterion("consume_mode_mapper is null");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperIsNotNull() {
            addCriterion("consume_mode_mapper is not null");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperEqualTo(String value) {
            addCriterion("consume_mode_mapper =", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperNotEqualTo(String value) {
            addCriterion("consume_mode_mapper <>", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperGreaterThan(String value) {
            addCriterion("consume_mode_mapper >", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperGreaterThanOrEqualTo(String value) {
            addCriterion("consume_mode_mapper >=", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperLessThan(String value) {
            addCriterion("consume_mode_mapper <", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperLessThanOrEqualTo(String value) {
            addCriterion("consume_mode_mapper <=", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperLike(String value) {
            addCriterion("consume_mode_mapper like", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperNotLike(String value) {
            addCriterion("consume_mode_mapper not like", value, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperIn(List<String> values) {
            addCriterion("consume_mode_mapper in", values, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperNotIn(List<String> values) {
            addCriterion("consume_mode_mapper not in", values, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperBetween(String value1, String value2) {
            addCriterion("consume_mode_mapper between", value1, value2, "consumeModeMapper");
            return (Criteria) this;
        }

        public Criteria andConsumeModeMapperNotBetween(String value1, String value2) {
            addCriterion("consume_mode_mapper not between", value1, value2, "consumeModeMapper");
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