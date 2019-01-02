<style lang="less">
.bcui-table-toolbar__item-label {
  font-size: 14px;
  margin-right: 5px;
}

.reset-time {
  // float: right;
  width: 180px !important;
}

.flatpickr-wrapper {
  float: right;
  // margin-left: 9px;
}
.custom-date-picker {
  position: relative;
  float: right;

  .bcui-inputfield__input--disabled {
    background-color: #fff;
    color: #657180;
  }

  .clear,
  .trigger-icon {
    position: absolute;
    right: 10px;
    top: 9px;
    font-size: 16px;
    cursor: pointer;
  }

  .clear {
    color: #c7c7c7;
    right: 6px;
    top: 8px;
    font-size: 18px;
  }

  .trigger-icon {
    color: #657180;
  }
}
.table-icon-edit {
  font-size: 20px;
}
.table-icon {
  line-height: 1;
}
</style>

<template>
  <div class="content">
    <div class="detail-block">
        <div class="detail-block-nav clearfix">
          <div class="detail-block-nav__back" @click="goBack">
            <bc-icon type="chevron-left"></bc-icon><span>Back to list</span>
          </div>
        </div>

        <div class="detail-block-header">
          <div class="detail-info">
            <div class="detail-title">
              <div class="detail-title__value">consume progress ({{ 'Consume group:' + groupName}})</div>
            </div>
          </div>
          </div>
      </div>

      <div class="detail-block-body">
         <div class="bcui-table-toolbar">
          <div class="bcui-table-toolbar__item pull-left">
            <bc-button icon="refresh"
                       type="gray"
                       @click="handelSearch"></bc-button>
             <bc-button type="primary" @click="resetToNew()">
                Reset to Latest
             </bc-button>
             <bc-button type="primary" @click="resetToDate()">
                Reset By Time
             </bc-button>
            <div class="custom-date-picker reset-time">
                <input class="bcui-inputfield__input" :width='300' type="text" data-input readonly placeholder="Reset time">
                <span class="input-button" title="toggle" data-clear @click="clearTime('resetTime')">
                  <i class="clear bcui-icon-information-fail" v-if="filter.resetTime"> </i>
                </span>
                <span class="input-button" title="clear" data-toggle>
                  <i class="trigger-icon bcui-icon-calendar" v-if="!filter.resetTime"></i>
                </span>
              </div>

          </div>

          <div class="bcui-table-toolbar__item pull-right">
            <span class="bcui-table-toolbar__item-label">Topic</span>
            <bc-select v-model="filter.topicId" :width='200' placeholder="Please input topic">
              <bc-option v-for="item in topics" :value="item.value" :key="item.id">{{item.label}}</bc-option>
            </bc-select>
            <bc-input icon="search" :width="150" placeholder="search" v-model="filter.text" @on-change="handelSearch()"></bc-input>
          </div>
        </div>

        <bc-table v-loading="loading"
                  :tableData="renderData"
                  :class="['bcui-table--circular', 'bcui-table--dark']"
                  :border="false"
                  :rowEdit="true"
                  empty-text="No data"
                  :pagination="false">
          <bc-table-column field="topicName" index="topicName" label="Topic">
          </bc-table-column>
          <bc-table-column field="qid" index="qid" label="Qid">
          </bc-table-column>
          <bc-table-column field="minOffset" index="minOffset" label="MinOffset">
          </bc-table-column>
          <bc-table-column field="maxOffset" index="maxOffset" label="MaxOffset">
          </bc-table-column>
          <bc-table-column field="consumeOffset" index="consumeOffset" label="ConsumerOffset">
          </bc-table-column>
          <bc-table-column field="lag" index="lag" label="Lag">
          </bc-table-column>
          <bc-table-column field="reset" index="reset" label="Reset offset" width="200px">
            <template slot-scope="scope">
              <div class="table-icon">
                <a v-if="!scope.record.show" class="bcui-icon-pencil text-link table-icon-edit" @click="showInput(scope.record)"></a>
                <bc-input v-if="scope.record.show" class="table-input" v-model="scope.record.offset" type="number"></bc-input>
              </div>
            </template>
          </bc-table-column>
          <bc-table-column field="submit" index="submit" label="" width="150px">
            <template slot-scope="scope">
              <div class="table-icon">
                <a v-if="scope.record.show" :class="['text-link']" @click="submit(scope.record)">Submit</a>
                <a v-if="scope.record.show" :class="['text-link']" @click="cancel(scope.record)">Cancel</a>
              </div>
            </template>
          </bc-table-column>
        </bc-table>
      </div>
      <consume-modal
          v-model="manageConsumeStatus"
          :type="manageType"
          :params="manangeParams"
          :resource="resource">
      </consume-modal>
  </div>
