package com.didi.carrera.console.web.controller.odin;


import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.ConsumeGroupService;
import com.didi.carrera.console.service.ConsumeSubscriptionService;
import com.didi.carrera.console.service.TopicService;
import com.didi.carrera.console.service.ZKV4ConfigService;
import com.didi.carrera.console.web.AbstractBaseController;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.AcceptTopicConfBo;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import com.didi.carrera.console.web.controller.bo.TopicOrderBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;


@Controller("odinInternalController")
@RequestMapping("/api/odin/internal")
public class InternalController extends AbstractBaseController {

    @Resource(name = "didiTopicServiceImpl")
    private TopicService topicService;

    @Resource(name = "didiConsumeGroupServiceImpl")
    private ConsumeGroupService consumeGroupService;

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    @Autowired
    private ZKV4ConfigService zkv4ConfigService;

    @ResponseBody
    @RequestMapping(value = {"/createTopic"}, method = {RequestMethod.POST})
    public ConsoleBaseResponse<?> createTopic(@Valid @RequestBody TopicOrderBo<AcceptTopicConfBo> topicinfo, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            String msg = getBindingResultErrorInfo(bindingResult);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, msg);
        }

        return topicService.create(topicinfo);
    }

    @ResponseBody
    @RequestMapping(value = {"/createSub"}, method = {RequestMethod.POST})
    public ConsoleBaseResponse<?> createSub(@Valid @RequestBody ConsumeSubscriptionOrderBo subBo, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            String msg = getBindingResultErrorInfo(bindingResult);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, msg);
        }

        return consumeSubscriptionService.createConsumeSubscription(subBo);
    }

    @ResponseBody
    @RequestMapping(value = {"/deleteGroup"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> deleteGroup(@RequestParam String user, @RequestParam Long groupId) throws Exception {
        return consumeGroupService.delete(user, groupId);
    }

    @ResponseBody
    @RequestMapping(value = {"/deleteSub"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> deleteSub(@RequestParam String user, @RequestParam Long subId) throws Exception {
        return consumeSubscriptionService.delete(user, subId);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/initZkPath"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> initZkPath() throws Exception {
        zkv4ConfigService.initZkPath();
        return ConsoleBaseResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/initAllZk"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> initAllZk() throws Exception {
        zkv4ConfigService.initAllZk();
        return ConsoleBaseResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/addPProxy"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> addPProxy(@RequestParam String cluster, @RequestParam String host) throws Exception {
        return topicService.addPProxy(cluster, host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/addCProxy"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> addCProxy(@RequestParam String cluster, @RequestParam String host) throws Exception {
        return consumeSubscriptionService.addCProxy(cluster, host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushTopicConfig"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushTopicConfig(@RequestParam String topic) throws Exception {
        return zkv4ConfigService.pushTopicConfig(topic);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushGroupConfig"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushGroupConfig(@RequestParam String group) throws Exception {
        return zkv4ConfigService.pushGroupConfig(group);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushPProxyConfig"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushPproxyConfig(@RequestParam String host) throws Exception {
        return zkv4ConfigService.pushPproxyConfig(host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushCProxyConfig"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushCproxyConfig(@RequestParam String host) throws Exception {
        return zkv4ConfigService.pushCproxyConfig(host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushTopicConfigByCluster"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushTopicConfigByCluster(@RequestParam String cluster) throws Exception {
        return zkv4ConfigService.pushTopicByCluster(cluster);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushGroupConfigByCluster"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushGroupConfigByCluster(@RequestParam String cluster) throws Exception {
        return zkv4ConfigService.pushGroupByCluster(cluster);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushPProxyConfigByCluster"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushPProxyConfigByCluster(@RequestParam String cluster) throws Exception {
        return zkv4ConfigService.pushPProxyByCluster(cluster);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/pushCProxyConfigByCluster"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> pushCProxyConfigByCluster(@RequestParam String cluster) throws Exception {
        return zkv4ConfigService.pushCProxyByCluster(cluster);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/addPProxyByTopic"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> addPProxyByTopic(@RequestParam String topic, @RequestParam String cluster, @RequestParam String host) throws Exception {
        return topicService.addPProxy(topic, cluster, host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/removePProxy"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> removePProxy(@RequestParam String host) throws Exception {
        return topicService.removePProxy(host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/removePProxyByTopic"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> removePProxyByTopic(@RequestParam String topic, @RequestParam String host) throws Exception {
        return topicService.removePProxy(topic, host);
    }


    @ResponseBody
    @RequestMapping(value = {"/v4/addCProxyByGroup"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> addCProxyByGroup(@RequestParam String group, @RequestParam String cluster, @RequestParam String host) throws Exception {
        return consumeSubscriptionService.addCProxy(group, cluster, host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/removeCProxyByGroup"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> removeCProxyByGroup(@RequestParam String group, @RequestParam String host) throws Exception {
        return consumeSubscriptionService.removeCProxy(group, host);
    }

    @ResponseBody
    @RequestMapping(value = {"/v4/removeCProxy"}, method = {RequestMethod.GET})
    public ConsoleBaseResponse<?> removeCProxy(@RequestParam String host) throws Exception {
        return consumeSubscriptionService.removeCProxy(host);
    }
}
