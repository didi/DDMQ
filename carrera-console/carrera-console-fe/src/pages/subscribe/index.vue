<style lang="less">

</style>

<template>
  <div class="container">
    <div class="block">

      <div class="block-header">
        <div class="block-header__title">Subscription List</div>
      </div>

      <div class="block-body">
        <div class="bcui-table-toolbar">
          <div class="bcui-table-toolbar__item pull-left">
            <bc-button icon="refresh"
                       type="gray"
                       @click="getSubscribeList"></bc-button>
            <bc-button icon="plus"
                       type="primary"
                       @click="handleCreateSubscribe">Subscription
            </bc-button>
            <bc-button-group>
              <bc-button type="gray"
                         :disabled="hasNoChecked"
                         @click="handleManageSubscribe('enable')">Enable
              </bc-button>
              <bc-button type="gray"
                         :disabled="hasNoChecked"
                         @click="handleManageSubscribe('disable')">Disable
              </bc-button>
            </bc-button-group>

            <bc-button type="gray" :disabled="hasNoChecked" @on-click="handleManageSubscribe('delete')">Delete
            </bc-button>
          </div>

          <div class="bcui-table-toolbar__item pull-right">
            <span class="bcui-table-toolbar__item-label">Consumer group:</span>
            <bc-select v-model="filter.groupId" :width='200' placeholder="Select consumer group" @on-change="handelSearch">
              <bc-option value="">全部</bc-option>
              <bc-option v-for="item in groups" :value="item.value" :key="item.id">{{item.label}}</bc-option>
            </bc-select>
            <bc-input icon="search" :width="200" placeholder="search" v-model="filter.text" @on-change="handelSearch()"></bc-input>
          </div>
        </div>

        <bc-table :tableData="subscribeList"
                  :class="['bcui-table--circular', 'bcui-table--dark']"
                  v-loading="loading"
                  :border="false"
                  :pagination="false"
                  :check="true"
                  :checkAll="true"
                  @on-check="handleCheckTableRow"
                  @on-check-all="handleCheckTableAll"
                  empty-text="No data"
                  ref="table">
          <bc-table-column field="topicName" index="topicName" label="Topic">
          </bc-table-column>
          <bc-table-column field="groupName" index="groupName" label="Consumer Group">
          </bc-table-column>
          <bc-table-column field="consumeType" index="consumeType" label="Consume Type">
            <template slot-scope="scope">
              <div>{{ dictTranslate('topic','consumeTypes', parseInt(scope.record.consumeType, 10))}}
              </div>
            </template>
          </bc-table-column>
          <bc-table-column field="state" index="state" label="Status">
            <template slot-scope="scope">
              <span :class="coloringText('topic', 'states', parseInt(scope.record.state,10))">{{dictTranslate('topic','states', parseInt(scope.record.state, 10))}}</span>
            </template>
          </bc-table-column>
          <bc-table-column field="operate" index="operate" label="Operation" width="350px">
            <template slot-scope="scope">
              <a class="text-link"  @click="handleDetail(scope.record)">Detail</a>
              <a class="text-link"  @click="handleEditSubscribe(scope.record)">Edit</a>
              <a class="text-link"  @click="handleGetMessage(scope.record)">Sampling</a>
              <router-link :class="['text-link']" :to="{
                          name: 'consume',
                          params: {
                            id: scope.record.groupId,
                            name: scope.record.groupName,
                          },
                          query: {
                            topicId: scope.record.topicId,
                            topicName: scope.record.topicName,
                          },}">Consume Progress
              </router-link>
            </template>
          </bc-table-column>
        </bc-table>
      </div>

      <div class="bcui-table-toolbar clearfix">
        <div class="bcui-table-toolbar__item pull-right">
          <bc-pagination v-if="subscribeList.length!==0"
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

    <bc-drawer
      v-model="detailStatus"
      title="Subscription Detail"
      @on-cancel="cancel">
      <detail :basicInfo=subscribeData></detail>
    </bc-drawer>

    <create-modal v-model="createSubscribeModalStatus" :subscribe="subscribeEditData"></create-modal>
    <ManageSubModal
      v-model="manageSubModalStatus"
      :type="manageType"
      :resources="checkedTableItems"></ManageSubModal>

    <bc-drawer
      v-model="messageStatus"
      v-if="messageStatus"
      @on-cancel="cancel">
      <messageDetail :topicId=topicId></messageDetail>
    </bc-drawer>
  </div>
