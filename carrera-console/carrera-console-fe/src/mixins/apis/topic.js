import request from '../request.js';

export default {

  methods: {
    request,

    // 获取所有Topic列表信息
    fetchAllTopicList (options) {
      let method = 'get';
      let url = '/carrera/api/console/topic/listAll';
      return this.request(method, url, options);
    },

    // 获取某个topic统计信息
    fetchTopicStatistics (options) {
      let method = 'get';
      let url = '/carrera/api/console/topic/getState';
      return this.request(method, url, options);
    },

    // 获取topic、集群下的消费组信息
    fetchGroupsByTopic (options) {
      let method = 'get';
      let url = '/carrera/api/console/topic/listGroup';
      return this.request(method, url, options);
    },

    // 消息体采样
    fectchMessages (options) {
      let method = 'get';
      let url = '/carrera/api/console/topic/getMessage';
      return this.request(method, url, options);
    },

    // 发送消息
    sendMessage (options) {
      let method = 'post';
      let url = '/carrera/api/console/topic/sendMsg';
      return this.request(method, url, options);
    },

    // 根据topicId获取详情
    fetchTopicDetail (options) {
      let method = 'get';
      let url = `/carrera/api/console/topic/findById`;
      return this.request(method, url, options);
    },

    // 新建Topic
    createTopic (options) {
      let method = 'post';
      let url = '/carrera/api/odin/internal/createTopic';
      return this.request(method, url, options);
    },

    send (data, cb) {
      let _this = this;
      this.$modal.confirm({
        title: '你确定要发送消息吗？',
        content: `Topic名为:${data.topicName}`,
        buttonType: 'warning',
        onOk () {
          let body = {
            topicId: data.topicId,
            msg: data.msg
          };
          _this.sendMessage(body).then(body => {
            if (body.message === 'ok') {
              this.$message.success(`操作成功，已发送信息，key为` + body.data.key);
            } else {
              cb();
            }
          });
        }
      });
    }
  }
};
