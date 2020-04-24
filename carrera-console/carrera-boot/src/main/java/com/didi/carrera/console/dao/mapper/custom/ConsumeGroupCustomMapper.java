package com.didi.carrera.console.dao.mapper.custom;

import java.util.List;

import com.didi.carrera.console.dao.BaseMapper;
import com.didi.carrera.console.dao.model.ConsumeGroup;
import org.apache.ibatis.annotations.Param;


public interface ConsumeGroupCustomMapper extends BaseMapper {

    List<ConsumeGroup> selectByCondition(@Param("contacters") String user, @Param("text") String text, @Param("limit") Integer index, @Param("size") Integer pageSize);

    Integer selectCountByCondition(@Param("contacters") String user, @Param("text") String text);

//    @Insert(SCRIPT_START +
//            "insert into consume_group\n" +
//            "    <trim prefix='(' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        id,\n" +
//            "      </if>\n" +
//            "      <if test='groupName != null'>\n" +
//            "        group_name,\n" +
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
//            "      <if test='alarmIsEnable != null'>\n" +
//            "        alarm_is_enable,\n" +
//            "      </if>\n" +
//            "      <if test='alarmGroup != null'>\n" +
//            "        alarm_group,\n" +
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
//            "      <if test='extraParams != null'>\n" +
//            "        extra_params,\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        config,\n" +
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
//            "    </trim>\n" +
//            "    <trim prefix='values (' suffix=')' suffixOverrides=','>\n" +
//            "      <if test='id != null'>\n" +
//            "        #{id,jdbcType=BIGINT},\n" +
//            "      </if>\n" +
//            "      <if test='groupName != null'>\n" +
//            "        #{groupName,jdbcType=VARCHAR},\n" +
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
//            "      <if test='alarmIsEnable != null'>\n" +
//            "        #{alarmIsEnable,jdbcType=TINYINT},\n" +
//            "      </if>\n" +
//            "      <if test='alarmGroup != null'>\n" +
//            "        #{alarmGroup,jdbcType=VARCHAR},\n" +
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
//            "      <if test='extraParams != null'>\n" +
//            "        #{extraParams,jdbcType=VARCHAR},\n" +
//            "      </if>\n" +
//            "      <if test='config != null'>\n" +
//            "        #{config,jdbcType=VARCHAR},\n" +
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
//            "    </trim>" +
//            SCRIPT_END)
//    int insertSelectiveWithPrimaryKey(ConsumeGroup consumeGroup);

}