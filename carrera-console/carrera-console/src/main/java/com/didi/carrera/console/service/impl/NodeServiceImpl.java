package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.NodeType;
import com.didi.carrera.console.dao.mapper.NodeMapper;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ClusterMqserverRelation;
import com.didi.carrera.console.dao.model.Node;
import com.didi.carrera.console.dao.model.NodeCriteria;
import com.didi.carrera.console.service.ClusterMqserverRelationService;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.NodeService;
import com.didi.carrera.console.service.ZKV4ConfigService;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.NodeBo;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("didiNodeServiceImpl")
public class NodeServiceImpl implements NodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private ClusterMqserverRelationService clusterMqserverRelationService;

    @Autowired
    private ZKV4ConfigService zkv4ConfigService;

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Override
    public List<Node> findByClusterId(Long clusterId) {
        NodeCriteria nc = new NodeCriteria();
        nc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId);
        return nodeMapper.selectByExample(nc);
    }

    @Override
    public Node findById(Long nodeId) {
        Node node = nodeMapper.selectByPrimaryKey(nodeId);
        if (node == null) {
            return null;
        }

        return node;
    }

    @Override
    public List<Node> findAll() {
        NodeCriteria nc = new NodeCriteria();
        nc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return nodeMapper.selectByExample(nc);
    }

    @Override
    public List<Node> findByClusterIdNodeType(Long clusterId, NodeType nodeType) {
        NodeCriteria nc = new NodeCriteria();
        nc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId).andNodeTypeEqualTo(nodeType.getIndex());
        return nodeMapper.selectByExample(nc);
    }

    @Override
    public List<Node> findByHostNodeType(String host, NodeType nodeType) {
        NodeCriteria nc = new NodeCriteria();
        nc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andHostEqualTo(host).andNodeTypeEqualTo(nodeType.getIndex());
        return nodeMapper.selectByExample(nc);
    }

    @Override
    public List<Node> findByClusterHostNodeType(Long clusterId, String host, NodeType nodeType) {
        NodeCriteria nc = new NodeCriteria();
        nc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId).andHostEqualTo(host).andNodeTypeEqualTo(nodeType.getIndex());
        return nodeMapper.selectByExample(nc);
    }


    private List<Node> findByCondition(Node node) {
        NodeCriteria nc = new NodeCriteria();
        NodeCriteria.Criteria ncc = nc.createCriteria();
        ncc.andIsDeleteEqualTo(IsDelete.NO.getIndex());
        if (node.getClusterId() != null) {
            ncc.andClusterIdEqualTo(node.getClusterId());
        }
        if (node.getNodeType() != null && NodeType.getByIndex(node.getNodeType()) != null) {
            ncc.andNodeTypeEqualTo(node.getNodeType());
        }
        if (node.getHost() != null) {
            ncc.andHostEqualTo(node.getHost());
        }
        if (node.getMasterId() != null) {
            ncc.andMasterIdEqualTo(node.getMasterId());
        }
        return nodeMapper.selectByExample(nc);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> create(NodeBo bo) throws Exception {

        Node node = new Node();
        node.setClusterId(bo.getClusterid());
        node.setNodeType(bo.getNodetype());
        node.setHost(bo.getHost());
        node.setModelId(bo.getModelid());
        node.setIsDelete(IsDelete.NO.getIndex());
        node.setCreateTime(new Date());

        create(node);

        Map<String, Object> ret = Maps.newHashMap();
        ret.put("id", node.getId());
        return ConsoleBaseResponse.success(ret);
    }

    private Long create(Node node) throws Exception {
        Cluster cluster = clusterService.findById(node.getClusterId());
        if (cluster == null) {
            throw new RuntimeException("cluster not found");
        }

        if (CollectionUtils.isNotEmpty(findByCondition(node))) {
            throw new RuntimeException("node existed");
        }

        nodeMapper.insertSelective(node);
        updateV4Zk(node, cluster);

        return node.getId();
    }

    private void updateV4Zk(Node node, Cluster cluster) throws Exception {
        Byte nodeType = node.getNodeType();
        if (nodeType == NodeType.CONSUMER_PROXY.getIndex()) {
            zkv4ConfigService.updateCProxyConfig(node.getId());
        } else if (nodeType == NodeType.PRODUCER_PROXY.getIndex()) {
            zkv4ConfigService.updatePProxyConfig(node.getId());
        } else if (nodeType == NodeType.ROCKETMQ_BROKER_MASTER.getIndex() || nodeType == NodeType.ROCKETMQ_BROKER_SLAVE.getIndex()) {
            List<ClusterMqserverRelation> relationList = clusterMqserverRelationService.findByClusterId(cluster.getId());
            if (CollectionUtils.isEmpty(relationList)) {
                LOGGER.warn("MqServer Relation not found, cluster=" + cluster.getName());
                return;
            }

            for (ClusterMqserverRelation relation : relationList) {
                zkv4ConfigService.updateBrokerConfig(relation.getMqServerId());
            }
        }
    }
}