</template>
<script>

  import groupMixins from '../../mixins/apis/subscribe.js';
  import commonMixins from '../../mixins/common.js';
  import tableMixins from '../../mixins/table.js';
  import detail from './modals/detail.vue';
  import CreateModal from './modals/create.vue';
  import ManageSubModal from './modals/manage.vue';
  import messageDetail from '../topic/modals/message.vue';

  export default {
    name: 'groups',
    mixins: [groupMixins, commonMixins, tableMixins],
    components: { detail, CreateModal, ManageSubModal, messageDetail },

    data () {
      return {
        subscribeList: [],
        loading: true,
        topicId: '',

        groups: [],

        pagination: {
          totalNum: 0,
          curPage: 1,
          pageSize: 10
        },

        filter: {
          isSearchShow: true,
          text: '',
          groupId: '',
          consumeType: '',
          state: '',
          groupName: '',
          clusterName: ''
        },

        groupQueryLoading: false,
        clusterQueryLoading: false,
        filterableGroups: [],

        title: '',
        subscribeData: {},
        subscribeEditData: {},
        subscribeEnableData: {},
        detailStatus: false,
        createSubscribeModalStatus: false,
        manageSubModalStatus: false,
        manageType: '',
        msgPushTypes: [],
        messageStatus: false
      };
    },

    computed: {
      hasNoChecked () {
        return this.checkedTableItems.length <= 0;
      }
    },

    methods: {
      handleGetMessage (data) {
        this.messageStatus = true;
        this.title = 'Sampling' + '( Topic:' + data.topicName + ')';
        this.topicId = data.topicId;
      },

      handleManageSubscribe (type) {
        this.manageType = type;
        this.manageSubModalStatus = true;
      },

      handleDetail (subscribe) {
        this.detailStatus = true;
        this.title = 'Subscription Detail';
        subscribe.msgPushTypeDesc = this.getNameById(subscribe.msgPushType, this.msgPushTypes);
        this.subscribeData = subscribe;
      },

      handleEditSubscribe (subscribe) {
        this.subscribeEditData = subscribe;
        this.createSubscribeModalStatus = true;
      },

      handleCreateSubscribe () {
        this.subscribeEditData = {};
        this.createSubscribeModalStatus = true;
      },

      cancel () {
        this.detailStatus = false;
      },

      handelSearch () {
        let { filter } = this;

        if (filter.text !== '') {
          clearTimeout(this.timer);

          this.timer = setTimeout(() => {
            let searchParams = {
              text: filter.text,
              clusterId: filter.clusterId,
              clusterName: 'ddmq',
              groupId: filter.groupId,
              groupName: this.getNameById(filter.groupId, this.groups),
              consumeType: filter.consumeType,
              state: filter.state,
              isSearchShow: filter.isSearchShow
            };
            localStorage.setItem(
              'subscribes_search_keyword',
              JSON.stringify(searchParams)
            );
            this.getSubscribeList();
          }, 500);
        } else {
          this.getSubscribeList();
          localStorage.setItem(
            'subscribes_search_keyword',
            ''
          );
        }
      },

      // 获取搜索group数据
      filterGroups (query) {
        let source = this.groups;
        if (query !== '') {
          this.groupQueryLoading = true;
          setTimeout(() => {
            this.groupQueryLoading = false;

            this.filterableGroups = source
              .filter((item) => {
                return item.label.toLowerCase().indexOf(query.toLowerCase()) > -1;
              })
              .slice(0, 99);
          }, 100);
        } else {
          this.filterableGroups = [];
        }
      },

      showSearch () {
        this.filter.isSearchShow = !this.filter.isSearchShow;
      },

      getConsumeState (data) {
        let groupId = data.groupId;
        let groupName = data.groupName;
        this.$router.push(`/group/consume/${groupId}/${groupName}`);
      },

      getMessages (topicId, clusterId) {
        this.$router.push(`/message/${topicId}/${clusterId}`);
      },

      getSubscribeList (params) {
        let { pagination, filter } = this;
        params = Object.assign(
          {},
          {
            curPage: pagination.curPage,
            pageSize: pagination.pageSize,
            text: filter.text,
            clusterId: 1,
            groupId: filter.groupId,
            consumeType: filter.consumeType,
            state: filter.state,
            user: 'administration'
          },
          params
        );

        this.loading = true;
        this.fetchSubscribeList({ params })
          .then((body) => {
            this.subscribeList = body.data.list;
            this.subscribeList.forEach((element) => {
              let clusterNames = Object.keys(element.clusters);
              this.$set(element, 'clusterId', element.clusters[clusterNames[0]]);
              this.$set(element, 'clusterName', clusterNames[0]);
            });
            this.pagination.totalNum = body.data.totalSize;
            this.loading = false;
          })
          .catch(() => {
            this.loading = false;
        });
      },

      // 更改页码
      refreshWhenPageChange (curPage) {
        this.pagination.curPage = curPage;
        this.getSubscribeList();
      },

      // 更改页数
      refreshWhenPageSizeChange (pageSize) {
        this.pagination.pageSize = pageSize;
        this.getSubscribeList();
      },

      // 获取消费组下拉框数据
      getGroups () {
        this.fetchGroupsWithOutPage({ params: { user: 'administration' } })
          .then((result) => {
            this.groupList = result.data;
            this.groups = this.groupList.map((item) => {
              return {
                label: item.groupName,
                value: item.groupId
              };
            });
            if (this.filter.groupId) {
              this.filter.groupName = this.getNameById(
                this.filter.groupId,
                this.groups
              );
            }
        });
      },

      initSearchParams () {
        let param = localStorage.getItem('subscribes_search_keyword');

        if (!param) {
          this.getSubscribeList();
          return;
        }

        param = JSON.parse(param);

        Object.keys(this.filter).forEach((item) => {
          let val = param[item];
          if (val !== undefined) {
            this.filter[item] = val;
          }
        });
        this.getSubscribeList();
      },

      // 获取消息推送方式列表
      getMsgPushTypes () {
        this.fetchMsgPushType({
          params: {
            user: 'administration'
          }
        }).then((result) => {
          let msgPushTypesList = result.data;
          this.msgPushTypes = msgPushTypesList.map((item) => {
            return {
              label: item.desc,
              value: item.id
            };
          });
        });
      },

      updateSubscribeList () {
        this.getSubscribeList();
        this.clearCheckTableRows();
      },

      init () {
        this.initSearchParams();
        this.getMsgPushTypes();
        this.getGroups();
        this.$root.bus.$on('updateSubscribeList', this.updateSubscribeList);
      },

      // 跳转topic编辑页面
      goTopicEdit (topicName) {
        this.$router.push(`/topics/create/${topicName}`);
      },

      // 新增订阅关系
      goToCreateSub () {
        this.$router.push(`/subscribe/create`);
      }
    },

    watch: {

    },

    mounted () {
      if (Object.keys(this.$route.query).length) {
        let { groupId } = this.$route.query;
        localStorage.setItem('subscribes_search_keyword', '');
        this.filter.groupId = groupId;
      }

      this.init();
    }
  };
</script>
