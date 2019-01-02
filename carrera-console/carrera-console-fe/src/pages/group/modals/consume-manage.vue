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
    name: 'modal-manage-consume',
    mixins: [groupMixins, CommonMixins],
    components: {},
    props: {
      value: {
        type: Boolean,
        default: false
      },

      resource: {
        type: Object,
        required: true
      },

      params: {
        type: Object,
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
          resetValue: {
            title: 'Reset offset',
            subTitle: 'Are you sure to reset offset to specified value ?',
            description: `Topic: ${this.resource.topicName}, reset offset value to ${this.resource.offset}`
          },

          resetToNew: {
            title: 'Reset offset',
            subTitle: 'Are you sure to reset offset to newest ?',
            description: `Topic: ${this.resource.topicName}`
          },

          resetToTime: {
            title: 'Reset offset',
            subTitle: 'Are you sure to reset offset to specified time ?',
            description: `Topic: ${this.resource.topicName}, reset offset to ${this.params.resetTime}？`
          }
        };
      }
    },
    methods: {
      cancel () {
        this.isSubmitting = false;
        this.$emit('input', false);
      },

      submit () {
        let { params } = this;
        params.user = 'administration';

        this.isSubmitting = true;

        this.resetOffset({ params })
          .then((body) => {
            if (body.errno === 0) {
              this.$notice.success({
                title: 'success'
              });
              this.$root.bus.$emit('updateConsumeList');
              this.cancel();
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
