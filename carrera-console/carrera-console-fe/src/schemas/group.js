// 修改负责RD
exports.createGroup = {
  groupName: [{
    required: true, message: 'Please input consumer group',
  }, {
    pattern: /^[A-Za-z0-9_-]*$/, message: 'Contain only letters, numbers, underscore(_) and dashes(-)',
  }],

};
