import IndexPage from '../pages/topic/index.vue';
import DetailPage from '../pages/topic/detail.vue';
import GroupPage from '../pages/topic/modals/group.vue';
import StatisticsPage from '../pages/topic/modals/statistics.vue';

const routes = [
  {
    path: '/',
    name: 'topics',
    component: IndexPage,
  },
  {
    path: '/topics',
    name: 'topics',
    component: IndexPage,
  },
  {
    path: '/topics/:id',
    name: 'topicDetail',
    component: DetailPage,
    children: [
      {
        path: 'group',
        name: 'topic-group-detail',
        component: GroupPage,
      },
      {
        path: 'statistic',
        name: 'topic-statistic-detail',
        component: StatisticsPage,
      },
    ],
  },
];

export default routes;
