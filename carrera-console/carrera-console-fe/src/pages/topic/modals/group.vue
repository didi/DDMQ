<template>
   <div class="block">


      <div class="block-body">
        <bc-table :tableData="groupList"
                  :class="['bcui-table--circular', 'bcui-table--dark']"
                  v-loading="loading"
                  :border="false"
                  :pagination="false"
                  empty-text="No data"
                  ref="table">
          <bc-table-column field="groupName"
                           index="groupName"
                           label="Consumer Group"
                           width="400px">
          </bc-table-column>

          <bc-table-column field="createTime" index="createTime" label="Create Time">
          </bc-table-column>
          <bc-table-column field="state" index="state" label="Status">
            <template slot-scope="scope">
              <span :class="coloringText('topic', 'states', parseInt(scope.record.state,10))">{{dictTranslate('topic','states', parseInt(scope.record.state, 10))}}</span>
            </template>
          </bc-table-column>
        </bc-table>
      </div>
    </div>
</template>

<script>
  import topicMixins from '../../../mixins/apis/topic.js';
  import commonmixins from '../../../mixins/common.js';

  export default {
    name: 'topic-group',
    mixins: [ topicMixins, commonmixins ],

    data () {
      return {
        groupList: [],
        loading: false
      };
    },

    methods: {

      getGroupList () {
        let topicId = this.$route.params.id;
        let params = {
          topicId,
          clusterId: 1,
          user: 'administration'
        };
        this.loading = true;
        this.fetchGroupsByTopic({ params }).then((body) => {
          this.groupList = body.data || [];
        }).finally(() => {
          this.loading = false;
        });
      }

    },

    mounted () {
      this.getGroupList();
    }
  };
</script>
