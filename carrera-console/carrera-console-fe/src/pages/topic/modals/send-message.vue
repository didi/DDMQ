<style lang="less">

</style>

<template>

  <bc-modal v-model="show"
            v-if="show"
            :title="title"
            :offsetTop="40"
            :width="610"
            class="send-message-modal"
            @on-cancel="cancel">
    <div class="send-message">
      <bc-form class="clearfix"
               ref="form"
               :width="540"
               :model="form"
               :rules="formRule"
               labelAlign="top">

        <bc-form-item label="Message：" prop="message">
          <bc-input v-model="form.message"
                    placeholder="Please input message" type="textarea" :rows="5">
                    </bc-input>
        </bc-form-item>
      </bc-form>
    </div>
    <template slot="footer">
        <bc-button @click="cancel">Cancel</bc-button>
        <bc-button type="primary" @click="handleSendMessage" :loading="isSubmitting">Submit</bc-button>
    </template>
  </bc-modal>
</template>

<script>
  import groupMixins from '../../../mixins/apis/topic.js';
  import commonMixins from '../../../mixins/common.js';

  export default {
    name: 'create-group-modal',
    mixins: [groupMixins, commonMixins],
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
        title: 'Send message',
        isSubmitting: false,

        form: {
          message: ''
        },

        formRule: {
          message: {
            required: true,
            message: 'Please input message'
          },
        },

      };
    },
    computed: {
      show: {
        get () {
          return this.value;
        },
        set () {}
      }
    },
    methods: {
      cancel () {
        this.isSubmitting = false;
        this.$refs['form'].resetFields();
        this.$emit('input', false);
      },

      // 确定新建topic
      handleSendMessage () {
        let { form } = this;
        let isValid = this.validateForm('form');
        if (!isValid) {
          this.$notice.error({
            title: 'Please checkout form',
            duration: 0
          });
          return;
        }

        this.isSubmitting = true;
        let body = {
          user: 'administration',
          topicId: this.topic.topicId,
          topicName: this.topic.topicName,
          msg: form.message
        };

        this.sendMessage(body)
          .then((body) => {
            this.cancel();
          }).finally(() => {
            this.isSubmitting = false;
        });
      }
    },
    watch: {
    }

  };
</script>