</template>
<script>
  import groupMixins from '../../mixins/apis/group.js';
  import flatpickr from 'flatpickr';
  import commonMixins from '../../mixins/common.js';

  import consumeModal from './modals/consume-manage.vue';

  export default {
    name: 'group-consume',
    mixins: [groupMixins, commonMixins],
    components: { consumeModal },

    data () {
      return {
        manageConsumeStatus: false,
        manageType: '',
        resource: {},
        manangeParams: {},
        isTopicConsume: false,
        renderData: [],
        loading: true,
        topics: [],
        clusters: [],
        groupName: '',
        groupId: '',

        filter: {
          clusterId: '',
          topicId: '',
          resetTime: ''
        },

        clusterName: '',
        topicName: ''
      };
    },
    methods: {
      goBack () {
        if (this.isTopicConsume) {
          this.$router.push({
            name: 'subscribes'
          });
        } else {
          this.$router.push({
            name: 'groups'
          });
        }
      },

      showInput (data) {
        data.show = true;
      },

      cancel (data) {
        data.show = false;
      },

      // 获取初始界面消费列表
      getConsumeList (params) {
        let { filter } = this;
        params = Object.assign(
          {},
          {
            clusterId: 1,
            topicId: filter.topicId,
            groupId: this.groupId,
            user: 'administration'
          },
          params
        );

        this.topicName = this.getNameById(filter.topicId, this.topics);
        this.loading = true;
        this.getTopicConsumeState({ params })
          .then((body) => {
            this.renderData = body.data;
            this.renderData.forEach((item) => {
              this.filter.clusterId = this.filter.clusterId || item.clusterId;
              this.filter.topicId = this.filter.topicId || item.topicId;

              item.show = false;
            });

            this.loading = false;
          })
          .catch(() => {
            this.loading = false;
        });
      },

      handelSearch () {
        this.getConsumeList();
      },

      init () {
        this.getConsumeList();
        this.$root.bus.$on('updateConsumeList', this.getConsumeList)
      },

      submit (data) {
        if (!this.filter.topicId) {
          this.$notice.error({
            title: 'Please select topic'
          });
          return;
        }

        if (!data.offset) {
          this.$notice.error({
            title: 'Please input reset value'
          });
          return;
        }

        if (isNaN(data.offset) || data.offset < 0) {
          this.$notice.error({
            title: 'Reset value must be Number'
          });
          return;
        }

        let { filter } = this;
        this.manangeParams = {
          resetType: 3,
          qid: data.qid,
          offset: data.offset,
          clusterId: 1,
          topicId: filter.topicId,
          groupId: this.groupId }

        this.resource = data;
        this.manageConsumeStatus = true;
        this.manageType = 'resetValue';
      },

      // 重置到最新
      resetToNew (params) {
        if (!this.filter.topicId) {
          this.$notice.error({
            title: 'Please select topic'
          });
          return;
        }

        let { filter } = this;
        this.manangeParams = {
          resetType: 1,
          clusterId: 1,
          topicId: filter.topicId,
          groupId: this.groupId
        };
        this.topicName = this.getNameById(filter.topicId, this.topics);

        this.$set(this.resource, 'topicName', this.topicName);
        this.manageConsumeStatus = true;
        this.manageType = 'resetToNew';
      },

      resetToDate (params) {
        if (!this.filter.topicId) {
          this.$notice.error({
            title: 'Please select topic'
          });
          return;
        }
        if (!this.filter.resetTime) {
          this.$notice.error({
            title: 'Please select reset date'
          });
          return;
        }
        let { filter } = this;
        this.manangeParams = {
          resetType: 2,
          resetTime: filter.resetTime,
          clusterId: 1,
          topicId: filter.topicId,
          groupId: this.groupId
        };

        this.topicName = this.getNameById(filter.topicId, this.topics);
        this.$set(this.resource, 'topicName', this.topicName);
        this.manageConsumeStatus = true;
        this.manageType = 'resetToTime';
      },

      clearTime (key) {
        this.filter[key] = '';
      },

      fetchTopicsAndClusters (groupId) {
        let params = {
          groupId: groupId,
          user: 'administration'
        };
        this.getTopicsByGroupId({ params }).then((body) => {
          let data = body.data;
          this.clusters = data.cluster.map((item) => {
            return {
              label: item.desc,
              value: item.id
            };
          });
          this.topics = data.topic.map((item) => {
            return {
              label: item.desc,
              value: item.id
            };
          });

          // 获取topic名
          for (let topic of this.topics) {
            if (topic.value === this.filter.topicId) {
              this.topicName = topic.label;
            }
          }
        });
      }
    },
    mounted () {
      let { id, name } = this.$route.params;
      this.groupName = name;
      this.groupId = id;

      // // 从订阅关系列表跳转过来
      if (Object.keys(this.$route.query).length) {
        let { topicId } = this.$route.query;
        this.isTopicConsume = true;
        this.filter.topicId = topicId;
        this.filter.clusterId = 1;
      }

      this.fetchTopicsAndClusters(this.groupId);

      flatpickr('.reset-time', {
        static: true,
        time_24hr: true,
        wrap: true,
        enableTime: true,
        enableSeconds: true,
        dateFormat: 'Y-m-d H:i:S',
        onClose: (selected, dateStr, instance) => {
          this.filter.resetTime = dateStr;
        }
      });

      this.init();
    }
  };
</script>
