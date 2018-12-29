import Vue from 'vue';
import axios from 'axios';
import bus from './eventbus';
import AppView from './app.vue';
import router from './router';

import Core from './ui-core';
import Components from './components';
import Table from './ui-core/components/table/src';

import VueCodeMirror from 'vue-codemirror';
import Clipboard from 'v-clipboard';

import 'flatpickr/dist/flatpickr.min.css';
import './ui-core/styles/index.less';
import './styles/index.less';

Vue.config.productionTip = false;

Vue.use(Core);
Vue.use(Components);
Vue.use(Table);
Vue.use(VueCodeMirror);
Vue.use(Clipboard);

Vue.prototype.$http = axios.create({
  headers: {
    'X-Requested-With': 'XMLHttpRequest'
  }
});

localStorage.setItem('topics_search_keyword', '');
localStorage.setItem('subscribes_search_keyword', '');
localStorage.setItem('group_search_keyword', '');

/* eslint-disable no-new */
new Vue({
  router,
  data () {
    return {
      bus
    };
  },
  render: h => h(AppView)
}).$mount('#app');
