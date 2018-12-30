package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.vo.ConsumeGroupSearchItemVo;
import com.didi.carrera.console.service.vo.ConsumeGroupVo;
import com.didi.carrera.console.service.vo.GroupConsumeStateVo;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupBo;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupResetOffsetBo;

import java.util.List;


public interface ConsumeGroupService {
    ConsumeGroup findByGroupName(String groupName);

    ConsumeGroup findById(Long groupId);

    List<ConsumeGroup> findById(List<Long> idList);

    List<ConsumeGroup> findByClusterId(Long clusterId);

    List<ConsumeGroup> findAll();

    ConsoleBaseResponse<PageModel<ConsumeGroupVo>> findAll(String user, String text, Integer curPage, Integer pageSize);

    ConsoleBaseResponse<?> create(ConsumeGroupBo groupBo) throws Exception;

    ConsoleBaseResponse<?> changeState(String user, Long groupId, Integer state) throws Exception;

    ConsoleBaseResponse<List<GroupConsumeStateVo>> getConsumeState(String user, Long groupId, Long topicId, Long clusterId);

    ConsoleBaseResponse<?> resetOffset(ConsumeGroupResetOffsetBo resetOffsetBo) throws Exception;

    ConsoleBaseResponse<ConsumeGroupSearchItemVo> findSearchItem(String user, Long groupId);

    boolean validUserExist(String user, Long groupId);

    ConsoleBaseResponse<List<ConsumeGroupVo>> findAllWithoutPage(String user);

    ConsoleBaseResponse<?> delete(String user, Long groupId) throws Exception;

    void insertOrUpdate(ConsumeGroup group);

}