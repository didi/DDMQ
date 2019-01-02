<style lang="less">
.drawer-detail {
  .title {
    margin-bottom: 20px;
    // margin: 0 0 16px;
    font-size: 14px;
    font-weight: 800;
    color: #333333;
  }
  .detail-info-block {
    background: #fafafa;
    padding: 0px 15px;
    
  }
  .detail-info-title {
    font-size: 16px;
    font-weight: 500;
    margin: 20px 0px 15px;;
    border-left: 3px solid #2d77ee;
    padding-left: 12px;
    line-height: 1.2;

  }
  .detail-info-title:first-child {
    margin-top: 0px;
  }
  // .detail-item:nth-child(even)  {
  //   background: #fafafa;
  // }

  .detail-item {
      display: flex;
      padding: 8px 0;
    }

    .detail-item-key {
      width: 30%;
      font-size: 14px;
      color: #333333;
    }
     .detail-item-value {
      font-size: 14px;
      color: #666666;
     }
}

</style>

<template>
<div class="drawer-detail">
  <bc-row :gutter="48">
      <bc-col span="24">
        <div class="detail-info-detail-item">
          <div class="detail-info-title">Basic information</div>
          <div class="detail-info-block">
            <div class="detail-item">
              <div class="detail-item-key">Consumer Group</div>
              <div class="detail-item-value">{{basicInfo.groupName}}</div>
            </div>
            <div class="detail-item">
              <div class="detail-item-key">Topic</div>
              <div class="detail-item-value">{{basicInfo.topicName}}</div>
            </div>
            <div class="detail-item">
              <div class="detail-item-key">Consume maxTps</div>
              <div class="detail-item-value">{{basicInfo.maxTps}}</div>
            </div>
            <div class="detail-item">
              <div class="detail-item-key">Receive Pressure Traffic</div>
              <div class="detail-item-value">{{this.dictTranslate('topic', 'pressureTrafficStates', basicInfo.pressureTraffic) }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-item-key">API Level</div>
              <div class="detail-item-value">{{this.dictTranslate('topic', 'apiTypes', basicInfo.apiType) }}</div>
            </div>
          </div>
          <div class="detail-info-title">Advanced config</div>
          <template v-if="basicInfo.apiType === CONSATNT.SUBSCRIBE_API_HIGHT_LEVEL">
            <div class="detail-info-block">
              <div class="detail-item">
                <div class="detail-item-key">Consume Timeout</div>
                <div class="detail-item-value">{{basicInfo.consumeTimeout }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-item-key">Error Retry Times</div>
                <div class="detail-item-value">{{basicInfo.errorRetryTimes }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-item-key">Retry Intervals</div>
                <div class="detail-item-value">{{basicInfo.retryIntervals }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-item-key">Message Type</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'msgTypes', basicInfo.msgType) }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-item-key">Enable GroovyScript</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'groovyStates', basicInfo.enableGroovy) }}</div>
              </div>
              <div class="detail-item" v-if="basicInfo.enableGroovy === CONSATNT.SUBSCRIBE_GROOVY_ENABLE">
                <div class="detail-item-key">GroovyScript</div>
                <div class="detail-item-value">{{ basicInfo.groovy}}</div>
              </div>
              <div class="detail-item">
                <div class="detail-item-key">Enable Transit</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'transitStates', basicInfo.enableTransit) }}</div>
              </div>
              <div class="detail-item" v-if="basicInfo.enableTransit === CONSATNT.SUBSCRIBE_TRANSIT_ENABLE">
                <div class="detail-item-key">Transits</div>
                <div class="detail-item-value">{{ basicInfo.transit}}</div>
              </div>

              <div class="detail-item">
                <div class="detail-item-key">Consume Type</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'consumeTypes', basicInfo.consumeType) }}</div>
              </div>

              <div class="detail-item">
                <div class="detail-item-key">Basic Ordering Rule</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'orderKeyTypes', basicInfo.orderKey) }}</div>
              </div>

              <div class="detail-item">
                <div class="detail-item-key">HttpMethod</div>
                <div class="detail-item-value">{{this.dictTranslate('topic', 'httpMethodTypes', basicInfo.httpMethod) }}</div>
              </div>

              <div class="detail-item">
                <div class="detail-item-key">Message Push Type</div>
                <div class="detail-item-value">{{basicInfo.msgPushTypeDesc}}</div>
              </div>
              
            </div>
          </template>
          <template v-else>
            <div class="detail-item-key">Have no advanced config</div>
          </template>
        </div>
      </bc-col>
  </bc-row>

</div>

</template>

<script>

  import commonmixins from '../../../mixins/common.js';
  import CONSATNT from '../../../mixins/constant.js';

  export default {
    name: 'subscribe-detail',
    mixins: [commonmixins],
    props: {
      basicInfo: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data () {
      return {
        CONSATNT
      };
    }
  };
</script>
