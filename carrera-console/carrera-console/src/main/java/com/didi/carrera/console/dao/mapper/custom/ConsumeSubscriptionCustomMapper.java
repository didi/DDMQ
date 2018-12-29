package com.didi.carrera.console.dao.mapper.custom;

import java.util.List;

import com.didi.carrera.console.dao.BaseMapper;
import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomSubscriptionStateCount;
import org.apache.ibatis.annotations.Param;


public interface ConsumeSubscriptionCustomMapper extends BaseMapper {

    List<CustomConsumeSubscription> selectByTopicIdClusterId(@Param("topicId") Long topicId, @Param("clusterId") Long clusterId);

    List<CustomSubscriptionStateCount> selectStateCountByGroupId(List<Long> list);

    List<ConsumeSubscription> selectByCondition(@Param("contacters") String user, @Param("groupId") Long groupId, @Param("clusterId") Long clusterId, @Param("consumeType") Integer consumeType, @Param("state") Integer state, @Param("text") String text, @Param("limit") Integer index, @Param("size") Integer pageSize);

    Integer selectCountByCondition(@Param("contacters") String user, @Param("groupId") Long groupId, @Param("clusterId") Long clusterId, @Param("consumeType") Integer consumeType, @Param("state") Integer state, @Param("text") String text);


//    @Insert(SCRIPT_START +
//            "insert into consume_subscription\n" +
//            "    <trim prefix='(' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        id,\n" +
//            "      </if>\n" +
//            "      <if test='groupId != null'>\n" +
//            "        group_id,\n" +
//            "      </if>\n" +
//            "      <if test='groupName != null'>\n" +
//            "        group_name,\n" +
//            "      </if>\n" +
//            "      <if test='topicId != null'>\n" +
//            "        topic_id,\n" +
//            "      </if>\n" +
//            "      <if test='topicName != null'>\n" +
//            "        topic_name,\n" +
//            "      </if>\n" +
//            "      <if test='clusterId != null'>\n" +
//            "        cluster_id,\n" +
//            "      </if>\n" +
//            "      <if test='clusterName != null'>\n" +
//            "        cluster_name,\n" +
//            "      </if>\n" +
//            "      <if test='pressureTraffic != null'>\n" +
//            "        pressure_traffic,\n" +
//            "      </if>\n" +
//            "      <if test='maxTps != null'>\n" +
//            "        max_tps,\n" +
//            "      </if>\n" +
//            "      <if test='alarmType != null'>\n" +
//            "        alarm_type,\n" +
//            "      </if>\n" +
//            "      <if test='alarmIsEnable != null'>\n" +
//            "        alarm_is_enable,\n" +
//            "      </if>\n" +
//            "      <if test='alarmLevel != null'>\n" +
//            "        alarm_level,\n" +
//            "      </if>\n" +
//            "      <if test='alarmMsgLag != null'>\n" +
//            "        alarm_msg_lag,\n" +
//            "      </if>\n" +
//            "      <if test='alarmDelayTime != null'>\n" +
//            "        alarm_delay_time,\n" +
//            "      </if>\n" +
//            "      <if test='apiType != null'>\n" +
//            "        api_type,\n" +
//            "      </if>\n" +
//            "      <if test='consumeTimeout != null'>\n" +
//            "        consume_timeout,\n" +
//            "      </if>\n" +
//            "      <if test='errorRetryTimes != null'>\n" +
//            "        error_retry_times,\n" +
//            "      </if>\n" +
//            "      <if test='retryIntervals != null'>\n" +
//            "        retry_intervals,\n" +
//            "      </if>\n" +
//            "      <if test='msgType != null'>\n" +
//            "        msg_type,\n" +
//            "      </if>\n" +
//            "      <if test='enableGroovy != null'>\n" +
//            "        enable_groovy,\n" +
//            "      </if>\n" +
//            "      <if test='enableTransit != null'>\n" +
//            "        enable_transit,\n" +
//            "      </if>\n" +
//            "      <if test='transit != null'>\n" +
//            "        transit,\n" +
//            "      </if>\n" +
//            "      <if test='enableOrder != null'>\n" +
//            "        enable_order,\n" +
//            "      </if>\n" +
//            "      <if test='orderKey != null'>\n" +
//            "        order_key,\n" +
//            "      </if>\n" +
//            "      <if test='consumeType != null'>\n" +
//            "        consume_type,\n" +
//            "      </if>\n" +
//            "      <if test='urls != null'>\n" +
//            "        urls,\n" +
//            "      </if>\n" +
//            "      <if test='httpMethod != null'>\n" +
//            "        http_method,\n" +
//            "      </if>\n" +
//            "      <if test='httpHeaders != null'>\n" +
//            "        http_headers,\n" +
//            "      </if>\n" +
//            "      <if test='httpQueryParams != null'>\n" +
//            "        http_query_params,\n" +
//            "      </if>\n" +
//            "      <if test='msgPushType != null'>\n" +
//            "        msg_push_type,\n" +
//            "      </if>\n" +
//            "      <if test='httpToken != null'>\n" +
//            "        http_token,\n" +
//            "      </if>\n" +
//            "      <if test='pushMaxConcurrency != null'>\n" +
//            "        push_max_concurrency,\n" +
//            "      </if>\n" +
//            "      <if test='actions != null'>\n" +
//            "        actions,\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        config,\n" +
//            "      </if>\n" +
//            "      <if test='state != null'>\n" +
//            "        state,\n" +
//            "      </if>\n" +
//            "      <if test='extraParams != null'>\n" +
//            "        extra_params,\n" +
//            "      </if>\n" +
//            "      <if test='remark != null'>\n" +
//            "        remark,\n" +
//            "      </if>\n" +
//            "      <if test='isDelete != null'>\n" +
//            "        is_delete,\n" +
//            "      </if>\n" +
//            "      <if test='createTime != null'>\n" +
//            "        create_time,\n" +
//            "      </if>\n" +
//            "      <if test='modifyTime != null'>\n" +
//            "        modify_time,\n" +
//            "      </if>\n" +
//            "      <if test='groovy != null'>\n" +
//            "        groovy,\n" +
//            "      </if>\n" +
//            "    </trim>\n" +
//            "    <trim prefix='values (' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        #{id,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='groupId != null'>\n" +
//            "        #{groupId,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='groupName != null'>\n" +
//            "        #{groupName,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='topicId != null'>\n" +
//            "        #{topicId,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='topicName != null'>\n" +
//            "        #{topicName,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='clusterId != null'>\n" +
//            "        #{clusterId,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='clusterName != null'>\n" +
//            "        #{clusterName,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='pressureTraffic != null'>\n" +
//            "        #{pressureTraffic,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='maxTps != null'>\n" +
//            "        #{maxTps,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='alarmType != null'>\n" +
//            "        #{alarmType,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='alarmIsEnable != null'>\n" +
//            "        #{alarmIsEnable,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='alarmLevel != null'>\n" +
//            "        #{alarmLevel,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='alarmMsgLag != null'>\n" +
//            "        #{alarmMsgLag,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='alarmDelayTime != null'>\n" +
//            "        #{alarmDelayTime,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='apiType != null'>\n" +
//            "        #{apiType,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='consumeTimeout != null'>\n" +
//            "        #{consumeTimeout,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='errorRetryTimes != null'>\n" +
//            "        #{errorRetryTimes,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='retryIntervals != null'>\n" +
//            "        #{retryIntervals,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='msgType != null'>\n" +
//            "        #{msgType,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='enableGroovy != null'>\n" +
//            "        #{enableGroovy,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='enableTransit != null'>\n" +
//            "        #{enableTransit,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='transit != null'>\n" +
//            "        #{transit,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='enableOrder != null'>\n" +
//            "        #{enableOrder,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='orderKey != null'>\n" +
//            "        #{orderKey,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='consumeType != null'>\n" +
//            "        #{consumeType,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='urls != null'>\n" +
//            "        #{urls,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='httpMethod != null'>\n" +
//            "        #{httpMethod,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='httpHeaders != null'>\n" +
//            "        #{httpHeaders,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='httpQueryParams != null'>\n" +
//            "        #{httpQueryParams,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='msgPushType != null'>\n" +
//            "        #{msgPushType,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='httpToken != null'>\n" +
//            "        #{httpToken,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='pushMaxConcurrency != null'>\n" +
//            "        #{pushMaxConcurrency,jdbcType=INTEGER},\n" +
//            "      </if>\n" +
//            "      <if test='actions != null'>\n" +
//            "        #{actions,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        #{config,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='state != null'>\n" +
//            "        #{state,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='extraParams != null'>\n" +
//            "        #{extraParams,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='remark != null'>\n" +
//            "        #{remark,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='isDelete != null'>\n" +
//            "        #{isDelete,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='createTime != null'>\n" +
//            "        #{createTime,jdbcType=TIMESTAMP},\n" +
//            "      </if>\n" +
//            "      <if test='modifyTime != null'>\n" +
//            "        #{modifyTime,jdbcType=TIMESTAMP},\n" +
//            "      </if>\n" +
//            "      <if test='groovy != null'>\n" +
//            "        #{groovy,jdbcType=LONGVARCHAR},\n" +
//            "      </if>\n" +
//            "    </trim>" +
//            SCRIPT_END)
//    int insertSelectiveWithPrimaryKey(ConsumeSubscription consumeSubscription);

}