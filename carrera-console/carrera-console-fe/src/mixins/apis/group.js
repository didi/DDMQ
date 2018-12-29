import request from '../request.js';

export default {
  methods: {
    request,

    // 获取Group列表
    fectchGroupList (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/listAll';
      return this.request(method, url, options);
    },

    // 新建Group
    createGroup (options) {
      let method = 'post';
      let url = '/carrera/api/console/group/create';
      return this.request(method, url, options);
    },

    // 删除消费组
    deleteGroup (options) {
      let method = 'get';
      let url = '/carrera/api/odin/internal/deleteGroup';
      return this.request(method, url, options);
    },

    // 启用禁用订阅
    changeAllSubscribeState (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/changeState';
      return this.request(method, url, options);
    },

    // 启用禁用消费组报警
    changeAlarmState (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/changeAlarmState';
      return this.request(method, url, options);
    },

    // 消费进度
    getTopicConsumeState (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/consumeState';
      return this.request(method, url, options);
    },

    // group下的所有Topic
    getTopicsByGroupId (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/consumeState/searchItemList';
      return this.request(method, url, options);
    },

    // 重置
    resetOffset (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/resetOffset';
      return this.request(method, url, options);
    },

    // group字段描述
    getGroupDesc (options) {
      let method = 'get';
      let url = '/carrera/api/console/common/group/fieldDesc';
      return this.request(method, url, options);
    }

  }
};
