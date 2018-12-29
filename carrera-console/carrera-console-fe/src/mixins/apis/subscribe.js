import request from '../request.js';

export default {
  methods: {
    request,

    // 获取订阅关系列表
    fetchSubscribeList (options) {
      let method = 'get';
      let url = '/carrera/api/console/sub/list';
      return this.request(method, url, options);
    },

    // 删除订阅关系
    deleteSubscribe (options) {
      let method = 'get';
      let url = '/carrera/api/odin/internal/deleteSub';
      return this.request(method, url, options);
    },

    // 模糊搜索订阅关系
    searchSubscribes (options) {
      let method = 'get';
      let url = 'searchSub';
      return this.request(method, url, options);
    },

    // 启用禁用订阅
    changeSubscribeState (options) {
      let method = 'get';
      let url = '/carrera/api/console/sub/changeState';
      return this.request(method, url, options);
    },

    // 新建、编辑订阅关系
    createSub (options) {
      let method = 'post';
      let url = '/carrera/api/odin/internal/createSub';
      return this.request(method, url, options);
    },

    // 新建订阅关系字段描述
    fetchSubDesc (options) {
      let method = 'get';
      let url = '/carrera/api/console/common/sub/fieldDesc';
      return this.request(method, url, options);
    },

    fetchGroupsWithOutPage (options) {
      let method = 'get';
      let url = '/carrera/api/console/group/listAllWithoutPage';
      return this.request(method, url, options);
    },

    fetchTopicsWithOutPage (options) {
      let method = 'get';
      let url = '/carrera/api/console/topic/listAllWithoutPage';
      return this.request(method, url, options);
    },

    // 消息推送方式列表
    fetchMsgPushType (options) {
      let method = 'get';
      let url = '/carrera/api/console/sub/listMsgPushType';
      return this.request(method, url, options);
    },

    fetchDetailById (options) {
      let method = 'get';
      let url = '/carrera/api/console/sub/findById';
      return this.request(method, url, options);
    }

  }
};
