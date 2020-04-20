<?php
require_once __DIR__ . '/thriftconf.php';

use didi\carrera\consumer\proxy\ConsumerServiceClient;
use didi\carrera\consumer\proxy\PullRequest;
use didi\carrera\consumer\proxy\AckResult;
use didi\carrera\consumer\proxy\PullResponse;
use didi\carrera\consumer\proxy\FetchRequest;
use didi\carrera\consumer\proxy\ConsumeResult;
use didi\carrera\consumer\proxy\PullException;

use Thrift\Protocol\TCompactProtocol;
use Thrift\Transport\TFramedTransport;
use Thrift\Transport\TSocket;

class Carrera
{
    // request log
    const REQ_LOG = 'mq.log';
    // 异常log
    const DROP_LOG = 'drop.log';
    //拉取消息失败时的延迟重试间隔
    const RETRY_INTERVAL = 2;
    //提交消费状态的最大重试次数
    const SUBMIT_MAX_RETRIES = 2;
    // 一次拉取能获取到的最大消息条数，服务端根据此值和服务端的配置，取最小值
    const MAX_BATCH_SIZE = 1;
    // 拉取消息时，在服务端等待消息的最长时间
    const MAX_LINGER_TIME = 500;
    // 日志格式2016-10-31 12:02:01 || {msg}
    const LOG_FORMAT = "%s || %s";
    /*  ...  */

    // 错误码
    const OK = 0;
    const CACHE_OK = 1;
    const EMPTY_RET = 2;
    const DOWNGRADE = 100;
    const CLIENT_EXCEPTION = 101;
    const MISSING_PARAMETERS = 102;

    const PHP_SDK_VERSION = "carrera_php_1.0";

    /**
     * proxy list
     *
     * @var array
     * @access private
     */
    private $proxyList = array();

    /**
     * default client timeout is 100ms
     *
     * @var float
     * @access private
     */
    private $clientTimeout = 100;

    /**
     * default proxy timeout is 50ms
     *
     * @var float
     * @access private
     */
    private $proxyTimeout = 50;

    /**
     * default client retry time is 3
     *
     * @var float
     * @access private
     */
    private $clientRetry = 2;

    private $cluster = 'ddmq';

    private $log_path;

    public $proxyLocked = false;

    public function __construct()
    {
        $ci = get_instance();
        $ci->load->config('config_carrera_cluster', true);
        $aConfig = $ci->config->item('carrera', 'config_carrera_cluster');
        $this->proxyList = $aConfig['CARRERA_PROXY_LIST'];
        $this->proxyTimeout = $aConfig['CARRERA_PROXY_TIMEOUT'];
        $this->clientTimeout = $aConfig['CARRERA_CLIENT_TIMEOUT'];
        $this->log_path = $aConfig['CARRERA_CLIENT_LOGPATH'];
    }

