// 修改负责RD
exports.createTopic = {
  topicName: [
    {required: true,   message: 'Topic name can not be empty.'},
    {validator(rule, value, callback) {
      if (!/^[A-Za-z0-9_-]*$/.test(value)) {
        return callback(new Error('Contain only letters, numbers, underscore (_) and dash (-).'));
      }
      callback();
    },
    }],

  delayTopic: {
    required: true,
    message: 'Delay topic can not be empty.',
  },

  produceTps: [{
    required: true,
    message: 'Rate limit can not be empty.',
  }, {
    pattern: /^\d*$/,
    message: 'Please input integer',
  }, {validator(rule, value, callback) {
    if (value > 1000 * 10000) {
      return callback(new Error('Rate limit can not exceed 1000*10000'));
    }
    if (value && value < 1) {
      return callback(new Error('Rate limit must be greater than 0'));
    }
    callback();
  },
  }],

  description: [
    { required: true, message: 'topic desc can not be empty.'},
    {message: 'Length must be less than 128 bits', max: 128},
    {validator(rule, value, callback) {
      if (/^\s*$/.test(value)) {
        return callback(new Error('Cannot enter a null character'));
      }
      callback();
    },
    }],
};
