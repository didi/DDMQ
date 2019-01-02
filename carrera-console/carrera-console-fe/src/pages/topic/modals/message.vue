<style lang="less">
.cm-s-base16-light.CodeMirror {
  width: 100%,
}
.samplingPage {

  .detail-info-prop {
    margin-top: 0px;

  }
  .detail-block-header {
    padding: 20px 0px;
    font-weight: 600;
    margin: 20px 0px;

     .detail-title {
      display: inline-block;
      font-size:24px;
    }

    .detail-button {
      float: right;
      .bcui-button {
        border-radius: 4px;
        font-size: 14px;
      }
    }
  }
}
</style>

<template>
  <div class="content samplingPage">
    <div class="detail-block">
      <div class="detail-block-header">
        <div class="detail-title">Sampling</div>
        <div class="detail-button">
          <bc-button type="primary" @click="handleResampling()" :loading=loading>Resampling</bc-button>
          <bc-button type="primary" @click="handleCopyMessage()">Copy MessageBody</bc-button>
        </div>
      </div>

      <div class="detail-block-body">
        <bc-row :gutter="48">
          <bc-col span="24">
            <div class="detail-info-prop">
              <div class="prop-item">
                <div class="prop-key">Topic</div>
                <div class="prop-value">{{basicInfo.topicName}}</div>
              </div>
              <div class="prop-item">
                <div class="prop-key">Qid</div>
                <div class="prop-value">{{basicInfo.qid || '-'}}</div>
              </div>
              <div class="prop-item">
                <div class="prop-key">Offset</div>
                <div class="prop-value">{{basicInfo.offset}}</div>
              </div>
              <div class="prop-item">
                <div class="prop-key">MessageBody</div>
                 <codemirror v-model="basicInfo.msg" :options="editorOptions" disabled></codemirror>
              </div>
            </div>
          </bc-col>
        </bc-row>
      </div>
    </div>
  </div>
</template>

<script>

  import topicMixin from '../../../mixins/apis/topic.js';
  import commonMixin from '../../../mixins/common.js';
  import SendMessageModal from './send-message.vue';

  export default {
    name: 'topic-sampling',
    mixins: [ topicMixin, commonMixin ],
    components: { SendMessageModal },

    props: {
      topicId: {
        type: Number,
        default: 0
      }

    },

    data () {
      return {
        basicInfo: {},
        editorOptions: {
          tabSize: 4,
          mode: 'text/javascript',
          theme: 'base16-light',
          lineNumbers: true,
          line: true,
          readOnly: true,
          lineWrapping: true
        },
        loading: false,
        sendmessageModalStatus: false,
        isSubscribe: false
      };
    },

    methods: {
      handleSendMessage () {
        this.sendmessageModalStatus = true;
      },

      handleResampling () {
        this.loading = true;
        this.getSampling('reSampling');
      },

      handleCopyMessage () {
        this.$clipboard(this.basicInfo.msg);
        this.$notice.success({
          title: '复制成功'
        });
      },

      getSampling (reSampling) {
        let { topicId } = this;
        let params = {
          topicId,
          clusterId: 1,
          user: 'administration'
        };
        this.fectchMessages({ params }).then((body) => {
          this.basicInfo = body.data;
          if (reSampling && body.errno === 0) {
            this.$notice.success({
              title: 'Resampling success'
            });
          }
        }).finally(() => {
          this.loading = false;
        });
      },

      init () {
        this.getSampling();

        if (Object.keys(this.$route.query)) {
          this.isSubscribe = true;
        }
      },

      goBack () {
        if (this.isSubscribe) {
          this.$router.push({
            name: 'subscribes'
          });
        } else {
          this.$router.push({
            name: 'topics'
          });
        }
      }

    },

    mounted () {
      this.init();
    }
  };
</script>
