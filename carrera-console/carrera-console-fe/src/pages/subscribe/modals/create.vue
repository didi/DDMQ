<style lang="less">
.subscribe-create {
  height: 100%;
  .icon-for-advanced-config {
    cursor: pointer;
  }
}
.config-form-item {
  display: flex;
  justify-content: space-between;
}
.title-default + .config-form-item {
  margin-top: 12px;
}
.title-default {
  font-size: 16px;
  font-weight: 600;
  color: #424242;
}
.subscribe-create {
  .bcui-radio-group {
    width: 265px;
  }

  .bc-codemirror {
    position: relative;
  }
  .bcui-tooltip {
    position: absolute;
    top:7px;
    z-index: 100;
    left: -15px;
  }
  .config-warnning {
    font-size: 16px;
    color: red;
  }
}

.config-result {
    width: 360px;
    height: 100%;
    padding: 20px;
    background: #fbfbfc;
    border: 1px solid #f2f3f5;
    border-radius: 4px;

    .title {
      margin-bottom: 20px;
    }

    .config-details {
      margin-bottom: 40px;
    }

    .detail-item {
      display: flex;
      margin: 8px 0;
    }

    .detail-item-key {
      width: 55%;
      font-size: 14px;
      color: #333333;
    }
     .detail-item-value {
      font-size: 14px;
      color: #666666;

     }

    .quota-item {
      margin-bottom: 16px;
    }

    .quota-item-info {
      display: flex;
      justify-content: space-between;
      font-size: 14px;
      color: #666666;
    }
  }

</style>

