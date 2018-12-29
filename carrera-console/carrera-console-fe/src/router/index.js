import Vue from 'vue';
import Router from 'vue-router';

import TopicRoutes from './topic';
import SubscribeRoutes from './subscribe';
import GroupRoutes from './group';
import IntroduceView from '../pages/introduce';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      component: IntroduceView
    },
    ...TopicRoutes,
    ...SubscribeRoutes,
    ...GroupRoutes
  ]
});
