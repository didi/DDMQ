package com.didi.carrera.console.web.controller.console;


import com.didi.carrera.console.service.ConsumeSubscriptionService;
import com.didi.carrera.console.web.AbstractBaseController;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


@Controller
@RequestMapping("/api/console/sub")
public class ConsumeSubscriptionController extends AbstractBaseController {

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    @ResponseBody
    @RequestMapping(value = {"/list"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> list(@RequestParam String user, String text, Long clusterId, Long groupId, Integer consumeType, Integer state, @RequestParam Integer curPage, @RequestParam Integer pageSize) {
        return consumeSubscriptionService.findAll(user, text, clusterId, groupId, consumeType, state, curPage, pageSize);
    }

    @ResponseBody
    @RequestMapping(value = {"/findById"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> findByGroupTopicCluster(@RequestParam String user, @RequestParam Long groupId, @RequestParam Long topicId, @RequestParam Long clusterId) {
        return consumeSubscriptionService.findByGroupClusterTopicId(groupId, clusterId, topicId);
    }

    @ResponseBody
    @RequestMapping(value = {"/listMsgPushType"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listMsgPushType(@RequestParam String user) {
        return consumeSubscriptionService.findMsgPushType(user);
    }

    @ResponseBody
    @RequestMapping(value = {"/changeState"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> changeState(@RequestParam String user, @RequestParam Long subId, @RequestParam Integer state) throws Exception {
        return consumeSubscriptionService.changeState(user, subId, state);
    }
}
