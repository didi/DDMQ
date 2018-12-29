-- MySQL dump 10.13  Distrib 5.7.24, for macos10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: carrera_open_source
-- ------------------------------------------------------
-- Server version	5.7.24

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群名称',
  `description` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '描述信息',
  `idc_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'idc table primarykey',
  `idc` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT 'gz01' COMMENT '集群所属机房',
  `remark` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`,`is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'ddmq','open source',1,'default','default cluster',0,'2017-03-15 03:47:48','2018-12-28 12:34:03');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster_mqserver_relation`
--

DROP TABLE IF EXISTS `cluster_mqserver_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster_mqserver_relation` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `cluster_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '集群id',
  `cluster_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群名称',
  `mq_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'mqserver id',
  `mq_server_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'mqserver名称',
  `proxy_conf` varchar(4096) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群配置',
  `type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '集群和mqserver关联类型，0:pproxy 1:cproxy',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='cluster和mq关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster_mqserver_relation`
--

LOCK TABLES `cluster_mqserver_relation` WRITE;
/*!40000 ALTER TABLE `cluster_mqserver_relation` DISABLE KEYS */;
INSERT INTO `cluster_mqserver_relation` VALUES (1,1,'ddmq',1,'R_default','{\"groupPrefix\":\"carrera_rocketmq_consumer_\",\"pullBatchSize\":32,\"pollNameServerInterval\":2000,\"consumeMessageBatchMaxSize\":1,\"persistConsumerOffsetInterval\":5000,\"pullThresholdForQueue\":1000,\"consumeFromWhere\":\"CONSUME_FROM_LAST_OFFSET\",\"heartbeatBrokerInterval\":2000,\"consumeConcurrentlyMaxSpan\":2000}',1,0,'2018-07-20 11:25:58','2018-12-28 12:36:28'),(2,1,'ddmq',1,'R_default','{\"useKafka\":false,\"warmUpConnection\":true,\"rocketmqConfigurationMap\":{\"R_default\":{\"groupPrefix\":\"carrera_rocketmq_producer_\",\"compressMsgBodyOverHowmuch\":4096,\"pollNameServerInterval\":2000,\"namesrvAddrs\":[\"127.0.0.1:9876\"],\"persistConsumerOffsetInterval\":5000,\"heartbeatBrokerInterval\":10000,\"clientCallbackExecutorThreads\":4,\"retryAnotherBrokerWhenNotStoreOK\":false,\"retryTimesWhenSendFailed\":1,\"sendMsgTimeout\":3000,\"maxMessageSize\":2097152}},\"warmUpFetchTopicRouteInfo\":true,\"rateLimit\":{\"staticMode\":false},\"thriftServer\":{\"workerQueueSize\":20000,\"backlog\":50,\"timeoutCheckerThreads\":3,\"port\":9613,\"acceptQueueSizePerThread\":100,\"selectorThreads\":8,\"maxReadBufferBytes\":5000000,\"clientTimeout\":0,\"workerThreads\":0},\"rocketmqProducers\":3,\"useRocketmq\":true,\"retryDelays\":[200,500,1000,5000,10000],\"autoBatch\":{\"maxBathBytes\":4096,\"encodeWorkerThreads\":4,\"maxBatchMessagesNumber\":32,\"doBatchThresholdBytes\":1024,\"maxContinuouslyRunningMills\":50,\"batchWaitMills\":5,\"maxEncodeWorkerForEachBroker\":1},\"kafkaProducers\":3,\"delay\":{\"chronosInnerTopicPrefix\":\"R_test_chronos_inner_\",\"innerTopicNum\":1},\"paramLength\":{\"tagLenMax\":255,\"failWhenIllegal\":true,\"keyLenMax\":255},\"tpsWarningRatio\":0.8,\"useAutoBatch\":true,\"maxTps\":3000,\"useRequestLimiter\":true,\"defaultTopicInfoConf\":{\"topics\":[]}}',0,0,'2018-07-20 11:27:03','2018-12-28 12:36:39');
/*!40000 ALTER TABLE `cluster_mqserver_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consume_group`
--

DROP TABLE IF EXISTS `consume_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consume_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `group_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'groupname',
  `service` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '成本分摊方',
  `department` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '部门，;分割',
  `contacters` varchar(512) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '负责rd，;分割',
  `alarm_is_enable` tinyint(3) NOT NULL DEFAULT '0' COMMENT '0启用报警 1禁用报警',
  `alarm_group` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '报警组信息, ;分割',
  `alarm_level` tinyint(3) NOT NULL DEFAULT '2' COMMENT '报警级别，默认 二级报警，1，2，3级报警',
  `alarm_msg_lag` int(11) NOT NULL DEFAULT '10000' COMMENT '消息积压报警阈值，默认积压10000条',
  `alarm_delay_time` int(11) NOT NULL DEFAULT '300000' COMMENT '消息延迟报警时间，单位ms，默认5分钟',
  `broadcast_consume` tinyint(3) NOT NULL DEFAULT '1' COMMENT '是否广播消费 0启用 1禁用，默认1',
  `consume_mode` tinyint(3) NOT NULL DEFAULT '1' COMMENT '0同机房消费 1跨机房消费 2自定义，默认1',
  `consume_mode_mapper` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '自定义消费模式，map结构，key为client idc，value为cproxy idc列表',
  `extra_params` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '额外参数',
  `config` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '运维端配置参数',
  `remark` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT ' group 备注',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_groupname` (`group_name`,`is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='消费组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consume_group`
--

LOCK TABLES `consume_group` WRITE;
/*!40000 ALTER TABLE `consume_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `consume_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consume_subscription`
--

DROP TABLE IF EXISTS `consume_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consume_subscription` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `group_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '组id',
  `group_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '组名称',
  `topic_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'topic主键id',
  `topic_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'topic名称',
  `cluster_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '集群id',
  `cluster_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群名称',
  `mq_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'mqserver id',
  `mq_server_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'mqserver名称',
  `pressure_traffic` tinyint(3) NOT NULL DEFAULT '0' COMMENT '0 否不接收压测流量，1 接收压测流量，默认0否不接收压测流量',
  `max_tps` double(11,2) NOT NULL DEFAULT '1000.00' COMMENT '限流tps',
  `alarm_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '报警类型：0.继承消费组配置 1.单独配置',
  `alarm_is_enable` tinyint(3) NOT NULL DEFAULT '0' COMMENT '0启用报警 1禁用报警',
  `alarm_level` tinyint(3) NOT NULL DEFAULT '2' COMMENT '报警级别，默认 二级报警，1，2，3级报警',
  `alarm_msg_lag` int(11) NOT NULL DEFAULT '10000' COMMENT '消息积压报警阈值，默认积压10000条',
  `alarm_delay_time` int(11) NOT NULL DEFAULT '300000' COMMENT '消息延迟报警时间，单位ms，默认5分钟',
  `api_type` tinyint(3) NOT NULL DEFAULT '1' COMMENT '使用消息接口类型，1：highlevel 2 lowlevel，默认1',
  `consume_timeout` int(11) NOT NULL DEFAULT '1000' COMMENT '消息超时时间，默认1000ms，单位ms',
  `error_retry_times` int(11) NOT NULL DEFAULT '3' COMMENT '消息错误重试次数，默认3次,-1 为一直重试',
  `retry_intervals` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '消息重试间隔，分号分隔',
  `msg_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '消息类型：1Json 2text 3二进制数据',
  `enable_groovy` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否启用groovy  0 启用 1禁用',
  `enable_transit` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否启用Transit 0 启用 1禁用',
  `groovy` text COLLATE utf8_bin COMMENT 'groovy脚本',
  `transit` text COLLATE utf8_bin COMMENT 'transit json字符串，key->val',
  `enable_order` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否启用保序，0启用 1禁用',
  `order_key` varchar(512) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '保序key',
  `consume_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '消费类型：1SDK 2HTTP 3直写第三方组件',
  `big_data_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '写入类型，0:hdfs 1:hbase 2:redis',
  `big_data_config` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '大数据配置内容，json格式',
  `urls` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'url列表，多个以分号分隔',
  `http_method` tinyint(3) NOT NULL DEFAULT '0' COMMENT '0 Post 1Get',
  `http_headers` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'header 分号分隔,key:val;key:val',
  `http_query_params` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'queryParmas,分号分隔,key:val;key:val',
  `msg_push_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT '消息推送方式：1.放在http消息体 2.param=<msg> 3.消息体第一层打平',
  `http_token` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'token 校验',
  `push_max_concurrency` int(11) NOT NULL DEFAULT '0' COMMENT 'http消息支持最大并发',
  `actions` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '消息链，根据config配置推算出actions',
  `config` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '运维端配置参数',
  `state` tinyint(3) NOT NULL DEFAULT '0' COMMENT '消息订阅状态，0:启用 1:禁用',
  `extra_params` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '额外参数',
  `remark` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupid_topicid` (`is_delete`,`group_id`,`topic_id`,`state`),
  KEY `idx_clu_group_consumetype` (`is_delete`,`cluster_id`,`group_id`,`consume_type`,`state`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='消费订阅关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consume_subscription`
--

LOCK TABLES `consume_subscription` WRITE;
/*!40000 ALTER TABLE `consume_subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `consume_subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `idc`
--

DROP TABLE IF EXISTS `idc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `idc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群名称',
  `remark` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`,`is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='机房信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `idc`
--

LOCK TABLES `idc` WRITE;
/*!40000 ALTER TABLE `idc` DISABLE KEYS */;
INSERT INTO `idc` VALUES (1,'default','',0,'2018-06-22 11:08:18','2018-12-28 09:40:19');
/*!40000 ALTER TABLE `idc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mq_server`
--

DROP TABLE IF EXISTS `mq_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mq_server` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'mq 集群名称',
  `description` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '描述信息',
  `addr` varchar(512) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'mq 集群地址',
  `type` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'mq 集群类型 0:rmq 1:kafka 2:virtual kafka',
  `idc_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'idc table primarykey',
  `idc` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT 'gz01' COMMENT '集群所属机房',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='mq 集群信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mq_server`
--

LOCK TABLES `mq_server` WRITE;
/*!40000 ALTER TABLE `mq_server` DISABLE KEYS */;
INSERT INTO `mq_server` VALUES (1,'R_default','rocketmq','127.0.0.1:9876',0,1,'default',0,'2017-04-17 14:56:17','2018-12-28 12:37:38');
/*!40000 ALTER TABLE `mq_server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node`
--

DROP TABLE IF EXISTS `node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `cluster_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '集群id',
  `model_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '机型id',
  `master_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'broker主节点',
  `host` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '机器地址 ip:port',
  `node_type` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'node类型, kafkabroker, rmqbroker xxxx',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_clusterid_host` (`is_delete`,`cluster_id`,`host`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='broker 信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node`
--

LOCK TABLES `node` WRITE;
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
/*!40000 ALTER TABLE `node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topic` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `topic_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'topic名称',
  `topic_schema` text COLLATE utf8_bin COMMENT 'topicschema',
  `service` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '成本分摊方',
  `department` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '部门 一级部门 - 二级部门',
  `contacters` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '联系人，;分割',
  `alarm_group` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '报警组信息, ;分割',
  `alarm_is_enable` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否启用报警，0 启用 1禁用，默认启用',
  `delay_topic` tinyint(3) NOT NULL DEFAULT '1' COMMENT '是否是延时Topic，0 延时 1非延时，默认1',
  `need_audit_subinfo` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否需要审核订阅信息 0需要审核订阅信息 1不需要审核订阅信息，默认1',
  `enable_schema_verify` tinyint(3) NOT NULL DEFAULT '1' COMMENT '是否启用schema校验 0启用 1禁用，默认1',
  `produce_mode` tinyint(3) NOT NULL DEFAULT '0' COMMENT '0 同机房生产 1 自定义，默认0',
  `produce_mode_mapper` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '生产模式自定义，map结构，key为client idc，value为pproxy idc列表',
  `state` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'topic状态，0启用,1禁用',
  `config` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '运维端配置参数',
  `description` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT ' topic描述信息',
  `extra_params` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '额外参数',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_topicname` (`topic_name`,`is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='topic信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic`
--

LOCK TABLES `topic` WRITE;
/*!40000 ALTER TABLE `topic` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_conf`
--

DROP TABLE IF EXISTS `topic_conf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topic_conf` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `topic_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'topic 主键id',
  `topic_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'topic名称',
  `cluster_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '集群id',
  `cluster_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群名称',
  `mq_server_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'mqserver id',
  `mq_server_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'mqserver名称',
  `server_idc_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'server对应的机房id',
  `server_idc_name` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'server对应的机房名称',
  `client_idc` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'clientidc对应的id和value',
  `produce_tps` int(11) NOT NULL DEFAULT '1024' COMMENT '预估生产tps,默认1024',
  `msg_avg_size` int(11) NOT NULL DEFAULT '0' COMMENT '消息平均大小，单位字节',
  `msg_max_size` int(11) NOT NULL DEFAULT '0' COMMENT '消息最大大小，单位字节',
  `state` tinyint(3) NOT NULL DEFAULT '0' COMMENT 'topic状态，0启用,1禁用',
  `config` varchar(2048) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '运维端配置参数',
  `is_delete` tinyint(3) NOT NULL DEFAULT '0' COMMENT '是否删除 0:未删除 1:删除',
  `create_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_topicid_clusterid_mqserverid` (`is_delete`,`topic_id`,`cluster_id`,`mq_server_id`),
  KEY `idx_topicname` (`is_delete`,`topic_name`,`cluster_name`,`mq_server_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='topic集群配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_conf`
--

LOCK TABLES `topic_conf` WRITE;
/*!40000 ALTER TABLE `topic_conf` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic_conf` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-29 14:12:30
