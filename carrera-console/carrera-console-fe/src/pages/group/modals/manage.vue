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
            {{'Consumer Group:' + item.groupName }}
      </div>
    </div>
    <template slot="footer">
      <bc-button @click="cancel">Cancel</bc-button>
      <bc-button type="primary" @click="submit" :loading="isSubmitting">Submit</bc-button>
  </template>
  </bc-modal>
</template>

<script>
  import groupMixins from '../../../mixins/apis/group.js';
  import CommonMixins from '../../../mixins/common';

  export default {
    name: 'modal-enable-group',
    mixins: [groupMixins, CommonMixins],
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
            title: 'Enable All subscription',
            subTitle: `Are you sure to enable all subscription under the following ${this.resources.length} consumer groups？`
          },
          disable: {
            title: 'Disable All subscription',
            subTitle: `Are you sure to disable all subscription under the following ${this.resources.length} consumer groups？`
          },
          disableAlarm: {
            title: 'Disable alarm',
            subTitle: `Are you sure to disable alarm the following ${this.resources.length} consumer groups？`
          },
          enableAlarm: {
            title: 'Enable alarm',
            subTitle: `Are you sure to enable alarm the following ${this.resources.length} consumer groups？`
          },
          delete: {
            title: 'Delete group',
            subTitle: `Are you sure to delete the following ${this.resources.length} consumer groups？`
          }
        };
      }
    },
    methods: {
      cancel () {
        this.isSubmitting = false;
        this.$emit('input', false);
      },

      getEnablePromises (data) {
        return data.map((item) => {
          let params = {
            groupId: item.groupId,
            user: 'administration',
            state: 0
          };
          return this.changeAllSubscribeState({ params });
        });
      },

      getDisablePromises (data) {
        return data.map((item) => {
          let params = {
            groupId: item.groupId,
            user: 'administration',
            state: 1
          };
          return this.changeAllSubscribeState({ params });
        });
      },

      getDeletePromises (data) {
        return data.map((item) => {
          let params = {
            groupId: item.groupId,
            user: 'administration'
          };
          return this.deleteGroup({ params });
        });
      },

      getDisableAlarmPromises (data) {
        return data.map((item) => {
          let params = {
            groupId: item.groupId,
            user: 'administration',
            state: 1
          };
          return this.changeAlarmState({ params });
        });
      },

      getEnableAlarmPromises (data) {
        return data.map((item) => {
          let params = {
            groupId: item.groupId,
            user: 'administration',
            state: 0
          };
          return this.changeAlarmState({ params });
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

        case 'disableAlarm':
          promises = this.getDisableAlarmPromises(resources);
          break;

        case 'enableAlarm':
          promises = this.getEnableAlarmPromises(resources);
          break;

        default:
          break;
        }

        Promise.all(promises).then((body) => {
          this.cancel();
          this.$root.bus.$emit('updateGroupList');
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