<template>
<bc-modal v-model="show"
          v-if="show"
          :title="title"
          :offsetTop="40"
          :width="1000"
          class="create-group-modal"
          @on-cancel="cancel">
  <div class="subscribe-create config-form-item">
    <div class="config-form">
    <bc-form  class="clearfix"
              v-if="show"
               ref="form"
               :width="540"
               :model="form"
               :rules="formRule"
               labelAlign="top">
      <div class="title-default">Basic Config</div>
      <div class="config-form-item">
        <bc-form-item label="Consumer Group："
                      prop="groupId">
          <bc-input v-if="isEditing" disabled :width="265" v-model="form.groupName"></bc-input>
          <bc-select  v-if="!isEditing"
                      remote
                      filterable
                      :width="265"
                      :remote-method="filterGroups"
                      :label="form.groupName"
                      v-model="form.groupId"
                      :loading="groupQueryLoading"
                      placeholder="Please input keywords to select">
            <template v-if="!groupQueryLoading">
              <bc-option v-for="item in filterableGroups"
                        :value="item.value"
                        :key="item.value">{{ item.label }}</bc-option>
            </template>
          </bc-select>
        </bc-form-item>
        <bc-form-item label="Topic："
                      prop="topicId">
          <bc-input v-if="isEditing" disabled :width="265" v-model="form.topicName"></bc-input>
          <bc-select  v-if="!isEditing"
                      remote
                      filterable
                      :width="265"
                      :remote-method="filterTopics"
                      :label="form.topicName"
                      v-model="form.topicId"
                      :loading="loading"
                      placeholder="Please input keywords to select">
            <template v-if="!loading">
              <bc-option v-for="item in filterableTopics"
                        :value="item.value"
                        :key="item.value">{{ item.label }}</bc-option>
            </template>
          </bc-select>
        </bc-form-item>
      </div>

      <div class="config-form-item">
        <bc-form-item label="Consume maxTps"
                      prop="maxTps">
          <bc-input v-model="form.maxTps" type="number" :width="265"></bc-input>
        </bc-form-item>

        <bc-form-item label="Receive Pressure Traffic"
                      prop="pressureTraffic">
          <bc-select placehold="Please select" :width="265" v-model.number="form.pressureTraffic">
            <bc-option v-for="item in dict.topic.pressureTrafficStates"
                      :value=item.value
                      :label=item.label
                      :key="item.label"></bc-option>
          </bc-select>

        </bc-form-item>
      </div>

      <div class="title-default" style="margin-bottom: 15px;">Advanced Config
        <span @click="toggleAdvancedConfig"
              class="icon-for-advanced-config">
              <bc-icon :type="iconForAdvancedConfig"></bc-icon>
        </span>
      </div>

      <div v-show="isShowAdvancedConfig">

        <bc-form-item label="API Level"
                      prop="apiType">
          <bc-select placehold="Please select" v-model.number="form.apiType">
            <bc-option v-for="item in dict.topic.apiTypes"
                      :value=item.value
                      :label=item.label
                      :key="item.label"></bc-option>
          </bc-select>
        </bc-form-item>

        <template v-if="(parseInt(form.apiType,10)===1)">
          <div class="config-form-item">
            <bc-form-item label="Consume Timeout"
                          prop="consumeTimeout">
              <bc-input v-model="form.consumeTimeout"
                        :width="265"
                        type="number"
                        placeholder="default 1000ms">
                <span slot="append">ms</span>
              </bc-input>
            </bc-form-item>

            <bc-form-item label="Error Retry Times"
                          prop="errorRetryTimes">
              <bc-input v-model="form.errorRetryTimes"
                        :width="265"
                        type="number"
                        placeholder="default 3"></bc-input>
            </bc-form-item>
          </div>

          <div class="config-form-item">
          <bc-form-item label="Retry Intervals"
                        prop="retryIntervals">
            <bc-input v-model="form.retryIntervals"
                      :width="265"
                      placeholder="integer,separate by ;">
              <span slot="append">ms</span>
            </bc-input>
          </bc-form-item>

          <bc-form-item label="Message Type"
                        prop="msgType">
            <bc-select placehold="Please select" :width="265" v-model.number="form.msgType">
              <bc-option v-for="item in dict.topic.msgTypes"
                    :value=item.value
                    :label=item.label
                    :key="item.label"></bc-option>
            </bc-select>
          </bc-form-item>
          </div>

          <template v-if="(parseInt(form.msgType,10)===1)">
            <div class="config-form-item">
              <bc-form-item label="Enable groovyScript"
                            prop="enableGroovy">
                <bc-select placehold="Please select" :width="265" v-model.number="form.enableGroovy">
                  <bc-option v-for="item in dict.topic.groovyStates"
                    :value=item.value
                    :label=item.label
                    :key="item.label"></bc-option>
                </bc-select>
              </bc-form-item>

              <bc-form-item label="Enable Transit"
                            prop="enableTransit">
                <bc-select placehold="Please select" :width="265" v-model.number="form.enableTransit">
                  <bc-option v-for="item in dict.topic.transitStates"
                    :value=item.value
                    :label=item.label
                    :key="item.label"></bc-option>
                </bc-select>
              </bc-form-item>
            </div>

            <template v-if="(form.enableGroovy===0)">
              <bc-form-item label="GroovyScript">
                <bc-input v-model="form.groovy"
                          type="textarea"
                          :rows="4"
                          placeholder=""></bc-input>
              </bc-form-item>
            </template>

            <template v-if="(form.enableTransit===0)">
              <bc-form-item label="Transit">
                <div class="conf-margin"
                      v-for="(item,i) in transits"
                      :key="i">
                  <bc-input v-model="item.key"
                            placeholder="key"
                            style="width:227px;margin-right: 10px;"></bc-input>
                  <bc-input v-model="item.value"
                            placeholder="value"
                            :width="227"></bc-input>
                  <span class="form-icon-operate" @click="addConf(transits)">
                    <bc-icon type="jia"></bc-icon>
                  </span>
                  <span class="form-icon-operate" @click="removeConf(item,transits)">
                    <bc-icon type="jian" v-if="transits.length>1"></bc-icon>
                  </span>
                </div>
              </bc-form-item>
            </template>
          </template>

          <div class="config-form-item">
            <bc-form-item label="Enable Sequential Consume"
                          prop="enableOrder">
              <bc-select placehold="Please select" :width="265" v-model.number="form.enableOrder">
                <bc-option v-for="item in dict.topic.orderStates"
                    :value=item.value
                    :label=item.label
                    :key="item.label"></bc-option>
              </bc-select>

            </bc-form-item>

            <bc-form-item label="Consume type"
                          prop="consumeType">
                <bc-select placehold="Please select" :width="265" v-model.number="form.consumeType">
                  <bc-option v-for="item in dict.topic.consumeTypes"
                    :value=item.value
                    :label=item.label
                    :key="item.label"></bc-option>
                </bc-select>

            </bc-form-item>
          </div>

          <template v-if="(form.enableOrder===0)">
            <bc-form-item label="Basis ordering rules"
                          prop="orderKey">
              <bc-select placehold="Please select" v-model="form.orderKey">
                <bc-option v-for="item in dict.topic.orderKeyTypes"
                          :value=item.value
                          :label=item.label
                          :key="item.label"></bc-option>
              </bc-select>

            </bc-form-item>

            <template v-if="form.orderKey=='JsonPath'">
              <bc-form-item label="JsonPath"
                            prop="jsonPath">
                <bc-input v-model="form.jsonPath"
                          placeholder="Please input JsonPath"></bc-input>
              </bc-form-item>
            </template>
          </template>

          <template v-if="(parseInt(form.consumeType,10)===2)">
            <div class="config-form-item">
              <bc-form-item label="HttpMethod"
                            prop="httpMethod">
                <bc-select placehold="Please select" :width='265' v-model.number="form.httpMethod">
                  <bc-option v-for="item in dict.topic.httpMethodTypes"
                      :value=item.value
                      :label=item.label
                      :key="item.label"></bc-option>
                </bc-select>

              </bc-form-item>
              <bc-form-item label="Message push type"
                            prop="msgPushType">
                <bc-select v-model="form.msgPushType"
                            placeholder="Please select"
                            :width='265'>
                  <bc-option v-for="(item,index) in msgPushTypes"
                              :value="item.value"
                              :label="item.label"
                              :key="index"></bc-option>
                </bc-select>
              </bc-form-item>
            </div>
            <div class="config-form-item">
              <bc-form-item label="Http token"
                            prop="httpToken">
                <bc-input v-model="form.httpToken" :width='265'></bc-input>
              </bc-form-item>

              <bc-form-item label="Push max concurrency"
                            prop="pushMaxConcurrency">
                <bc-input v-model="form.pushMaxConcurrency" :width='265'></bc-input>
              </bc-form-item>
            </div>
            <bc-form-item label="Urls："
                          prop="urls">
              <div class="conf-margin"
                    v-for="(item,index) in urls"
                    :key="index">
                <bc-form-item>
                  <bc-input v-model="item.key" width="87%" placeholder="Please input url"></bc-input>
                  <span class="form-icon-operate" @click="addUrl(urls)">
                    <bc-icon type="jia"></bc-icon>
                  </span>
                  <span class="form-icon-operate" @click="removeUrl(item,urls)">
                    <bc-icon type="jian" v-if="urls.length>1"></bc-icon>
                  </span>

                </bc-form-item>

              </div>
            </bc-form-item>
            <bc-form-item label="QueryParams">
              <div class="conf-margin"
                    v-for="(item,i) in queryParams"
                    :key="i">
                <bc-input v-model="item.key"
                          placeholder="key"
                          style="width:227px;margin-right: 10px;"></bc-input>
                <bc-input v-model="item.value"
                          placeholder="value"
                          :width="227"></bc-input>
                <span class="form-icon-operate" @click="addConf(queryParams)">
                  <bc-icon type="jia"></bc-icon>
                </span>
                <span class="form-icon-operate" @click="removeConf(item,queryParams)">
                  <bc-icon type="jian" v-if="queryParams.length>1"></bc-icon>
                </span>
              </div>
            </bc-form-item>

          </template>

          <template v-if="(parseInt(form.consumeType,10)===3)">
            <bc-form-item label="Write type"
                          prop="bigDataType">
              <bc-select placehold="Please select" v-model.number="form.bigDataType">
                <bc-option v-for="item in dict.topic.writeTypes"
                          :value=item.value
                          :label=item.label
                          :key="item.label"></bc-option>
              </bc-select>

            </bc-form-item>

            <bc-form-item label="Write config"
                          prop="bigDataConfig">
              <div class="bc-codemirror">
                <codemirror v-model="form.bigDataConfig"
                            :options="editorOptions"></codemirror>
                <bc-tooltip v-if="bigDataConfigError"
                            :content="bigDataConfigError"
                            placement="top">
                  <span class="bcui-icon-icon config-warnning"></span>
                </bc-tooltip>
              </div>
            </bc-form-item>
          </template>
        </template>
      </div>
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
    <div class="config-result">
      <div class="config-details">
      <div class="title-default">Config Detail</div>

      <div v-for="(item,index) in configResultArr"
            :key="item.key + index"
            class="detail-item">
        <div class="detail-item-key">{{item.key}}</div>
        <div class="detail-item-value">{{item.value}}</div>
      </div>

      <div v-for="(item,index) in advanceConfig" v-if="form.apiType === 1"
            :key="item.key + index"
            class="detail-item">
        <div class="detail-item-key">{{item.key}}</div>
        <div class="detail-item-value">{{item.value}}</div>
      </div>

      <div v-for="(item,index) in writeConfig" v-if="form.consumeType === 3"
            :key="item.key + index"
            class="detail-item">
        <div class="detail-item-key">{{item.key}}</div>
        <div class="detail-item-value">{{item.value}}</div>
      </div>
      </div>
    </div>

  </div>
  <template slot="footer">
      <bc-button @click="cancel">Cancel</bc-button>
      <bc-button type="primary" @click="handleCreateTopic" :loading="isSubmitting">Submit</bc-button>
  </template>
