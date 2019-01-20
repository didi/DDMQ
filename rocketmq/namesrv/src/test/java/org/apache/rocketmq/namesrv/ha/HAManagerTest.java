package org.apache.rocketmq.namesrv.ha;

import org.apache.rocketmq.common.namesrv.NamesrvConfig;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.TreeSet;



public class HAManagerTest {

    @Test
    public void testSelectSlaveId() {
        HAManager haManager = new HAManager();

        Assert.assertEquals(2, haManager.selectSlaveId(new TreeSet<Long>(Arrays.asList(0l, 1l))));

        Assert.assertEquals(1, haManager.selectSlaveId(new TreeSet<Long>(Arrays.asList(0l, 2l))));

        Assert.assertEquals(1, haManager.selectSlaveId(new TreeSet<Long>(Arrays.asList(0l, 2l, 4l))));

        Assert.assertEquals(2, haManager.selectSlaveId(new TreeSet<Long>(Arrays.asList(1l))));

        Assert.assertEquals(1, haManager.selectSlaveId(new TreeSet<Long>(Arrays.asList(2l))));
    }

    @Test
    public void testSelectMaster() throws Exception {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        NamesrvConfig namesrvConfig = new NamesrvConfig();

        NamesrvController nameSrvController = new NamesrvController(namesrvConfig, nettyServerConfig);
        HAManager haManager = new HAManager(nameSrvController);

        String clusterName = "clusterTest";
        String brokerName = "brokerTest";
        String brokerAddrMaster = "brokerAddrMaster";
        String brokerAddrSlave1 = "brokerAddrSlave1";
        String brokerAddrSlave2 = "brokerAddrSlave2";

        //no broker
        HAManager.RoleChangeInfo roleChangeInfo = haManager.selectNewMaster(clusterName, brokerName);
        Assert.assertEquals(null, roleChangeInfo);

        //only master
        TopicConfigSerializeWrapper topicConfigSerializeWrapperMaster = new TopicConfigSerializeWrapper();
        nameSrvController.getRouteInfoManager().registerBroker(clusterName,brokerAddrMaster, brokerName, 0,
            brokerAddrMaster, 10000L, 1L, topicConfigSerializeWrapperMaster, null, null);
        roleChangeInfo = haManager.selectNewMaster(clusterName, brokerName);
        Assert.assertEquals(null, roleChangeInfo);

        //1 slave
        TopicConfigSerializeWrapper topicConfigSerializeWrapperSlave1 = new TopicConfigSerializeWrapper();
        nameSrvController.getRouteInfoManager().registerBroker(clusterName,brokerAddrSlave1, brokerName, 1,
            null, 10000L, 1L, topicConfigSerializeWrapperSlave1, null, null);
        roleChangeInfo = haManager.selectNewMaster(clusterName, brokerName);
        Assert.assertEquals(brokerAddrSlave1, roleChangeInfo.newMaster.addr);
        Assert.assertEquals(brokerAddrMaster, roleChangeInfo.oldMaster.addr);


        //2 slave, select the phy offset latest
        TopicConfigSerializeWrapper topicConfigSerializeWrapperSlave2 = new TopicConfigSerializeWrapper();
        nameSrvController.getRouteInfoManager().registerBroker(clusterName,brokerAddrSlave2, brokerName, 2,
            null, 10001L, 1L, topicConfigSerializeWrapperSlave2, null, null);
        roleChangeInfo = haManager.selectNewMaster(clusterName, brokerName);
        Assert.assertEquals(brokerAddrSlave2, roleChangeInfo.newMaster.addr);
        Assert.assertEquals(brokerAddrMaster, roleChangeInfo.oldMaster.addr);

        //no master
        nameSrvController.getRouteInfoManager().unregisterBroker(clusterName, brokerAddrMaster, brokerName, 0);
        roleChangeInfo = haManager.selectNewMaster(clusterName, brokerName);
        Assert.assertEquals(brokerAddrSlave2, roleChangeInfo.newMaster.addr);
        Assert.assertEquals(null, roleChangeInfo.oldMaster);
    }
}
