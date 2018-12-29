import CONSATNT from './constant.js';
const topic = {
  consumeTypes: [
    {
      label: 'SDK Pull',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_SDK
    },
    {
      label: 'HTTP Push',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_HTTP
    },
    {
      label: 'Write to Third party components',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_OTHER
    },
  ],

  delayStates: [
    {
      label: 'Enable',
      value: CONSATNT.TOPIC_DELAY_ENABLE
    },
    {
      label: 'Disable',
      value: CONSATNT.TOPIC_DELAY_DISABLE
    }
  ],

  states: [
    {
      label: 'Enable',
      value: CONSATNT.SUBSCRIBE_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_DISABLE,
      className: 'state--disable'
    }
  ],

  alarmStates: [
    {
      label: 'Enable',
      value: CONSATNT.GROUP_ALARM_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_GROOVY_DISABLE,
      className: 'state--disable'
    }
  ],

  msgTypes: [
    {
      label: 'Json',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_SDK
    },
    {
      label: 'Text',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_HTTP
    },
    {
      label: 'Bytes',
      value: CONSATNT.SUBSCRIBE_CONSUMETYPE_OTHER
    }
  ],

  groovyStates: [
    {
      label: 'Enable',
      value: CONSATNT.SUBSCRIBE_GROOVY_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_GROOVY_DISABLE,
      className: 'state--disable'
    }
  ],

  transitStates: [
    {
      label: 'Enable',
      value: CONSATNT.SUBSCRIBE_TRANSIT_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_TRANSIT_DISABLE,
      className: 'state--disable'
    }
  ],

  pressureTrafficStates: [
    {
      label: 'Enable',
      value: CONSATNT.SUBSCRIBE_PRESSURE_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_PRESSURE_DISABLE,
      className: 'state--disable'
    }
  ],

  orderStates: [
    {
      label: 'Enable',
      value: CONSATNT.SUBSCRIBE_ORDER_ENABLE,
      className: 'state--enable'
    },
    {
      label: 'Disable',
      value: CONSATNT.SUBSCRIBE_ORDER_DISABLE,
      className: 'state--disable'
    }
  ],

  orderKeyTypes: [
    {
      label: 'QID',
      value: CONSATNT.SUBSCRIBE_ORDERKEY_QID
    },
    {
      label: 'KEY',
      value: CONSATNT.SUBSCRIBE_ORDERKEY_KEY
    },
    {
      label: 'JsonPath',
      value: CONSATNT.SUBSCRIBE_ORDERKEY_JSONPATH
    }
  ],

  writeTypes: [
    {
      label: 'HDFS',
      value: CONSATNT.SUBSCRIBE_WRITETYPE_HDFS
    },
    {
      label: 'HBASE',
      value: CONSATNT.SUBSCRIBE_WRITETYPE_HBASE
    },
    {
      label: 'REDIS',
      value: CONSATNT.SUBSCRIBE_WRITETYPE_REDIS
    }
  ],

  httpMethodTypes: [
    {
      label: 'POST',
      value: CONSATNT.SUBSCRIBE_HTTPMETHOD_POST
    },
    {
      label: 'GET',
      value: CONSATNT.SUBSCRIBE_HTTPMETHOD_GET
    }
  ],

  apiTypes: [
    {
      label: 'Low level',
      value: CONSATNT.SUBSCRIBE_API_LOW_LEVEL
    },
    {
      label: 'High level',
      value: CONSATNT.SUBSCRIBE_API_HIGHT_LEVEL
    }
  ]

}

export default { topic }
