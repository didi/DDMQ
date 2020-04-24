package com.didi.carrera.console.web.controller.console;


import com.didi.carrera.console.service.TopicService;
import com.didi.carrera.console.web.AbstractBaseController;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


@Controller
@RequestMapping("/api/console/topic")
public class TopicController extends AbstractBaseController {

    @Resource(name = "didiTopicServiceImpl")
    private TopicService topicService;

    @ResponseBody
    @RequestMapping(value = {"/listAll"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listAll(@RequestParam(defaultValue = "0") Long clusterId, String text, @RequestParam String user, @RequestParam Integer curPage, @RequestParam Integer pageSize) {
        return topicService.findAll(clusterId, text, user, curPage, pageSize);
    }

    @ResponseBody
    @RequestMapping(value = {"/listAllWithoutPage"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listAllWithoutPage(@RequestParam String user) {
        return topicService.findAllSimple(user);
    }

    @ResponseBody
    @RequestMapping(value = {"/findById"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> findById(@RequestParam Long topicId, @RequestParam String user) {
        return topicService.findVoById(topicId);
    }

    @ResponseBody
    @RequestMapping(value = {"/getState"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> getState(@RequestParam String user, @RequestParam Long topicId, @RequestParam Long clusterId) {
        return topicService.findState(user, topicId, clusterId);
    }

    @ResponseBody
    @RequestMapping(value = {"/getMessage"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> getMessage(@RequestParam String user, @RequestParam Long topicId, @RequestParam Long clusterId) {
        return topicService.findMessage(user, topicId, clusterId);
    }

    @ResponseBody
    @RequestMapping(value = {"/listGroup"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ConsoleBaseResponse<?> listGroup(@RequestParam String user, @RequestParam Long topicId, @RequestParam Long clusterId) {
        return topicService.findGroup(user, topicId, clusterId);
    }
}
