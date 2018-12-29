import IndexPage from '../pages/group/index.vue';
import ConsumePage from '../pages/group/consume.vue';

const routes = [
  {
    path: '/groups',
    name: 'groups',
    component: IndexPage,
  },
  {
    path: '/group/:id/:name',
    name: 'consume',
    component: ConsumePage,
  },
];

export default routes;
