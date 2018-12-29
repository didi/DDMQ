<style lang="less">
</style>

<template>
  <div class="container group-index">
    <div class="block">
      <div class="block-header">
        <div class="block-header__title">Consumer Group List</div>
      </div>

      <div class="block-body">
        <div class="bcui-table-toolbar">
          <div class="bcui-table-toolbar__item pull-left">
            <bc-button icon="refresh"
                        type="gray"
                        @click="getGroupList"></bc-button>
            <bc-button icon="plus"
                        type="primary"
                        @click="handleCreateGroup">Consumer Group
            </bc-button>
            <bc-button-group>
              <bc-button type="gray"
                          :disabled="hasNoChecked"
                          @click="handleManageGroup('enable')">Enable
              </bc-button>
              <bc-button type="gray"
                          :disabled="hasNoChecked"
                          @click="handleManageGroup('disable')">Disable
              </bc-button>
            </bc-button-group>

            <bc-button type="gray" :disabled="hasNoChecked" @click="handleManageGroup('delete')">Delete
            </bc-button>

          </div>

          <div class="bcui-table-toolbar__item pull-right">
            <bc-input icon="search" :width="200" placeholder="search" v-model="filter.text" @on-change="handelSearch()"></bc-input>
          </div>
        </div>

        <bc-table :tableData="groupList"
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
            <bc-table-column field="groupName" index="groupName" label="Consumer Group">
            </bc-table-column>
            <bc-table-column field="subscriptionNum" index="subscriptionNum" label="Subscription Number">
            </bc-table-column>
            <bc-table-column field="subscriptionEnableNum" index="subscriptionEnableNum" label="Enabled Subscription Number">
            </bc-table-column>

            <bc-table-column field="operate" index="operate" label="Operation" width="430px">
              <template slot-scope="scope">
                <router-link class="text-link" :to="{ name: 'subscribes', query: { groupId: scope.record.groupId} }">Subscriptions</router-link>
                <a :class="['text-link', scope.record.subscriptionNum ? '':'disable-text-link']" href="" @click.prevent="handleConsume(scope.record)">Consume Progress</a>
                <a :class="['text-link']" @click="handleEditGroup(scope.record)">Edit Consumer Group</a>
              </template>
            </bc-table-column>
        </bc-table>
      </div>

      <div class="bcui-table-toolbar clearfix">
        <div class="bcui-table-toolbar__item pull-right">
          <bc-pagination v-if="groupList.length!==0"
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
    <CreateGroupModal v-model="createGroupModalStatus" :group=groupEditData></CreateGroupModal>

    <ManageGroupModal
      v-model="manageSubModalStatus"
      :type="manageType"
      :resources="checkedTableItems"></ManageGroupModal>
  </div>
</template>

<script>
  import groupMixins from '../../mixins/apis/group.js';
  import commonMixins from '../../mixins/common.js';
  import tableMixins from '../../mixins/table.js';
  import CreateGroupModal from './modals/create.vue';
  import ManageGroupModal from './modals/manage.vue';

  export default {
    name: 'groups',
    mixins: [groupMixins, commonMixins, tableMixins],
    components: { CreateGroupModal, ManageGroupModal },

    data () {
      return {
        groupList: [],
        groupIds: [],

        loading: true,

        pagination: {
          totalNum: 0,
          curPage: 1,
          pageSize: 10
        },

        filter: {
          text: ''
        },

        createGroupModalStatus: false,
        detailStatus: false,
        groupId: 0,
        groupName: '',
        title: '',
        groupData: {},
        groupEditData: {},
        manageType: '',
        manageSubModalStatus: false
      };
    },

    computed: {
      hasNoChecked () {
        return this.checkedTableItems.length <= 0;
      }
    },

    methods: {

      handleManageGroup (type) {
        this.manageType = type;
        this.manageSubModalStatus = true;
      },

      handelDetail (group) {
        this.detailStatus = true;
        this.title = group.groupName;
        this.groupData = group;
      },

      handleEditGroup (group) {
        this.createGroupModalStatus = true;
        this.groupEditData = group;
      },

      handleCreateGroup () {
        this.createGroupModalStatus = true;
        this.groupEditData = {};
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
              text: filter.text
            };
            localStorage.setItem(
              'group_search_keyword',
              JSON.stringify(searchParams)
            );
            this.getGroupList();
          }, 500);
        } else {
          this.getGroupList();
          localStorage.setItem(
            'group_search_keyword', ''
          );
        }
      },

      addSubscribe (data) {
        let groupdata = {
          groupId: data.groupId,
          groupName: data.groupName
        };
        this.$router.push({
          name: 'group-sub-create',
          query: {
            groupdata: JSON.stringify(groupdata)
          }
        });
      },

      handleConsume (data) {
        let id = data.groupId;
        let name = data.groupName;
        this.$router.push(`/group/${id}/${name}`);
      },

      // 获取初始界面Groups列表
      getGroupList (params) {
        let { pagination } = this;
        params = Object.assign(
          {},
          {
            curPage: pagination.curPage,
            pageSize: pagination.pageSize,
            text: this.filter.text,
            user: 'administration'
          },
          params
        );
        this.loading = true;
        this.fectchGroupList({ params })
          .then(body => {
            this.groupList = body.data.list;
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
        this.getGroupList();
      },

      // 更改页数
      refreshWhenPageSizeChange (pageSize) {
        this.pagination.pageSize = pageSize;
        this.getGroupList();
      },

      initSearchParam () {
        let param = localStorage.getItem('group_search_keyword');

        if (!param) {
          this.getGroupList();
          return;
        }

        this.keyword = param;
        this.getGroupList();
      },

      updateGroupList () {
        this.getGroupList();
        this.clearCheckTableRows();
      },

      init () {
        this.initSearchParam();
        this.$root.bus.$on('updateGroupList', this.updateGroupList);
      }

    },

    watch: {

    },

    mounted () {
      this.init();
    }

  }
</script>
