import request from '../request.js';

export default {

  methods: {
    request,

    // 登陆
    requestPostLogin (options) {
      let method = 'get';
      let url = 'carrera/api/odin/internal/login';
      return this.request(method, url, options);
    },
    // 退出登陆
    requestPostLogout (options) {
      let method = 'get';
      let url = 'carrera/api/odin/internal/logout';
      return this.request(method, url, options);
    }
  }
};