    public function pull($sGroupId, $sTopic, $iMaxBatchSize = null, $iMaxLingerTime = null, $oResult = null)
    {
        $dropInfo = array(
            'opera_stat_key' => 'carrera_drop',
            'groupId' => $sGroupId,
            'topic' => $sTopic,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'result' => $oResult,
            'version' => self::PHP_SDK_VERSION
        );

        if (!isset($sGroupId) || !isset($sTopic)) {
            return array(
                'code' => self::MISSING_PARAMETERS,
                'msg' => 'missing parameters'
            );
        }
        if (!isset($iMaxBatchSize) || !isset($iMaxLingerTime)) {
            $iMaxBatchSize = self::MAX_BATCH_SIZE;
            $iMaxLingerTime = self::MAX_LINGER_TIME;
        }

        $request = new PullRequest(array(
            'groupId' => $sGroupId,
            'topic' => $sTopic,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'result' => $oResult,
            'version' => self::PHP_SDK_VERSION
        ));

        $startTime = microtime(true);

        $retryCount = 0;
        do {
            try {
                $this->proxyLocked = true;
                $ret = $this->pullWithThrift('pull', $request);
                switch ($ret['code']) {
                    case self::OK:
                        $status = 'success';
                        break;
                    case self::CACHE_OK:
                        $status = 'cache_ok';
                        break;
                    default:
                        $status = 'failure';
                        break;
                }
            } catch (\Exception $e) {
                $this->proxyLocked = false;
                $ret = array(
                    'code' => self::CLIENT_EXCEPTION,
                    'msg' => $e->getMessage(),
                );
                $status = 'failure';
                sleep(self::RETRY_INTERVAL);
            }
        } while ($retryCount++ < $this->clientRetry);

        $used = (microtime(true) - $startTime) * 1000;
        $addr = isset($ret['ip']) ? $ret['ip'] : '';

        $logInfo = array(
            'opera_stat_key' => 'carrera_trace',
            'result' => $status,
            'errno' => $ret['code'],
            'errmsg' => $ret['msg'],
            'ip' => $addr,
            'groupId' => $sGroupId,
            'topic' => $sTopic,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'used' => $used,
            'version' => self::PHP_SDK_VERSION
        );

        if ($ret['code'] > self::CACHE_OK) {
            $dropInfo['errno'] = $ret['code'];
            $dropInfo['errmsg'] = $ret['msg'];
            $this->writeLog($this->log_path . self::DROP_LOG, $dropInfo);
        }
        $this->writeLog($this->log_path . self::REQ_LOG, $logInfo);

        return $ret;
    }

    public function fetch($sGroupId, $sConsumerId, $iMaxBatchSize = null, $iMaxLingerTime = null, array $oOffset = [])
    {
        $dropInfo = array(
            'opera_stat_key' => 'carrera_drop',
            'groupId' => $sGroupId,
            'consumerId' => $sConsumerId,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'fetchOffset' => $oOffset,
            'version' => self::PHP_SDK_VERSION
        );

        if (!isset($sGroupId) || !isset($sConsumerId)) {
            return array(
                'code' => self::MISSING_PARAMETERS,
                'msg' => 'missing parameters'
            );
        }
        if (!isset($iMaxBatchSize) || !isset($iMaxLingerTime)) {
            $iMaxBatchSize = self::MAX_BATCH_SIZE;
            $iMaxLingerTime = self::MAX_LINGER_TIME;
        }

        $request = new FetchRequest(array(
            'consumerId' => $sConsumerId,
            'groupId' => $sGroupId,
            'fetchOffset' => $oOffset,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'cluster' => $this->cluster,
            'version' => self::PHP_SDK_VERSION
        ));

        $startTime = microtime(true);
        try {
            $this->proxyLocked = true;
            $ret = $this->pullWithThrift('fetch', $request);
            switch ($ret['code']) {
                case self::OK:
                    $status = 'success';
                    break;
                case self::CACHE_OK:
                    $status = 'cache_ok';
                    break;
                default:
                    $status = 'failure';
                    break;
            }
        } catch (\Exception $e) {
            $this->proxyLocked = false;
            $ret = array(
                'code' => self::CLIENT_EXCEPTION,
                'msg' => $e->getMessage(),
            );
            $status = 'failure';
        }
        $used = (microtime(true) - $startTime) * 1000;
        $addr = $ret['ip'];

        $logInfo = array(
            'opera_stat_key' => 'carrera_trace',
            'result' => $status,
            'errno' => $ret['code'],
            'errmsg' => $ret['msg'],
            'ip' => $addr,
            'groupId' => $sGroupId,
            'consumerId' => $sConsumerId,
            'cluster' => $this->cluster,
            'maxBatchSize' => $iMaxBatchSize,
            'maxLingerTime' => $iMaxLingerTime,
            'used' => $used,
            'version' => self::PHP_SDK_VERSION
        );

        if ($ret['code'] > self::CACHE_OK) {
            $dropInfo['errno'] = $ret['code'];
            $dropInfo['errmsg'] = $ret['msg'];
            $this->writeLog($this->log_path . self::DROP_LOG, $dropInfo);
        }
        $this->writeLog($this->log_path . self::REQ_LOG, $logInfo);

        return $ret;
    }

