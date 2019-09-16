<?php if (!defined('BASEPATH')) exit('No direct script access allowed');

$config['carrera'] = array(
    // proxy list
    'CARRERA_PROXY_LIST' => array('127.0.0.1:9713'),
    // time out for each send from proxy to mq broker
    'CARRERA_PROXY_TIMEOUT' => 50,
    // time out for each send from client to proxy
    'CARRERA_CLIENT_TIMEOUT' => 1000,
    // log path
    'CARRERA_CLIENT_LOGPATH' => '/home/xiaoju/webroot/log/mq/',
);