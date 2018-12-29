<style lang="less">

</style>

<template>

  <bc-modal v-model="show"
            v-if="show"
            :title="title"
            :offsetTop="40"
            :width="610"
            class="create-group-modal"
            @on-cancel="cancel">
    <div class="group-create">
      <bc-form class="clearfix"
               ref="form"
               :width="540"
               :model="form"
               :rules="formRule"
               labelAlign="top">

        <bc-form-item label="Consumer Group：" prop="groupName">
          <bc-input v-model="form.groupName"
                    :disabled='isEditing'
                    placeholder="Please input Consumer Group">
                    <span slot="prepend">cg_</span>
          </bc-input>
        </bc-form-item>
        <div class="alert-message">
          <div>1. The Consumer Group name can contain only letters, numbers, underscore(_)and dashes(-)</div>
          <div>2. Once created can't modify the Consumer Group name</div>
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
    <template slot="footer">
        <bc-button @click="cancel">Cancel</bc-button>
        <bc-button type="primary" @click="handleCreateGroup" :loading="isSubmitting">Submit</bc-button>
    </template>
  </bc-modal>
</template>

<script>
  import groupMixins from '../../../mixins/apis/group.js';
  import commonMixins from '../../../mixins/common.js';
  import { createGroup } from '../../../schemas/group.js';
  export default {
    name: 'create-group-modal',
    mixins: [groupMixins, commonMixins],
    components: { },

    props: {
      value: {
        type: Boolean,
        default: false
      },

      group: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data () {
      return {
        title: 'Create Consumer Group',
        isSubmitting: false,

        isEditing: false,

        others: [{ key: '', value: '' }],
        form: {
          groupId: '',
          groupName: ''
        },
        formRule: createGroup,

        topicDesc: {},
        showConfOperationParams: false
      };
    },
    computed: {
      show: {
        get () {
          if (this.value && Object.keys(this.group).length) {
            this.isEditing = true;
            this.title = 'Edit Consumer Group';
            this.$nextTick(() => {
              this.form = JSON.parse(JSON.stringify(this.group));
              this.form.groupName = this.form.groupName.replace('cg_', '');
            });
            this.others = this.formatObject(this.group.extraParams);
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
        this.title = 'Create Consumer Group';
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
      handleCreateGroup () {
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
          groupId: this.isEditing ? form.groupId : 0,
          groupName: 'cg_' + form.groupName,
          service: 'Engineering',
          department: 'Software',
          contacters: 'administration;',
          alarmGroup: null,
          alarmLevel: 1,
          alarmIsEnable: 1,
          alarmMsgLag: 10000,
          alarmDelayTime: 300000,
          extraParams: {},
          broadcastConsume: 1,
          consumeMode: 1,
          consumeModeMapper: null,
          operationParams: null
        };
        body.extraParams = this.formatMap(this.others);

        this.createGroup(body)
          .then((body) => {
            if (body.errno === 0) {
              this.$root.bus.$emit('updateGroupList');
              this.cancel();
            }
          }).finally(() => {
            this.isSaveLoading = false;
            this.isSubmitting = false;
        });
      }
    },

    mounted () {

    }
  };
</script>
