package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.config.v4.PProxyConfig;
import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.pproxy.kafka.QueueToPartitionManager;
import com.xiaojukeji.carrera.pproxy.kafka.network.*;
import com.xiaojukeji.carrera.pproxy.producer.ConfigManager;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.producer.TopicConfigManager;
import com.xiaojukeji.carrera.pproxy.server.ProducerAsyncServerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.network.Send;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.record.MemoryRecords;
import org.apache.kafka.common.requests.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;
import static org.apache.kafka.common.record.RecordVersion.V2;

public class KafkaApis {

    private RequestChannel requestChannel;

    private ProducerAdapterService producerAdapterService;

    private FetchAdapterService fetchAdapterService;

    private CoordinatorAdapterService coordinatorAdapterService;

    private QueueToPartitionManager queueToPartitionManager;

    private TopicConfigManager topicConfigManager;

    private PProxyConfig proxyConfig;

    public KafkaApis(RequestChannel requestChannel, ProducerPool producerPool, ConfigManager configManager) {
        this.requestChannel = requestChannel;
        this.producerAdapterService = new ProducerAdapterService(producerPool);
        this.coordinatorAdapterService = new CoordinatorAdapterService();
        this.fetchAdapterService = new FetchAdapterService();
        String clusterName = configManager.getProxyConfig().getProxyCluster();
        Map<String, RocketmqConfiguration> rocketmqConfigurationMap = configManager.getProxyConfig().getCarreraConfiguration().getRocketmqConfigurationMap();
        queueToPartitionManager = new QueueToPartitionManager(clusterName, rocketmqConfigurationMap);
        topicConfigManager = configManager.getTopicConfigManager();
        proxyConfig = configManager.getProxyConfig();
    }

    public void handle(Request request) {
        KafkaAdapterLog.debug("handle request apiKey:{}", request.getHeader().apiKey());
        try {
            switch (request.getHeader().apiKey()) {
                case PRODUCE: handleProduceRequest(request); break;
                case FETCH: handleFetchRequest(request); break;
                case LIST_OFFSETS: handleListOffsetRequest(request); break;
                case METADATA: handleTopicMetadataReqeust(request); break;
                case LEADER_AND_ISR : handleLeaderAndIsrRequest(request); break;
                case STOP_REPLICA : handleStopReplicaRequest(request); break;
                case UPDATE_METADATA : handleUpdateMetadataRequest(request); break;
                case CONTROLLED_SHUTDOWN : handleControlledShutdownRequest(request); break;
                case OFFSET_COMMIT : handleOffsetCommitRequest(request); break;
                case OFFSET_FETCH : handleOffsetFetchRequest(request); break;
                case FIND_COORDINATOR : handleFindCoordinatorRequest(request); break;
                case JOIN_GROUP : handleJoinGroupRequest(request); break;
                case HEARTBEAT : handleHeartbeatRequest(request); break;
                case LEAVE_GROUP : handleLeaveGroupRequest(request); break;
                case SYNC_GROUP : handleSyncGroupRequest(request); break;
                case DESCRIBE_GROUPS : handleDescribeGroupRequest(request); break;
                case LIST_GROUPS : handleListGroupsRequest(request); break;
                case SASL_HANDSHAKE : handleSaslHandshakeRequest(request); break;
                case API_VERSIONS : handleApiVersionsRequest(request); break;
                case CREATE_TOPICS : handleCreateTopicsRequest(request); break;
                case DELETE_TOPICS : handleDeleteTopicsRequest(request); break;
                case DELETE_RECORDS : handleDeleteRecordsRequest(request); break;
                case INIT_PRODUCER_ID : handleInitProducerIdRequest(request); break;
                case OFFSET_FOR_LEADER_EPOCH : handleOffsetForLeaderEpochRequest(request); break;
                case ADD_PARTITIONS_TO_TXN : handleAddPartitionToTxnRequest(request); break;
                case ADD_OFFSETS_TO_TXN : handleAddOffsetsToTxnRequest(request); break;
                case END_TXN : handleEndTxnRequest(request); break;
                case WRITE_TXN_MARKERS : handleWriteTxnMarkersRequest(request); break;
                case TXN_OFFSET_COMMIT : handleTxnOffsetCommitRequest(request); break;
                case DESCRIBE_ACLS : handleDescribeAcls(request); break;
                case CREATE_ACLS : handleCreateAcls(request); break;
                case DELETE_ACLS : handleDeleteAcls(request); break;
                case ALTER_CONFIGS : handleAlterConfigsRequest(request); break;
                case DESCRIBE_CONFIGS : handleDescribeConfigsRequest(request); break;
                case ALTER_REPLICA_LOG_DIRS : handleAlterReplicaLogDirsRequest(request); break;
                case DESCRIBE_LOG_DIRS : handleDescribeLogDirsRequest(request); break;
                case SASL_AUTHENTICATE : handleSaslAuthenticateRequest(request); break;
                case CREATE_PARTITIONS : handleCreatePartitionsRequest(request); break;
                case CREATE_DELEGATION_TOKEN : handleCreateTokenRequest(request); break;
                case RENEW_DELEGATION_TOKEN : handleRenewTokenRequest(request); break;
                case EXPIRE_DELEGATION_TOKEN : handleExpireTokenRequest(request); break;
                case DESCRIBE_DELEGATION_TOKEN : handleDescribeTokensRequest(request); break;
                case DELETE_GROUPS : handleDeleteGroupsRequest(request); break;
            }
        } catch (Throwable throwable) {
            KafkaAdapterLog.error("handle request error ", throwable);
            sendNoOpResponseExemptThrottle(request);
        }
    }

