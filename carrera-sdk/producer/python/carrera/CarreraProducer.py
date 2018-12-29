#!/usr/bin/env python
# -*- coding: utf-8 -*-
import random
import string

# import thrift_0_9_2 as thrift
from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TCompactProtocol

from producerProxy import ProducerService
from producerProxy.ttypes import *

class CarreraProducer:
    def __init__(self, host, port=9613, proxyTimeout=1000):
        self._host = host
        self._port = port
        self._proxyTimeout = proxyTimeout
        self._keyLength = 16
        self._sdkVersion = "py-0.2"
        self._socketTimeout = 5000
    
    def genKey(self):
        return ''.join(random.choice('ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789') for _ in range(self._keyLength))

    def send(self, topic, body, key=None):
        # 建立socket
        transport = TSocket.TSocket(self._host, self._port)
        transport.setTimeout(self._socketTimeout)
        # 选择传输层，这块要和服务端的设置一致
        transport = TTransport.TFramedTransport(transport)
        # 打开连接
        transport.open()
        # 选择传输协议，这个也要和服务端保持一致，否则无法通信
        protocol = TCompactProtocol.TCompactProtocol(transport)
        # 创建客户端
        client = ProducerService.Client(protocol)
        
        if key is None:
            key=self.genKey()
        message = Message(topic=topic, key=key, body=body, partitionId=-2, version=self._sdkVersion)
        try:
            result = client.sendSync(message, self._proxyTimeout)
            result.key = message.key
        finally:
            transport.close()
        return result