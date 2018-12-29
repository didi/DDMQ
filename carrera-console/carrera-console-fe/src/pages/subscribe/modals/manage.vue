<style lang="less">

</style>

<template>
  <bc-modal
    v-model="show"
    v-if="show"
    :width="552"
    :title="title"
    @on-cancel="cancel">
    <div class="modal-container">
      <div class="modal-text subtitle">{{subTitle}}</div>
      <div class="modal-text description" v-if="description">{{description}}</div>
    </div>
    <div class="modal-rect">
      <div class="modal-rect__item"
            v-for="(item) in resources"
            :key="item.id">
            {{'Topic:' + item.topicName+ ', Consumer Group:' + item.groupName }}
      </div>
    </div>
    <template slot="footer">
      <bc-button @click="cancel">Cancel</bc-button>
      <bc-button type="primary" @click="submit" :loading="isSubmitting">Submit</bc-button>
  </template>
  </bc-modal>
</template>

<script>
  import SubscribeMixins from '../../../mixins/apis/subscribe.js';
  import CommonMixins from '../../../mixins/common';

  export default {
    name: 'modal-enable-subscribe',
    mixins: [SubscribeMixins, CommonMixins],
    components: {},
    props: {
      value: {
        type: Boolean,
        default: false
      },

      resources: {
        type: Array,
        required: true
      },

      type: {
        type: String,
        required: true,
        default: 'enable'
      }
    },
    data () {
      return {
        isSubmitting: false
      };
    },
    watch: {},
    computed: {
      title () {
        return this.typeDict[this.type].title;
      },
      subTitle () {
        return this.typeDict[this.type].subTitle;
      },
      description () {
        return this.typeDict[this.type].description;
      },
      show: {
        get () {
          return this.value;
        },
        set () {}
      },
      typeDict () {
        return {
          enable: {
            title: 'Enable subscription',
            subTitle: `Are you sure to enable the following ${this.resources.length} subscriptions？`
          },
          disable: {
            title: 'Disable subscription',
            subTitle: `Are you sure to disable the following ${this.resources.length} subscriptions？`
          },
          delete: {
            title: 'Delete subscription',
            subTitle: `Are you sure to delete the following ${this.resources.length} subscriptions？`
          }
        };
      }
    },
    methods: {
      cancel () {
        this.isSubmitting = false;
        this.$emit('input', false);
      },

      // createOptions(type) {
      //   type = type || this.type;
      //   let options = {};

      //   switch (type) {
      //     case 'enable':
      //     options = {

      //     }
      //   }

      // },
      getEnablePromises (data) {
        return data.map((item) => {
          let params = {
            subId: item.subId,
            user: 'administration',
            state: 0
          };
          return this.changeSubscribeState({ params });
        });
      },

      getDisablePromises (data) {
        return data.map((item) => {
          let params = {
            subId: item.subId,
            user: 'administration',
            state: 1
          };
          return this.changeSubscribeState({ params });
        });
      },

      getDeletePromises (data) {
        return data.map((item) => {
          let params = {
            subId: item.subId,
            user: 'administration'
          };
          return this.deleteSubscribe({ params });
        });
      },

      submit () {
        let { resources } = this;
        this.isSubmitting = true;

        let promises = [];

        switch (this.type) {
        case 'enable':
          promises = this.getEnablePromises(resources);
          break;

        case 'disable':
          promises = this.getDisablePromises(resources);
          break;

        case 'delete':
          promises = this.getDeletePromises(resources);
          break;

        default:
          break;
        }

        Promise.all(promises).then((body) => {
          this.cancel();
          this.$root.bus.$emit('updateSubscribeList');
          if (body.every((item) => item.errno === 0)) {
            this.$notice.success({
              title: 'Success'
            });
          }
        }).finally(() => {
          this.isSubmitting = false;
        });
      }

    },
    mounted () {

    },
    beforeDestroy () {
    }
  };
</script>
