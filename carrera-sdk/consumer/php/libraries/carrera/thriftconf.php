<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

$GLOBALS['THRIFT_ROOT'] = FCPATH . 'vendor/packaged/thrift/src';

/* Dependencies. In the proper order. */
require_once $GLOBALS['THRIFT_ROOT'].'/Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Protocol/TCompactProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Protocol/TProtocolDecorator.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Transport/TBufferedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Transport/TFramedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Type/TMessageType.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Factory/TStringFuncFactory.php';
require_once $GLOBALS['THRIFT_ROOT'].'/StringFunc/TStringFunc.php';
require_once $GLOBALS['THRIFT_ROOT'].'/StringFunc/Core.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Type/TType.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Base/TBase.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Exception/TTransportException.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Exception/TProtocolException.php';
require_once $GLOBALS['THRIFT_ROOT'].'/Exception/TApplicationException.php';

require_once __DIR__ . '/Thrift/ConsumerService.php';
require_once __DIR__ . '/Thrift/Types.php';
