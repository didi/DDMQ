package com.didi.carrera.console.dao.mapper.custom;

import java.util.List;

import com.didi.carrera.console.dao.BaseMapper;
import com.didi.carrera.console.dao.model.Topic;
import org.apache.ibatis.annotations.Param;


public interface TopicCustomMapper extends BaseMapper {

    List<Topic> selectByCondition(@Param("clusterId") Long clusterId, @Param("contacters") String user, @Param("text") String text, @Param("limit") Integer curPage, @Param("size") Integer pageSize);

    Integer selectCountByCondition(@Param("clusterId") Long clusterId, @Param("contacters") String user, @Param("text") String text);

//    @Insert(SCRIPT_START +
//            "insert into topic\n" +
//            "    <trim prefix='(' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        id,\n" +
//            "      </if>\n" +
//            "      <if test='topicName != null'>\n" +
//            "        topic_name,\n" +
//            "      </if>\n" +
//            "      <if test='department != null'>\n" +
//            "        department,\n" +
//            "      </if>\n" +
//            "      <if test='service != null'>\n" +
//            "        service,\n" +
//            "      </if>\n" +
//            "      <if test='contacters != null'>\n" +
//            "        contacters,\n" +
//            "      </if>\n" +
//            "      <if test='alarmGroup != null'>\n" +
//            "        alarm_group,\n" +
//            "      </if>\n" +
//            "      <if test='delayTopic != null'>\n" +
//            "        delay_topic,\n" +
//            "      </if>\n" +
//            "      <if test='state != null'>\n" +
//            "        state,\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        config,\n" +
//            "      </if>\n" +
//            "      <if test='description != null'>\n" +
//            "        description,\n" +
//            "      </if>\n" +
//            "      <if test='extraParams != null'>\n" +
//            "        extra_params,\n" +
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
//            "      <if test='topicSchema != null'>\n" +
//            "        topic_schema,\n" +
//            "      </if>\n" +
//            "    </trim>\n" +
//            "    <trim prefix='values (' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        #{id,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='topicName != null'>\n" +
//            "        #{topicName,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='department != null'>\n" +
//            "        #{department,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='service != null'>\n" +
//            "        #{service,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='contacters != null'>\n" +
//            "        #{contacters,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='alarmGroup != null'>\n" +
//            "        #{alarmGroup,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='delayTopic != null'>\n" +
//            "        #{delayTopic,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='state != null'>\n" +
//            "        #{state,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        #{config,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='description != null'>\n" +
//            "        #{description,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='extraParams != null'>\n" +
//            "        #{extraParams,jdbcType=VARCHAR},\n" +
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
//            "      <if test='topicSchema != null'>\n" +
//            "        #{topicSchema,jdbcType=LONGVARCHAR},\n" +
//            "      </if>\n" +
//            "    </trim>" +
//            SCRIPT_END)
//    int insertSelectiveWithPrimaryKey(Topic topic);
}