    private void handleDeleteGroupsRequest(Request request) {
        KafkaAdapterLog.info("handleDeleteGroupsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleExpireTokenRequest(Request request) {
        KafkaAdapterLog.info("handleExpireTokenRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDescribeTokensRequest(Request request) {
        KafkaAdapterLog.info("handleDescribeTokensRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleRenewTokenRequest(Request request) {
        KafkaAdapterLog.info("handleRenewTokenRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleCreateTokenRequest(Request request) {
        KafkaAdapterLog.info("handleCreateTokenRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleCreatePartitionsRequest(Request request) {
        KafkaAdapterLog.info("handleCreatePartitionsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleSaslAuthenticateRequest(Request request) {
        KafkaAdapterLog.info("handleSaslAuthenticateRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDescribeLogDirsRequest(Request request) {
        KafkaAdapterLog.info("handleDescribeLogDirsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleAlterReplicaLogDirsRequest(Request request) {
        KafkaAdapterLog.info("handleAlterReplicaLogDirsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDescribeConfigsRequest(Request request) {
        KafkaAdapterLog.info("handleDescribeConfigsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleAlterConfigsRequest(Request request) {
        KafkaAdapterLog.info("handleAlterConfigsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDeleteAcls(Request request) {
        KafkaAdapterLog.info("handleDeleteAcls");
        throw new RuntimeException("un support protocol");
    }

    private void handleCreateAcls(Request request) {
        KafkaAdapterLog.info("handleCreateAcls");
        throw new RuntimeException("un support protocol");
    }

    private void handleDescribeAcls(Request request) {
        KafkaAdapterLog.info("handleDescribeAcls");
        throw new RuntimeException("un support protocol");
    }

    private void handleTxnOffsetCommitRequest(Request request) {
        KafkaAdapterLog.info("handleTxnOffsetCommitRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleWriteTxnMarkersRequest(Request request) {
        KafkaAdapterLog.info("handleWriteTxnMarkersRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleEndTxnRequest(Request request) {
        KafkaAdapterLog.info("handleEndTxnRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleAddOffsetsToTxnRequest(Request request) {
        KafkaAdapterLog.info("handleAddOffsetsToTxnRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleAddPartitionToTxnRequest(Request request) {
        KafkaAdapterLog.info("handleAddPartitionToTxnRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleOffsetForLeaderEpochRequest(Request request) {
        KafkaAdapterLog.info("handleOffsetForLeaderEpochRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleInitProducerIdRequest(Request request) {
        KafkaAdapterLog.info("handleInitProducerIdRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDeleteRecordsRequest(Request request) {
        KafkaAdapterLog.info("handleDeleteRecordsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDeleteTopicsRequest(Request request) {
        KafkaAdapterLog.info("handleDeleteTopicsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleCreateTopicsRequest(Request request) {
        KafkaAdapterLog.info("handleCreateTopicsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleApiVersionsRequest(Request request) {
        AbstractResponse response = createApiVersionsResponse(request, 3);
        sendResponseMaybeThrottle(request, response);
    }

    private ApiVersionsResponse createApiVersionsResponse(Request request, int reqeustThrottleMs) {
        ApiVersionsRequest apiVersionsRequest = (ApiVersionsRequest) request.body();
        ApiVersionsResponse response;
        if (apiVersionsRequest.hasUnsupportedRequestVersion()) {
            response = apiVersionsRequest.getErrorResponse(reqeustThrottleMs, Errors.UNSUPPORTED_VERSION.exception());
        } else {
            response = ApiVersionsResponse.apiVersionsResponse(reqeustThrottleMs,V2.value);//todo V2从配置中读取
        }
        return response;
    }

    private void sendResponseMaybeThrottle(Request request, AbstractResponse response) {
        sendResponse(request,response);
    }

    private void sendResponse(Request request, AbstractResponse response) {
        Response res = null;
        if ( null != response) {
            Send responseSend = request.getRequestContext().buildResponse(response);
            res = new SendResponse(request, responseSend, response.toString());
        } else {
            res = new NoOpResponse(request);
        }
        sendResponse(res);
    }


    private void sendNoOpResponseExemptThrottle(Request request) {
        sendResponse(request, null);
    }

    private void sendResponse(Response res) {
        requestChannel.sendResponse(res);
    }

    private void handleSaslHandshakeRequest(Request request) {
        KafkaAdapterLog.info("handleSaslHandshakeRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleListGroupsRequest(Request request) {
        KafkaAdapterLog.info("handleListGroupsRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleDescribeGroupRequest(Request request) {
        KafkaAdapterLog.info("handleDescribeGroupRequest");
        throw new RuntimeException("un support protocol");
    }

    /**
     * 获取
     * @param request
     */
    private void handleSyncGroupRequest(Request request) {
        KafkaAdapterLog.info("handleSyncGroupRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleLeaveGroupRequest(Request request) {
        KafkaAdapterLog.info("handleLeaveGroupRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleHeartbeatRequest(Request request) {
        KafkaAdapterLog.info("handleHeartbeatRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleJoinGroupRequest(Request request) {
        KafkaAdapterLog.info("handleJoinGroupRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleOffsetCommitRequest(Request request) {
        KafkaAdapterLog.info("handleOffsetCommitRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleFindCoordinatorRequest(Request request) {
        KafkaAdapterLog.info("handleOffsetCommitRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleOffsetFetchRequest(Request request) {
        KafkaAdapterLog.info("handleOffsetFetchRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleControlledShutdownRequest(Request request) {
        KafkaAdapterLog.info("handleControlledShutdownRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleUpdateMetadataRequest(Request request) {
        KafkaAdapterLog.info("handleUpdateMetadataRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleStopReplicaRequest(Request request) {
        KafkaAdapterLog.info("handleStopReplicaRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleLeaderAndIsrRequest(Request request) {
        KafkaAdapterLog.info("handleLeaderAndIsrRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleTopicMetadataReqeust(Request request) {
        try {
            MetadataRequest metadataRequest = (MetadataRequest) request.body();
            if (metadataRequest.isAllTopics()) {
                KafkaAdapterLog.warn("allTopic");
                //todo future
                return;
            }
            //todo future 这里只会传进来topic,因此针对消费者的服务发现也是以topic为维度，如果服务发现是topic粒度，则需要watch一份topic配置。先实现集群粒度的服务发现。
            List<String> topics = metadataRequest.topics();
            List<MetadataResponse.TopicMetadata> topicMetadataList = new ArrayList<>();
            List<Node> brokerList = null;//todo 本该是所有broker的列表,暂时只支持所有topic使用相同的proxy ip list

            for(String topic: topics) {
                List<Address> proxyAddress = new ArrayList<>();
                if (!CollectionUtils.isEmpty(proxyConfig.getCarreraConfiguration().getKafkaAdapterVip())) {
                    for (String ipPortStr : proxyConfig.getCarreraConfiguration().getKafkaAdapterVip()) {
                        String[] addressArray = ipPortStr.split(":");
                        proxyAddress.add(new Address(addressArray[0], new Integer(addressArray[1])));
                    }
                } else {
                    proxyAddress = topicConfigManager.getProxyIpList(topic);
                }
                Collections.sort(proxyAddress);
                List<Node> nodeList = new ArrayList<>();
                Integer proxyId = 0;
                for (Address address : proxyAddress) {
                    Node node = new Node(proxyId++, address.getHost(), address.getPort());
                    nodeList.add(node);
                }
                Collections.shuffle(nodeList);
                brokerList = nodeList;

                queueToPartitionManager.initMessageQueueInfo(topic);
                Set<Integer> partitions = queueToPartitionManager.getQidToQueueInfo(topic).keySet();
                List<MetadataResponse.PartitionMetadata> partitionMetadataList = new ArrayList<>();
                AtomicInteger index = new AtomicInteger();
                partitions.forEach(partition -> {
                    Node masterNode = nodeList.get(index.getAndIncrement() % nodeList.size());
                    MetadataResponse.PartitionMetadata partitionMetadata = new MetadataResponse.PartitionMetadata(Errors.NONE,partition, masterNode, nodeList, nodeList ,Collections.EMPTY_LIST);
                    partitionMetadataList.add(partitionMetadata);
                });

                MetadataResponse.TopicMetadata topicMetadata = new MetadataResponse.TopicMetadata(Errors.NONE, topic, false, partitionMetadataList);
                topicMetadataList.add(topicMetadata);
            }
            MetadataResponse metadataResponse = new MetadataResponse( 0, brokerList, "xxxxx",0, topicMetadataList);
            sendResponseMaybeThrottle(request, metadataResponse);
        } catch (Exception e) {
            e.printStackTrace();//todo 处理错误
            //todo 所有的错误均需要返回响应，否则由于Selector被mute，无法收发消息
            sendNoOpResponseExemptThrottle(request);
        }
    }

    private void handleListOffsetRequest(Request request) {
        KafkaAdapterLog.info("handleListOffsetRequest");
        throw new RuntimeException("un support protocol");
    }

    private void handleProduceRequest(Request request) {
        try{
            ProduceRequest produceRequest = (ProduceRequest) request.body();
            if (produceRequest.isTransactional()) {
                throw new RuntimeException("un support protocol");
            }
            Map<TopicPartition, MemoryRecords> recordsMap = produceRequest.partitionRecordsOrFail();
            producerAdapterService.send(
                    produceRequest.timeout(),
                    produceRequest.acks(),
                    recordsMap,
                    (Consumer<Map<TopicPartition, ProduceResponse.PartitionResponse>>) responseStatus -> {
                        sendResponse(request, new ProduceResponse(responseStatus));
                    }
            );
        }catch (Exception e) {
            KafkaAdapterLog.error("handleProduceRequest error {}",request);
            sendNoOpResponseExemptThrottle(request);
        }
    }

    private void handleFetchRequest(Request request) {
        throw new RuntimeException("un support protocol");
    }
}
