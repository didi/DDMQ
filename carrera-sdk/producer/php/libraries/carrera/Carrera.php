<?php


require_once __DIR__ . '/thriftconf.php';

use didi\carrera\producer\proxy\Result;
use didi\carrera\producer\proxy\Message;
use didi\carrera\producer\proxy\ProducerServiceClient;

use Thrift\Protocol\TCompactProtocol;
use Thrift\Transport\TFramedTransport;
use Thrift\Transport\TSocket;


class Carrera {
    // request log
    const REQ_LOG = 'mq.log';
    // 异常log
    const DROP_LOG = 'drop.log';
    // 依赖hash分区
    const PARTITION_HASH = -1;
    // 随机分区
    const PARTITION_RAND = -2;
    // 日志格式2016-10-31 12:02:01 || {msg}
    const LOG_FORMAT = "%s || %s";
    /*  ...  */

    // 错误码
    const OK = 0;
    const CACHE_OK = 1;
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

    private $log_path;

    public function __construct() {
        $ci = get_instance();
        $ci->load->config('config_carrera_cluster', true);
        $aConfig = $ci->config->item('carrera', 'config_carrera_cluster');
        $this->proxyList = $aConfig['CARRERA_PROXY_LIST'];
        $this->proxyTimeout = $aConfig['CARRERA_PROXY_TIMEOUT'];
        $this->clientRetry = $aConfig['CARRERA_CLIENT_RETRY'];
        $this->clientTimeout = $aConfig['CARRERA_CLIENT_TIMEOUT'];
        $this->log_path = $aConfig['CARRERA_CLIENT_LOGPATH'];
    }

    public function send($sTopic, $sBody, $iPartition, $iHashId, $sTags = null)
    {
        $dropInfo = array(
            'opera_stat_key' => 'carrera_drop',
            'topic' => $sTopic,
            'partition' => $iPartition,
            'hashID' => $iHashId,
            'body'  => $sBody,
            'tags' => $sTags,
            'version' => Carrera::PHP_SDK_VERSION
        );

        if (!isset($sTopic) || !isset($sBody)) {
            return new Result(array(
                'code' => self::MISSING_PARAMETERS,
                'msg' => 'missing parameters'
            ));
        }
        if (!isset($iPartition) || !isset($iHashId)) {
            $iPartition = self::PARTITION_RAND;
            $iHashId = 0;
        }

        $sKey = md5($sBody . microtime(true));

        $msgObj = new Message(array(
            'topic' => $sTopic,
            'partitionId' => $iPartition,
            'hashId' => $iHashId,
            'value' => $sBody,
            'key' => $sKey,
            'tags' => $sTags,
            'version' => Carrera::PHP_SDK_VERSION
        ));

        $startTime = microtime(true);
        try {

            $result = $this->sendWithThrift($msgObj);

            $ret = $result['ret'];
            $ret->key = $msgObj->key;
            switch($ret->code) {
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
            $ret = new Result(array(
                'code' => self::CLIENT_EXCEPTION,
                'msg' => $e->getMessage(),
                'key' => $msgObj->key
            ));
            $status = 'failure';
        }
        $used = (microtime(true) - $startTime)*1000;
        $addr = $result['ip'];

        $logInfo = array(
            'opera_stat_key' => 'carrera_trace',
            'result' => $status,
            'errno' => $ret->code,
            'errmsg' => $ret->msg,
            'ip' => $addr,
            'topic' => $msgObj->topic,
            'key' => $ret->key,
            'partition' => $msgObj->partitionId,
            'hashID' => $msgObj->hashId,
            'len' => strlen($msgObj->value),
            'used' => $used,
            'version' => Carrera::PHP_SDK_VERSION
        );

        if ($ret->code > self::CACHE_OK) {
            $dropInfo['errno'] = $ret->code;
            $dropInfo['errmsg'] = $ret->msg;
            $dropInfo['key'] = $ret->key;
            $this->writeLog($this->log_path . self::DROP_LOG, $dropInfo);
        }
        $this->writeLog($this->log_path . self::REQ_LOG, $logInfo);

        return $ret;
    }

    private function sendWithThrift($msg) {

        $proxyAddr = null;
        $tmpProxyList = $this->proxyList;
        $retryCount = 0;
        do {
            try {
                if ($proxyAddr != null) {
                    if (count($tmpProxyList) <= 1) {
                        $tmpProxyList = $this->proxyList;
                    } else {
                        $rmProxyIndex = array_search($proxyAddr, $tmpProxyList);
                        unset($tmpProxyList[$rmProxyIndex]);
                    }
                }
                $proxyIndex = array_rand($tmpProxyList, 1);
                $proxyAddr = $tmpProxyList[$proxyIndex];

                list($hostname, $port) = explode(':', $proxyAddr);
                $socket = new TSocket($hostname, $port);
                $socket->setSendTimeout($this->clientTimeout);
                $socket->setRecvTimeout($this->clientTimeout);
                $transport = new TFramedTransport($socket);
                $transport->open();
                $protocol = new TCompactProtocol($transport);
                $client = new ProducerServiceClient($protocol);

                $ret = $client->sendSync($msg, $this->proxyTimeout);
                $transport->close();
                $result = array(
                    'ret' => $ret,
                    'ip' => $proxyAddr
                );
                if ($ret->code <= self::CACHE_OK) {
                    return $result;
                }
            } catch (\Exception $e) {
                $transport->close();
                $result = array(
                    'ret' => new Result(array(
                        'code' => self::CLIENT_EXCEPTION,
                        'msg' => $e->getMessage()
                    )),
                    'ip' => $proxyAddr
                );
            }
        }while($retryCount ++ < $this->clientRetry);

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
    private function writeLog($sPath, $mLog) {
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