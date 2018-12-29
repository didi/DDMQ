<template>
   <div class="block">

      <div class="block-body">
        <bc-table :tableData="topicStatisticList"
                  :class="['bcui-table--circular', 'bcui-table--dark']"
                  v-loading="loading"
                  :border="false"
                  :pagination="false"
                  empty-text="No data"
                  ref="table">
            <bc-table-column field="qid"
                             index="qid"
                             label="Qid">
            </bc-table-column>
            <bc-table-column field="minOffset"
                             index="minOffset"
                             label="MinOffset"
                             width="100px">
            </bc-table-column>
            <bc-table-column field="maxOffset"
                             index="maxOffset"
                             label="MaxOffset"
                             width="100px">
            </bc-table-column>
            <bc-table-column field="lastUpdateTime"
                             index="lastUpdateTime"
                             label="LastUpdateTime">
            </bc-table-column>
        </bc-table>
      </div>
    </div>
</template>

<script>
  import topicMixins from '../../../mixins/apis/topic.js';
  import commonmixins from '../../../mixins/common.js';

  export default {
    name: 'topic-statistic',
    mixins: [ topicMixins, commonmixins ],

    data () {
      return {
        topicStatisticList: [],
        loading: false
      };
    },

    methods: {

      getTopicStatisticList () {
        let topicId = this.$route.params.id;
        let params = {
          topicId,
          clusterId: 1,
          user: 'administration'
        };
        this.loading = true;
        this.fetchTopicStatistics({ params }).then((body) => {
          this.topicStatisticList = body.data || [];
        }).finally(() => {
          this.loading = false;
        });
      }

    },

    mounted () {
      this.getTopicStatisticList();
    }
  };
  </script>
