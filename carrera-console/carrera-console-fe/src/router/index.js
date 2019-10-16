import Vue from 'vue';
import Router from 'vue-router';

// utils
import { login } from './../utils';

import TopicRoutes from './topic';
import SubscribeRoutes from './subscribe';
import GroupRoutes from './group';
import Main from '../components/main';
import IntroduceView from '../pages/introduce';
import LoginView from '../pages/login';

const { getToken } = login;

Vue.use(Router);

const LOGIN_PAGE_NAME = 'login';
const HOME_PAGE_NAME = 'intro';

const router = new Router({
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      name: 'index',
      component: Main,
      children: [
        {
          path: '/',
          redirect: { name:
            'intro'
          }
        },
        {
          path: '/intro',
          name: 'intro',
          component: IntroduceView
        },
        ...TopicRoutes,
        ...SubscribeRoutes,
        ...GroupRoutes
      ].map(_ => ({
        ..._,
        beforeRouteEnter: (to, from, next) => {
          const token = getToken() || '';

          if (!token) {
            // 未登录且要跳转的页面不是登录页
            return next({
              name: LOGIN_PAGE_NAME // 跳转到登录页
            })
          }
          return next();
        }
      }))
    }
  ]
});

export default router;