</bc-modal>
</template>

<script>
  import subscribeMixins from '../../../mixins/apis/subscribe.js';
  import commonMixins from '../../../mixins/common.js';
  import { createSub } from '../../../schemas/subscribe.js';

  export default {
    name: 'subscrib-create',
    mixins: [subscribeMixins, commonMixins],
    props: {
      value: {
        type: Boolean,
        default: false
      },

      subscribe: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data () {
      return {
        editorOptions: {
          tabSize: 4,
          mode: 'text/javascript',
          theme: 'base16-light',
          lineNumbers: true,
          line: true,
          lineWrapping: true
        },
        title: 'Create Subscription',
        formRule: createSub,
        form: {
          id: '',
          groupId: '',
          groupName: '',
          topicId: '',
          topicName: '',
          clusterId: '',
          clusterIds: [],
          maxTps: 128,
          alarmType: 0,
          alarmIsEnable: '',
          alarmMsgLag: '',
          alarmDelayTime: '',
          apiType: 1,
          consumeTimeout: 1000,
          errorRetryTimes: 3,
          retryIntervals: '50;100;150',
          msgType: 1,
          enableGroovy: 1,
          enableTransit: 1,
          groovy: '',
          transit: '',
          enableOrder: 1,
          orderKey: '',
          consumeType: 1,
          urls: [],
          httpMethod: 0,
          httpQueryParams: '',
          msgPushType: 1,
          httpToken: '',
          pushMaxConcurrency: 128,
          orderRemark: '',
          jsonPath: '',
          pressureTraffic: 0,
          bigDataType: 0,
          bigDataConfig: ''
        },
        isSubmitting: false,
        filterableGroups: [],
        filterableTopics: [],
        others: [{ key: '', value: '' }],
        transits: [{ key: '', value: '' }],
        queryParams: [{ key: '', value: '' }],
        loading: false,
        subDesc: {},
        groupQueryLoading: false,
        isShowAdvancedConfig: false,
        urls: [{ key: '' }],
        iconForAdvancedConfig: '',

        isEditing: false,
        bigDataConfigError: ''
      };
    },
    computed: {
      show: {
        get () {
          if (this.value && Object.keys(this.subscribe).length){
            this.isEditing = true;
            this.title = 'Edit Subscription';
            this.$nextTick(() => {
              this.formatData(this.subscribe);
            });
            this.others = this.formatObject(this.subscribe.extraParams);
          } else {
            this.isEditing = false;
            this.title = 'Create Subscription';
          }
          return this.value;
        },
        set () {}
      },
      configResultArr: {
        get () {
          return [ {
                     key: 'Consumer group',
                     value: this.form.groupId ? this.getGroupNameById(this.form.groupId, this.groupList) : '',
                   },
                   {
                     key: 'Topic',
                     value: this.form.topicId ? this.getTopicNameById(this.form.topicId, this.topicList) : '',
                   },
                   {
                     key: 'Consume maxTps',
                     value: this.form.maxTps
                   },
                   {
                     key: 'Receive Pressure Traffic',
                     value: this.form.pressureTraffic === 1 ? 'Enable' : 'Disable',
                   },
                   {
                     key: 'Api Level',
                     value: this.form.apiType === 1 ? 'High level' : 'Low level',
          }];
        },
        set () {
        }
      },

      advanceConfig: {
        get () {
          return [
            {
              key: 'Consume Timeout',
              value: this.form.consumeTimeout
            },
            {
              key: 'Error Retry Times',
              value: this.form.errorRetryTimes
            },
            {
              key: 'Retry Intervals',
              value: this.form.retryIntervals
            },
            {
              key: 'Message Type',
              value: this.dictTranslate('topic', 'msgTypes', this.form.msgType)
            },
            {
              key: 'Enable GroovyScript',
              value: this.dictTranslate('topic', 'groovyStates', this.form.enableGroovy)
            },
            {
              key: 'Enable Transit',
              value: this.dictTranslate('topic', 'transitStates', this.form.enableTransit)
            },
            {
              key: 'Enable Sequential Consume',
              value: this.dictTranslate('topic', 'orderStates', this.form.enableOrder)
            },
            {
              key: 'Consume Type',
              value: this.dictTranslate('topic', 'consumeTypes', this.form.consumeType)
            },
            {
              key: 'Basis Ordering rule',
              value: this.dictTranslate('topic', 'orderKeyTypes', this.form.orderKey)
            },
            {
              key: 'HttpMethod',
              value: this.dictTranslate('topic', 'httpMethodTypes', this.form.httpMethod)
            },
            {
              key: 'Http Token',
              value: this.form.httpToken
            },
            {
              key: 'Push Concurrency',
              value: this.form.pushMaxConcurrency
            }
          ];
        },
        set () {
        }
      },

      writeConfig: {
        get () {
          return [
            {
              key: 'Write type',
              value: this.dictTranslate('topic', 'writeTypes', this.form.bigDataType),
            }
          ];
        }
      }

    },
    methods: {

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

      toggleAdvancedConfig () {
        if (this.isShowAdvancedConfig) {
          this.isShowAdvancedConfig = false;
          this.iconForAdvancedConfig = 'chevron-bottom';
        } else {
          this.isShowAdvancedConfig = true;
          this.iconForAdvancedConfig = 'chevron-top';
        }
      },

      // 针对线下环境初始化默认值
      initData () {
        if (window.ENV === 'daily' || window.ENV === 'development') {
          this.form.maxTps = 128;
          this.form.pushMaxConcurrency = 16;
        }
      },

      // 增加key,value
      addConf (obj) {
        obj.push({
          key: '',
          value: ''
        });
      },

      // 删除key,value
      removeConf (item, obj) {
        const index = obj.indexOf(item);
        if (index !== -1) obj.splice(index, 1);
      },

      // 添加url
      addUrl (urls) {
        urls.push({
          key: ''
        });
      },

      // 删除url
      removeUrl (item, urls) {
        const index = urls.indexOf(item);
        if (index !== -1) urls.splice(index, 1);
      },

      cancelCreateSub () {
        this.$router.push(`/subscribes`);
      },

      formatUrl () {
        let result = this.urls.map((item) => {
          return item.key;
        });
        return result;
      },

      handleCreateTopic () {
        let { form } = this;
        let isValid = this.validateForm('form');

        if (!isValid) {
          this.$notice.error({
            title: 'Please checkout form',
            duration: 0
          });
          return;
        }

        if (parseInt(form.consumeType, 10) === 3 &&
          form.bigDataConfig && this.bigDataConfigError) {
            this.$notice.error({
              title: '写入配置选项，请填写正确的json格式',
              duration: 0
            });
            return;
          }

        if (this.isSave) {
          this.isSaveButtonLoading = true;
        } else {
          this.isButtonLoading = true;
        }

        let body = {
          user: 'administration',
          subId: this.isEditing ? form.subId : 0,
          groupId: form.groupId,
          groupName: form.groupName || this.getGroupNameById(form.groupId),
          topicId: form.topicId,
          topicName: form.topicName || this.getTopicNameById(form.topicId),
          clusters: { 'ddmq': 1 },
          maxTps: Number(form.maxTps),
          alarmType: 0,
          alarmIsEnable: 1,
          alarmMsgLag: 10000,
          alarmDelayTime: 300000,
          apiType: form.apiType,
          extraParams: this.formatMap(this.others),
          queryParams: {},
          transit: {},
          httpQueryParams: {},
          urls: [],
          orderRemark: form.orderRemark,
          pressureTraffic: form.pressureTraffic
        };

        // 接口类型选择决定读取以下字段
        if (body.apiType === 1) {
          if (form.consumeTimeout) {
            body.consumeTimeout = Number(form.consumeTimeout);
          }
          if (form.errorRetryTimes) {
            body.errorRetryTimes = Number(form.errorRetryTimes);
          }

          if (form.retryIntervals) {
            body.retryIntervals = form.retryIntervals
              .split(';')
              .filter((item) => item);
          }

          body.msgType = form.msgType;
          // 消息类型决定读取以下字段
          if (body.msgType === 1) {
            body.enableGroovy = form.enableGroovy;
            if (form.enableGroovy === 0 && form.groovy) {
              body.groovy = form.groovy;
            }
            body.enableTransit = form.enableTransit;
            if (form.enableTransit === 0) {
              body.transit = this.formatMap(this.transits) || null;
            }
          }

          body.enableOrder = form.enableOrder;

          // 是否顺序消费决定读取以下参数
          if (body.enableOrder === 0) {
            if (form.orderKey == 'JsonPath') {
              body.orderKey = form.jsonPath;
            } else {
              body.orderKey = form.orderKey;
            }
          }

          // 消费方式决定是否读取以下值
          body.consumeType = parseInt(form.consumeType, 10);
          if (body.consumeType === 2) {
            body.urls = this.formatUrl();
            body.httpMethod = parseInt(form.httpMethod, 10);
            body.httpQueryParams = this.formatMap(this.queryParams);
            body.msgPushType = parseInt(form.msgPushType, 10);
            body.httpToken = form.httpToken;
            body.pushMaxConcurrency = parseInt(form.pushMaxConcurrency, 10);
          } else if (body.consumeType === 3) {
            body.bigDataType = form.bigDataType;
            body.bigDataConfig = form.bigDataConfig.trim('');
          }
        }

        this.createSub(body)
          .then((body) => {
            this.isSaveButtonLoading = false;
            this.isButtonLoading = false;
            if (body.errno === 0) {
              this.cancel();
              this.$root.bus.$emit('updateSubscribeList');
            }
          })
          .catch(() => {
            this.isSaveButtonLoading = false;
            this.isButtonLoading = false;
        });
      },

      // 获取搜索topic数据
      filterTopics (query) {
        let source = this.topicList;

        if (query !== '') {
          this.loading = true;
          setTimeout(() => {
            this.loading = false;
            const list = source.map((item) => {
              return {
                value: item.topicId,
                label: item.topicName
              };
            });
            this.filterableTopics = list
              .filter((item) => {
                return item.label.toLowerCase().indexOf(query.toLowerCase()) > -1;
              })
              .slice(0, 99);
          }, 100);
        } else {
          this.filterableTopics = source.slice(0, 99);
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

      // 获取消费组下拉框数据
      getGroups () {
        this.fetchGroupsWithOutPage({
          params: {
            user: 'administration'
          }
        }).then((result) => {
          let groupList = result.data;
          this.groups = groupList.map((item) => {
            return {
              label: item.groupName,
              value: item.groupId
            };
          });
        });
      },

      // 获取topic列表下拉框数据
      getTopics () {
        this.fetchTopicsWithOutPage({
          params: {
            user: 'administration'
          }
        }).then((result) => {
          this.topicList = result.data;
        });
      },

      // 获取字段提示
      getSubDesc () {
        this.fetchSubDesc({
          params: {
            user: 'administration'
          }
        }).then((result) => {
          this.subDesc = result.data;
        });
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

      cancel () {
        this.isSubmitting = false;
        this.others = [{ key: '', value: '' }];
        this.transits = [{ key: '', value: '' }];
        this.queryParams = [{ key: '', value: '' }];
        this.urls = [{ key: '' }];
        this.$refs['form'].resetFields();
        this.$emit('input', false);
      },

      init () {
        this.getMsgPushTypes();
        this.getGroups();
        this.getTopics();
        // this.getSubDesc();
        this.initData();
        this.toggleAdvancedConfig();
      },

      // 根据消费组Id获取消费组名
      getGroupNameById (id) {
        let groupName = '';
        this.groups.forEach((item) => {
          if (item.value === id) {
            groupName = item.label;
          }
        });
        return groupName;
      },

      // 根据TopicId获取Topic名
      getTopicNameById (id) {
        let topicName = '';
        this.topicList.forEach((item) => {
          if (item.topicId === id) {
            topicName = item.topicName;
          }
        });
        return topicName;
      },

      formatData (data) {
        this.form = JSON.parse(JSON.stringify(data));

        if (data.retryIntervals && data.retryIntervals.length && data.retryIntervals instanceof Array) {
          this.form.retryIntervals = data.retryIntervals.join(';');
        }
        if (data.urls && data.urls.length) {
          this.urls = this.formatUrlObject(data.urls, this.urls);
        }
        if (data.httpQueryParams && Object.keys(data.httpQueryParams).length) {
          this.queryParams = this.formatObject(
            data.httpQueryParams,
            this.queryParams
          );
        }
        if (data.transit && Object.keys(data.transit).length) {
          this.transits = this.formatObject(data.transit, this.transits);
        }
        if (data.extraParams && Object.keys(data.extraParams).length) {
          this.others = this.formatObject(data.extraParams, this.others);
        }

        if (data.orderKey != 'QID' && data.orderKey != 'KEY') {
          this.form.jsonPath = data.orderKey;
          this.form.orderKey = 'JsonPath';
        }
      }
    },
    watch: {

      'form.bigDataConfig' (newValue) {
        if (newValue) {
          try {
            JSON.parse(newValue);
            this.bigDataConfigError = '';
          } catch (error) {
            this.bigDataConfigError = error.message;
          }
        } else {
          this.bigDataConfigError = '';
        }
      }

    },
    created () {
      this.init();
    },
    beforeMount () {},
    mounted () {
    }

  };
</script>
