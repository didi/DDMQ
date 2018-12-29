exports.createSub = {
  topicId: {
    required: true,
    message: 'Please select topic',
  },
  groupId: {
    required: true,
    message: 'Please select consumer group',
  },

  maxTps: [{
    required: true,
    message: 'Please input Consume maxTps',
  },
  {
    pattern: /^\d+(\.\d+)?$/,
    message: 'Please input integer or float type',
  },
  {
    validator(rule, value, callback) {
      if (value > 1000 * 10000) {
        return callback(new Error('MaxTps can not exceed 1000w'));
      }
      if (value == 0) {
        return callback(new Error('MaxTps must be greater than 0'));
      }
      callback();
    },
  },
  ],

  consumeTimeout: [{
    pattern: /^\d*$/,
    message: 'Please input integer',
  },
  {
    validator(rule, value, callback) {
      if (value > 7200 * 1000) {
        return callback(new Error('Can not exceed 5 day'));
      }
      if (value && value < 1) {
        return callback(new Error('Must be greater than 1ms'));
      }
      callback();
    },
  },
  ],
  errorRetryTimes: [{
    pattern: /-1|^\d+$/,
    message: 'Must be integer and greater than -1',
  },
  {
    validator(rule, value, callback) {
      if (value > 10000) {
        return callback(new Error('Can not exceed 10000'));
      }
      callback();
    },
  },
  ],

  retryIntervals: [{
    max: 1024,
    message: 'Can not exceed 1024 bytes',
  }, {
    pattern: /^((-1|[0-9])+[;]?)*$/,
    message: 'Must be integer and split by ;',
  }],

  apiType: {
    required: true,
    message: 'Please select Api Level',
  },

  msgType: {
    required: true,
    message: 'Please select Message type',
  },

  enableOrder: {
    required: true,
    message: 'Please select Enable sequential consume',
  },

  consumeType: {
    required: true,
    message: 'Please select Consume type',
  },

  orderKey: {
    required: true,
    message: 'Please select Basis ordering rules',
  },

  enableGroovy: {
    required: true,
    message: 'Please select Enable groovyScript',
  },

  groovy: {
    required: true,
    message: 'Please input GroovyScript',
  },
  enableTransit: {
    required: true,
    message: 'Please select Enable transit',
  },

  // urls: {required: true, message: 'url 不能为空'},

  httpMethod: {
    required: true,
    message: 'Please select HttpMethod',
  },

  msgPushType: {
    required: true,
    message: 'Please select Message push type',
  },

  pushMaxConcurrency: [{
    required: true,
    message: 'Please input Push max concurrency',
  },
  {
    pattern: /^\d*$/,
    message: 'Must be integer',
  },
  {
    validator(rule, value, callback) {
      if (value > 100000) {
        return callback(new Error('Can not exceed 100000'));
      }
      if (value < 1) {
        return callback(new Error('Must be greater than 1'));
      }
      callback();
    },

  },
  ],

  queryParams: {
    required: true,
    message: 'Please input QueryParams',
  },

  bigDataType: {
    required: true,
    message: 'Please select Write type',
  },

  bigDataConfig: {
    required: true,
    message: 'Please input Write config',
  },

};