    public function ack($sGroupId, $sConsumerId, array $oOffsets = [])
    {
        $dropInfo = array(
            'opera_stat_key' => 'carrera_drop',
            'groupId' => $sGroupId,
            'consumerId' => $sConsumerId,
            'cluster' => $this->cluster,
            'offsets' => $oOffsets
        );

        if (!isset($sGroupId) || !isset($sConsumerId)) {
            return array(
                'code' => self::MISSING_PARAMETERS,
                'msg' => 'missing parameters'
            );
        }

        $request = new AckResult(array(
            'consumerId' => $sConsumerId,
            'groupId' => $sGroupId,
            'cluster' => $this->cluster,
            'offsets' => $oOffsets,
        ));

        $startTime = microtime(true);
        try {
            $ret = $this->pullWithThrift('ack', $request);
            switch ($ret['code']) {
                case self::OK:
                    $status = 'success';
                    break;
                case self::CACHE_OK:
                    $status = 'cache_ok';
                    break;
                default:
                    $status = 'failure';
                    break;
            }
        } catch (\Exception $e) {
            $ret = array(
                'code' => self::CLIENT_EXCEPTION,
                'msg' => $e->getMessage(),
            );
            $status = 'failure';
        }
        $this->proxyLocked = false;
        $used = (microtime(true) - $startTime) * 1000;
        $addr = $ret['ip'];

        $logInfo = array(
            'opera_stat_key' => 'carrera_trace',
            'result' => $status,
            'errno' => $ret['code'],
            'errmsg' => $ret['msg'],
            'ip' => $addr,
            'groupId' => $sGroupId,
            'consumerId' => $sConsumerId,
            'cluster' => $this->cluster,
            'used' => $used
        );

        if ($ret['code'] > self::CACHE_OK) {
            $dropInfo['errno'] = $ret['code'];
            $dropInfo['errmsg'] = $ret['msg'];
            $this->writeLog($this->log_path . self::DROP_LOG, $dropInfo);
        }
        $this->writeLog($this->log_path . self::REQ_LOG, $logInfo);

        return $ret;
    }

    public function submit($oContext, array $aSuccessOffsets, array $aFailOffsets, $oNextResult = null)
    {
        $dropInfo = array(
            'opera_stat_key' => 'carrera_drop',
            'context' => $oContext,
            'nextResult' => $oNextResult,
        );

        if (!isset($oContext) || !isset($aSuccessOffsets) || !isset($aFailOffsets)) {
            return array(
                'code' => self::MISSING_PARAMETERS,
                'msg' => 'missing parameters'
            );
        }

        $request = new ConsumeResult(array(
            'context' => $oContext,
            'successOffsets' => $aSuccessOffsets,
            'failOffsets' => $aFailOffsets,
            'nextResult' => $oNextResult
        ));

        $startTime = microtime(true);
        try {
            $ret = $this->pullWithThrift('submit', $request);
            switch ($ret['code']) {
                case self::OK:
                    $status = 'success';
                    break;
                case self::CACHE_OK:
                    $status = 'cache_ok';
                    break;
                default:
                    $status = 'failure';
                    break;
            }
        } catch (\Exception $e) {
            $ret = array(
                'code' => self::CLIENT_EXCEPTION,
                'msg' => $e->getMessage(),
            );
            $status = 'failure';
        }
        $this->proxyLocked = false;
        $used = (microtime(true) - $startTime) * 1000;
        $addr = $ret['ip'];

        $logInfo = array(
            'opera_stat_key' => 'carrera_trace',
            'result' => $status,
            'errno' => $ret['code'],
            'errmsg' => $ret['msg'],
            'ip' => $addr,
            'used' => $used
        );

        if ($ret['code'] > self::CACHE_OK) {
            $dropInfo['errno'] = $ret['code'];
            $dropInfo['errmsg'] = $ret['msg'];
            $this->writeLog($this->log_path . self::DROP_LOG, $dropInfo);
        }
        $this->writeLog($this->log_path . self::REQ_LOG, $logInfo);

        return $ret;
    }

