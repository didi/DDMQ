
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
            <div class="detail-title__value">{{basicInfo.topicName}}</div>
          </div>
        </div>

        <div class="detail-functions">
          <bc-button type="primary" @click="handleGetMessage(basicInfo)">Sampling</bc-button>
        </div>
      </div>

      <div class="detail-block-body">
        <bc-tab v-model="currentTab" @on-tab-click="clickTabs">
          <bc-tab-pane name="info" label="Detail" >
            <bc-row :gutter="48" v-if="Object.keys(basicInfo).length">
              <bc-col span="6">
                <div class="detail-info-prop">
                  <div class="title">Basic Information</div>
                  <div class="prop-item">
                    <div class="prop-key">Topic</div>
                    <div class="prop-value">{{basicInfo.topicName}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">DelayTopic</div>
                    <div class="prop-value">{{basicInfo.delayTopic ? 'false': 'true'}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">Cluster</div>
                    <div class="prop-value">{{basicInfo.conf[0].clusterName}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">Rate Limit TPS</div>
                    <div class="prop-value">{{basicInfo.conf[0].produceTps}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">msgMaxSize</div>
                    <div class="prop-value">{{basicInfo.conf[0].msgMaxSize}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">msgAvgSize</div>
                    <div class="prop-value">{{basicInfo.conf[0].msgAvgSize}}</div>
                  </div>
                  <div class="prop-item">
                    <div class="prop-key">Description</div>
                    <div class="prop-value">{{basicInfo.description}}</div>
                  </div>
                </div>
              </bc-col>
              <bc-col span="12">
                <div class="detail-info-prop">
                  <div class="title">Additional Parameters</div>
                  <div class="prop-item" v-for="(value, key) in basicInfo.extraParams" :key="key">
                    <div class="prop-key">{{ key }}</div>
                    <div class="prop-value">{{value}}</div>
                  </div>
                </div>
              </bc-col>
            </bc-row>
          </bc-tab-pane>
          <bc-tab-pane name="group" label="Consumer Group"></bc-tab-pane>
          <bc-tab-pane name="statistic" label="Statistics"></bc-tab-pane>
        </bc-tab>
        <router-view></router-view>
      </div>
    </div>
<bc-drawer
    v-model="messageStatus"
    v-if="messageStatus"
    @on-cancel="cancel">
    <messageDetail :topicId=topicId></messageDetail>
  </bc-drawer>
  </div>
</template>

  <script>

  import topicMixin from '../../mixins/apis/topic.js';
  import commonMixin from '../../mixins/common.js';
  import messageDetail from './modals/message.vue';
  export default {
    name: 'topic-detail',
    mixins: [ topicMixin, commonMixin ],
    components: { messageDetail },
    data () {
      return {
        basicInfo: {},
        currentTab: 'info',
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

      clickTabs (flag) {
        let id = this.$route.params.id;
        if (flag !== 'info') {
          this.$router.push({
            path: `/topics/${id}/${flag}`
          });
        } else {
          this.$router.push({
            path: `/topics/${id}`
          });
        }
      },

      getTopicDetail (id) {
        let params = {
          topicId: id,
          user: 'administration'
        };
        this.fetchTopicDetail({ params }).then((body) => {
          this.basicInfo = body.data;
        });
      },

      init () {
        let topicId = this.$route.params.id;
        this.getTopicDetail(topicId);
      },

      goBack () {
        this.$router.push({
          name: 'topics'
        });
      }

    },

    mounted () {
      this.init();
    }
  };
  </script>
