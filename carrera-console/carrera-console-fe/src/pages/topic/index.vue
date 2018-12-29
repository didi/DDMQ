<style lang="less">

</style>

<template>
  <div class="container">
    <div class="block">
      <div class="block-header">
        <div class="block-header__title">Topic List</div>
      </div>

      <div class="block-body">
        <div class="bcui-table-toolbar">
          <div class="bcui-table-toolbar__item pull-left">
            <bc-button icon="refresh"
                       type="gray"
                       @click="getUserTopicList"></bc-button>
            <bc-button icon="plus"
                       type="primary"
                       @click="handleCreateTopic">Topic
            </bc-button>
          </div>

          <div class="bcui-table-toolbar__item pull-right">
            <bc-input icon="search" :width="200" placeholder="search" v-model="filter.text" @on-change="handelSearch()"></bc-input>
          </div>
        </div>

        <bc-table :tableData="topicList"
                  :class="['bcui-table--circular', 'bcui-table--dark']"
                  v-loading="loading"
                  :border="false"
                  :pagination="false"
                  class=""
                  empty-text="No data"
                  ref="table">
          <bc-table-column field="topicName"
                           index="topicName"
                           label="Topic"
                           width="400px">
            <template slot-scope="scope">
              <router-link :class="['text-link']"
                           :to="{
                        name: 'topicDetail',
                        params: {
                          id: scope.record.topicId,
                         }
                         }">{{scope.record.topicName}}</router-link>
            </template>
          </bc-table-column>

           <bc-table-column field="clusterName"
                           index="clusterName"
                           label="Cluster">
            <template slot-scope="scope">
              {{scope.record.conf[0].clusterName}}
            </template>
          </bc-table-column>

          <bc-table-column field="produceTps"
                           index="produceTps"
                           label="Rate Limit TPS">
            <template slot-scope="scope">
              {{scope.record.conf[0].produceTps}}
            </template>
          </bc-table-column>
          <bc-table-column field="operate"
                           index="operate"
                           label="Operation"
                           width="200px">
            <template slot-scope="scope">
              <a :class="['text-link']" @click="handleEditTopic(scope.record)">Edit Topic</a>
              <a :class="['text-link']" @click="handleGetMessage(scope.record)">Sampling</a>
            </template>
          </bc-table-column>
        </bc-table>
      </div>

      <div class="bcui-table-toolbar clearfix">
        <div class="bcui-table-toolbar__item pull-right">
          <bc-pagination v-if="topicList.length!==0"
                        show-sizer
                        show-total
                        :page-size="this.pagination.pageSize"
                        :total="this.pagination.totalNum"
                        @on-change="refreshWhenPageChange"
                        @on-page-size-change="refreshWhenPageSizeChange">
          </bc-pagination>
        </div>
      </div>

    </div>
  <CreateTopicModal v-model="createTopicModalStatus" :topic=topicData></CreateTopicModal>

  <bc-drawer
    v-model="messageStatus"
    v-if="messageStatus"
    @on-cancel="cancel">
    <messageDetail :topicId=topicId></messageDetail>
  </bc-drawer>

  </div>

</template>

<script>
  import topicMixins from '../../mixins/apis/topic.js';
  import commonMixins from '../../mixins/common.js';

  import CreateTopicModal from './modals/create.vue';
  import messageDetail from './modals/message.vue';

  export default {
    name: 'topics',
    mixins: [topicMixins, commonMixins],
    components: { CreateTopicModal, messageDetail },

    data () {
      return {
        topicList: [],

        loading: true,
        pagination: {
          totalNum: 0,
          curPage: 1,
          pageSize: 10
        },
        topicData: {},

        filter: {
          text: ''
        },
        createTopicModalStatus: false,
        messageStatus: false,
        topicId: ''
      };
    },

    methods: {

      handleGetMessage (data) {
        this.messageStatus = true;
        this.title = 'Sampling' + '( Topic:' + data.topicName + ')';
        this.topicId = data.topicId;
      },

      cancel () {
        this.messageStatus = false;
      },

      handleEditTopic (topic) {
        this.createTopicModalStatus = true;
        this.topicData = topic;
      },

      getMessages (topicId, clusterId) {
        this.$router.push(`/message/${topicId}/${clusterId}`);
      },

      handelSearch () {
        let { filter } = this;

        if (filter.text !== '') {
          clearTimeout(this.timer);

          this.timer = setTimeout(() => {
            let searchParams = {
              text: filter.text
            };
            localStorage.setItem(
              'topics_search_keyword',
              JSON.stringify(searchParams)
            );
            this.getUserTopicList();
          }, 500);
        } else {
          this.getUserTopicList();
          localStorage.setItem(
            'topics_search_keyword',
            ''
          );
        }
      },

      // 获取初始界面Topics列表
      getUserTopicList (params) {
        let { pagination, filter } = this;
        params = Object.assign(
          {},
          {
            curPage: pagination.curPage,
            pageSize: pagination.pageSize,
            text: filter.text,
            clusterId: 1,
            user: 'administration'
          },
          params
        );
        this.loading = true;
        this.fetchAllTopicList({ params })
          .then((body) => {
            this.topicList = body.data.list;
            this.pagination.totalNum = body.data.totalSize;
          }).finally(() => {
            this.loading = false;
        });
      },

      // 更改页码
      refreshWhenPageChange(curPage) {
        this.pagination.curPage = curPage;
        this.getUserTopicList();
      },

      // 更改页数
      refreshWhenPageSizeChange(pageSize) {
        this.pagination.pageSize = pageSize;
        this.getUserTopicList();
      },

      initUserTopicList () {
        let param = localStorage.getItem('topics_search_keyword');

        if (!param) {
          this.getUserTopicList();
          return;
        }

        param = JSON.parse(param);

        Object.keys(this.filter).forEach((item) => {
          let val = param[item];
          if (val) {
            this.filter[item] = val;
          }
        });

        this.getUserTopicList();
      },

      init () {
        this.initUserTopicList();
        this.$root.bus.$on('updateTopicList', this.getUserTopicList);
      },

      // 跳转topic编辑页面
      goTopicEdit (topicName) {
        this.$router.push(`/topics/create/${topicName}`);
      },

      // 添加
      handleCreateTopic () {
        this.topicData = {};
        this.createTopicModalStatus = true;
      }
    },
    watch: {
    },
    mounted () {
      this.init();
    }
  };
</script>
