<style lang="less">

</style>

<template>
  <div :class="stepItemClasses" :style="stepItemStyles">
    <!--放在最前面， 使用定位后，同层级下位置最低-->
    <div :class="[`${classPrefix}-tail`]">
      <span :class="[`${classPrefix}-tail__line`]"></span>
    </div>
    <div :class="[`${classPrefix}-head`]">
      <div :class="[`${classPrefix}-head__inner`]">
        <span v-if="showIcon" :class="iconClasses"></span>
        <span v-else class="bcui-steps-icon">{{stepNumber}}</span>
      </div>
    </div>
    <div :class="[`${classPrefix}-body`]">
      <div v-if="title" :class="[`${classPrefix}-title`]">{{title}}</div>
      <div v-if="description" :class="[`${classPrefix}-description`]">{{description}}</div>
    </div>
  </div>
</template>

<script>
  import { oneOf } from '../../utils/tools';
  const classPrefix = 'bcui-steps';

  export default {
    name: 'bc-steps-item',
    components: {},
    props: {
      icon: {
        type: String,
        default: '',
      },
      title: {
        type: String,
        default: ''
      },
      description: {
        type: String,
        default: ''
      },
      status: {
        validator (value) {
          return oneOf(value, ['wait', 'process', 'finish', 'error']);
        }
      },
    },
    data() {
      return {
        classPrefix,
        stepNumber: '',
        stepLength: 1,
        nextError: false,
        currentStatus: '',
      };
    },
    watch: {
      status (val) {
        this.currentStatus = val;
        if (this.currentStatus === 'error') {
          this.$parent.setNextError();
        }
      }
    },
    computed: {
      showIcon() {
        return this.icon || this.currentStatus === 'finish' || this.currentStatus === 'error';
      },
      stepItemClasses() {
        return [
          `${classPrefix}-item`,
          `${classPrefix}-status--${this.currentStatus}`,
          {
            [`${classPrefix}-custom`]: !!this.icon,
            [`${classPrefix}-next-error`]: this.nextError
          }]
      },
      iconClasses () {
        let icon = '';
        let iconClassPrefix = 'bcui-icon';
        if (this.icon) {
          icon = this.icon;
        } else {
          if (this.currentStatus === 'finish') {
            icon = 'dui';
          } else if (this.currentStatus === 'error') {
            icon = 'close';
          }
        }
        return [
          `${classPrefix}-icon`,
          `${iconClassPrefix}`,
          {
            [`${iconClassPrefix}-${icon}`]: icon !== '',
          }
        ];
      },
      stepItemStyles() {
        if (this.stepLength === this.stepNumber) {
          return {};
        } else {
          return {
            marginRight: `-${this.$parent.offset}px`,
            width: `${100 / (this.stepLength - 1)}%`,
          };
        }
      }
    },

    methods: {},
    created () {
      this.currentStatus = this.status;
    },
    mounted() {},
  };
</script>
