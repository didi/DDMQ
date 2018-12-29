<style lang="less">
.form-icon-operate {
  font-size: 22px;
  vertical-align: middle;
  color: #2d77ee;
  cursor: pointer;
  margin-left: 8px;
}
.form-icon-operate:hover {
  color: #2d77ee;
}
.conf-margin {
  margin-bottom: 5px;
}

</style>

<template>

  <bc-modal v-model="show"
            v-if="show"
            :title="title"
            :offsetTop="40"
            :width="610"
            class="create-topic-modal"
            @on-cancel="cancel">
    <div class="topic-create">
      <bc-form class="clearfix"
               ref="form"
               :width="540"
               :model="form"
               :rules="topicFormRule"
               labelAlign="top">

        <bc-form-item label="Topic" prop="topicName">
          <bc-input v-model="form.topicName"
                    :disabled='isEditing'
                    placeholder="Please input Topic name"></bc-input>
        </bc-form-item>
        <div class="alert-message">
          <div>1. The topic name can contain only letters, numbers, underscore(_)and dashes(-)</div>
          <div>2. Once created can't modify the Topic name</div>
        </div>
        <bc-form-item label="Delay Topic " prop="delayTopic">
          <bc-select placehold="Please select" v-model.number="form.delayTopic">
            <bc-option v-for="item in dict.topic.delayStates"
                :value=item.value
                :label=item.label
                :key="item.label"></bc-option>
              </bc-select>
        </bc-form-item>

        <bc-form-item label="Topic Description" prop="description">
          <bc-input v-model="form.description" type="textarea" :rows="4" placeholder="topic description information."></bc-input>
        </bc-form-item>

        <bc-form-item label="Rate Limit" prop="produceTps">
          <bc-input type="text" v-model="form.produceTps">
            <span slot="append">msg/s</span>
          </bc-input>
        </bc-form-item>

        <bc-form-item label="Additional Parameters">
          <div class="conf-margin" v-for="(item,i) in others" :key="i">
            <bc-input v-model="item.key" placeholder="key" style="width:240px;margin-right: 10px;"></bc-input>
            <bc-input v-model="item.value" placeholder="value" style="width:240px;"></bc-input>
            <span class="form-icon-operate" @click="removeOther(item)">
               <bc-icon type="trash" v-if="others.length>1"></bc-icon>
            </span>
          </div>
          <div>
            <span class="text-link" @click="addOther()">Add additional parameters</span>
          </div>
        </bc-form-item>
      </bc-form>
    </div>
    <template slot="footer">
        <bc-button @click="cancel">Cancel</bc-button>
        <bc-button type="primary" @click="handleCreateTopic" :loading="isSubmitting">Submit</bc-button>
    </template>
  </bc-modal>
</template>

<script>
  import topicMixins from '../../../mixins/apis/topic.js';
  import commonMixins from '../../../mixins/common.js';
  import { createTopic } from '../../../schemas/topic.js';
  import CONSTANT from '../../../mixins/constant.js';
  export default {
    name: 'create-topic-modal',
    mixins: [topicMixins, commonMixins],
    components: { },

    props: {
      value: {
        type: Boolean,
        default: false
      },

      topic: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data () {
      return {
        title: 'Create Topic',
        isSubmitting: false,

        isEditing: false,

        others: [{ key: '', value: '' }],
        form: {
          topicId: '',
          topicName: '',
          remark: '',
          produceMode: 0,
          conf: [],
          delayTopic: CONSTANT.TOPIC_DELAY_DISABLE,
          enableSchemaVerify: false,
          produceTps: 1024
        },
        topicFormRule: createTopic,

        topicDesc: {},
        showConfOperationParams: false
      };
    },
    computed: {
      show: {
        get () {
          if (this.value && Object.keys(this.topic).length) {
            this.isEditing = true;
            this.title = 'Edit topic';

            this.$nextTick(() => {
              this.form = JSON.parse(JSON.stringify(this.topic));
              this.form.produceTps = this.topic.conf[0].produceTps;
              this.form.delayTopic = this.topic.delayTopic;
            });
            this.others = this.formatObject(this.topic.extraParams);
          }
          return this.value;
        },
        set () {}
      }
    },
    methods: {
      cancel () {
        this.isSubmitting = false;
        this.isEditing = false;
        this.title = 'Create Topic';
        this.others = [{ key: '', value: '' }];
        this.$refs['form'].resetFields();
        this.$emit('input', false);
      },

      // 增加额外参数
      addOther () {
        this.others.push({
          key: '',
          value: ''
        });
      },

      // 删除配置
      removeOther (item) {
        const index = this.others.indexOf(item);
        if (index !== -1) {
          this.others.splice(index, 1);
        }
      },

      // 确定新建topic
      handleCreateTopic () {
        let { form } = this;
        let isTopicValid = this.validateForm('form');
        if (!isTopicValid) {
          this.isSaveLoading = false;
          this.$notice.error({
            title: 'Please checkout form',
            duration: 0
          });
          return;
        }

        this.isSubmitting = true;
        let body = {
          user: 'administration',
          topicId: this.isEditing ? form.topicId : 0, // 编辑topic时，或者属于编辑订阅类型的工单在编辑时，需要传入topicId，新建传入0
          topicName: form.topicName,
          service: 'Engineering',
          department: 'Software',
          contacters: 'administration;',
          schema: null,
          alarmGroup: null,
          description: form.description,
          extraParams: {},
          delayTopic: form.delayTopic,
          needAuditSubinfo: 1,
          produceMode: 0,
          conf: [],
          enableSchemaVerify: 1,
          operationParams: null
        };
        body.extraParams = this.formatMap(this.others);
        body.conf = [ {
          clusterId: 1,
          clusterName: 'ddmq',
          clientIdcMap: null,
          clusterDesc: null,
          serverIdcId: 1,
          serverIdcName: 'default',
          operationParams: null,
          msgMaxSize: 1024,
          msgAvgSize: 1024,
          produceTps: form.produceTps,
          mqServerId: 1,
          mqServerName: 'R_default'
        }];

        this.createTopic(body)
          .then((body) => {
            if (body.errno === 0) {
              this.$root.bus.$emit('updateTopicList');
              this.cancel();
            }
          }).finally(() => {
            this.isSaveLoading = false;
            this.isSubmitting = false;
        });
      },

      init () {
        // this.getTopicDesc();
      },

      // 编辑页面时，获取页面初始字段值
      getEditInitData (data) {
        this.form = data;
      }

    },
    watch: {
    },

    mounted () {
      this.init();
    }
  };
</script>
