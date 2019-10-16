import request from '../request.js';

export default {

  methods: {
    request,

    // login
    requestPostLogin (options) {
      let method = 'get';
      let url = '/carrera/api/odin/internal/login';
      return this.request(method, url, options);
    },
    // logout
    requestPostLogout (options) {
      let method = 'get';
      let url = '/carrera/api/odin/internal/logout';
      return this.request(method, url, options);
    }
  }
};
