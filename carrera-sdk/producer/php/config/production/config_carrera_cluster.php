<?php if (!defined('BASEPATH')) exit('No direct script access allowed');

$config['carrera'] = array(
    // proxy list
    'CARRERA_PROXY_LIST' => array('127.0.0.1:9613'),
    // time out for each send from proxy to mq broker
    'CARRERA_PROXY_TIMEOUT' => 50,
    // retry times while sending failed from client to proxy
    'CARRERA_CLIENT_RETRY' => 3,
    // time out for each send from client to proxy
    'CARRERA_CLIENT_TIMEOUT' => 100,
    // log path
    'CARRERA_CLIENT_LOGPATH' => '/home/xiaoju/webroot/log/mq/',
);