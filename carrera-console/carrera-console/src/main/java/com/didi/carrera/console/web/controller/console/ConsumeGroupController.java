package com.didi.carrera.console.web.controller.console;


import com.didi.carrera.console.service.ConsumeGroupService;
import com.didi.carrera.console.web.AbstractBaseController;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupBo;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupResetOffsetBo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;


@Controller
@RequestMapping("/api/console/group")
public class ConsumeGroupController extends AbstractBaseController {

    @Resource(name = "didiConsumeGroupServiceImpl")
    private ConsumeGroupService consumeGroupService;

    @ResponseBody
    @RequestMapping(value = {"/listAll"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listAll(@RequestParam String user, String text, @RequestParam Integer curPage, @RequestParam Integer pageSize) {
        return consumeGroupService.findAll(user, text, curPage, pageSize);
    }

    @ResponseBody
    @RequestMapping(value = {"/listAllWithoutPage"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listAllWithoutPage(@RequestParam String user) {
        return consumeGroupService.findAllWithoutPage(user);
    }

    @ResponseBody
    @RequestMapping(value = {"/create"}, method = {RequestMethod.POST})
    public ConsoleBaseResponse<?> create(@Valid @RequestBody ConsumeGroupBo groupBo, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            String msg = getBindingResultErrorInfo(bindingResult);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, msg);
        }

        return consumeGroupService.create(groupBo);
    }

    @ResponseBody
    @RequestMapping(value = {"/changeState"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> changeState(@RequestParam String user, @RequestParam Long groupId, @RequestParam Integer state) throws Exception {
        return consumeGroupService.changeState(user, groupId, state);
    }

    @ResponseBody
    @RequestMapping(value = {"/consumeState"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> consumeState(@RequestParam String user, @RequestParam Long groupId, @RequestParam(required = false) Long topicId, @RequestParam(required = false) Long clusterId) {
        return consumeGroupService.getConsumeState(user, groupId, topicId, clusterId);
    }

    @ResponseBody
    @RequestMapping(value = {"/consumeState/searchItemList"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> consumeStateSearchItemList(@RequestParam String user, @RequestParam Long groupId) {
        return consumeGroupService.findSearchItem(user, groupId);
    }

    @ResponseBody
    @RequestMapping(value = {"/resetOffset"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> resetOffset(@Valid ConsumeGroupResetOffsetBo offsetBo, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            String msg = getBindingResultErrorInfo(bindingResult);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, msg);
        }
        return consumeGroupService.resetOffset(offsetBo);
    }
}
