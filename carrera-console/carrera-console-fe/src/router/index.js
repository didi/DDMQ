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
      ]
    }
  ]
});

router.beforeEach((to, from, next) => {
  const token = getToken();
  next()
  // if (!token && to.name !== LOGIN_PAGE_NAME) {
  //   // 未登录且要跳转的页面不是登录页
  //   next({
  //     name: LOGIN_PAGE_NAME // 跳转到登录页
  //   })
  // } else if (!token && to.name === LOGIN_PAGE_NAME) {
  //   // 未登陆且要跳转的页面是登录页
  //   next() // 跳转
  // } else if (token && to.name === LOGIN_PAGE_NAME) {
  //   // 已登录且要跳转的页面是登录页
  //   next({
  //     name: HOME_PAGE_NAME
  //   })
  // }
})

export default router;
