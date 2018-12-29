#!/usr/bin/env python
# -*- coding: utf-8 -*-

#carrera 使用 0.9.2 版本的thrift
from thrift import Thrift
from carrera.CarreraProducer import CarreraProducer;

if __name__ == '__main__':
    try:
        producer = CarreraProducer(host='127.0.0.1', port=9613)
        # topic 需要提前在控制台申请
        ret =  producer.send(topic="test", body="hello");
        # 生产结果最好打印到日志中，特别是ret.key，方便追查问题。
        if ret.code <= 1:
            print "send success, result=", ret
        else:
            # 生产失败之后，建议实现重试逻辑。
            print "send failed. result=", ret
    except Thrift.TException as tx:
        print('Thrift.TException %s' % tx.message)
    except:
        print('unknown excpetion')

    