    private function pullWithThrift($cmd, $request)
    {
        static $proxyAddr;

        $tmpProxyList = $this->proxyList;
        if (!$proxyAddr) {
            $proxyIndex = array_rand($tmpProxyList, 1);
            $proxyAddr = $tmpProxyList[$proxyIndex];
        }

        $retryCount = 0;
        do {
            try {
                if ($retryCount > 1) {
                    if (!$this->proxyLocked) {
                        if (count($tmpProxyList) <= 1) {
                            $tmpProxyList = $this->proxyList;
                        } else {
                            $rmProxyIndex = array_search($proxyAddr, $tmpProxyList);
                            unset($tmpProxyList[$rmProxyIndex]);
                        }
                        $proxyIndex = array_rand($tmpProxyList, 1);
                        $proxyAddr = $tmpProxyList[$proxyIndex];
                    }
                }

                list($hostname, $port) = explode(':', $proxyAddr);
                $socket = new TSocket($hostname, $port);
                $socket->setSendTimeout($this->clientTimeout);
                $socket->setRecvTimeout($this->clientTimeout);
                $transport = new TFramedTransport($socket);
                $transport->open();
                $protocol = new TCompactProtocol($transport);
                $client = new ConsumerServiceClient($protocol);

                $response = $client->$cmd($request);
                $transport->close();

                switch ($cmd) {
                    case 'pull':
                        if ($response instanceof PullResponse) {
                            if ($response->context->qid) {
                                $ret = [
                                    'context' => $response->context,
                                    'messages' => $response->messages
                                ];
                                $result = array(
                                    'ret' => $ret,
                                    'code' => self::OK,
                                    'msg' => 'success',
                                    'ip' => $proxyAddr
                                );
                                return $result;
                            } elseif ($retryCount > 1) {
                                $result = array(
                                    'ret' => null,
                                    'code' => self::EMPTY_RET,
                                    'msg' => 'empty',
                                    'ip' => $proxyAddr
                                );
                                return $result;
                            }
                        }
                        break;
                    default:
                        if ($response !== null) {
                            $result = array(
                                'ret' => $response,
                                'code' => self::OK,
                                'msg' => 'success',
                                'ip' => $proxyAddr
                            );
                            return $result;
                        }
                        break;
                }
                $result = array(
                    'code' => self::CLIENT_EXCEPTION,
                    'msg' => 'failure',
                    'ip' => $proxyAddr
                );
                sleep(self::RETRY_INTERVAL);
            } catch (PullException $e) {
                if (isset($transport)) {
                    $transport->close();
                }
                $result = array(
                    'code' => self::CLIENT_EXCEPTION,
                    'msg' => $e->getMessage(),
                    'ip' => $proxyAddr
                );
            }
        } while ($retryCount++ < $this->clientRetry);

        return $result;
    }

    /**
     * 写日志
     *
     * @param $sPath string 日志文件路径
     * @param @xLog  mixed  日志信息
     *
     * @return void
     */
    private function writeLog($sPath, $mLog)
    {
        if (file_exists(dirname($sPath))) {
            if (is_array($mLog)) {
                $sMsg = json_encode($mLog);
            } else {
                $sMsg = (string)$mLog;
            }
            $sLine = sprintf(self::LOG_FORMAT, date('Y-m-d H:i:s'), $sMsg);
            $rFp = fopen($sPath, 'a+');
            fwrite($rFp, $sLine . "\n");
            fclose($rFp);
        }
    }
}