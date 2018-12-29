<style lang="less">
</style>

<template>
  <div :class="stepsClasses">
    <slot></slot>
  </div>
</template>

<script>
  function oneOf (target, list) {
    return list.indexOf(target) !== -1;
  }

  const classPrefix = 'bcui-steps';

  export default {
    name: 'bc-steps',
    components: {},
    props: {
      current: {
        type: Number,
        default: 0,
      },
      size: {
        type: String,
        validator(value) {
          return oneOf(value, ['small']);
        }
      },
      direction: {
        type: String,
        validator(value) {
          return oneOf(value, ['horizontal', 'vertical']);
        },
        default: 'horizontal',
      },
      status: {
        validator (value) {
          return oneOf(value, ['wait', 'process', 'finish', 'error']);
        },
        default: 'process'
      },

    },
    data() {
      return {
        classPrefix,
        offset: 0,
      };
    },
    watch: {
      current () {
        this.updateStepItem();
      },
      status () {
        this.updateCurrent();
      }
    },
    computed: {
      stepsClasses() {
        return [classPrefix,
          `${classPrefix}--${this.direction}`,
          {
            [`${classPrefix}-${this.size}`]: !!this.size,
          },
        ];
      },

    },
    methods: {

      updateStepItem(isInit) {
        const stepLength = this.$children.length;
        let { direction } = this;
        let _this = this;
        this.$children.forEach((child, i) => {
          child.stepNumber = i + 1;

          if (direction === 'horizontal') {
            child.stepLength = stepLength;
          }

          // 如果已存在status,且在初始化时,则略过
          if (!(isInit && child.currentStatus)) {
            if (i === this.current) {
              if (this.status !== 'error') {
                child.currentStatus = 'process';
              }
            } else if (i < this.current) {
              child.currentStatus = 'finish';
            } else {
              child.currentStatus = 'wait';
            }
          }
          if (child.currentStatus !== 'error' && i !== 0) {
            this.$children[i - 1].nextError = false;
          }

          if (i === stepLength - 1) {
            // 加 4 做计算修正
            _this.offset = child.$el.offsetWidth / (stepLength - 1) + 4;
          }

        });

      },

      setNextError () {
        this.$children.forEach((child, i) => {
          if (child.currentStatus === 'error' && i !== 0) {
            this.$children[i - 1].nextError = true;
          }
        });
      },
      updateCurrent (isInit) {

        // 防止溢出边界
        if (this.current < 0 || this.current >= this.$children.length) {
          return;
        }
        if (isInit) {
          const current_status = this.$children[this.current].currentStatus;
          if (!current_status) {
            this.$children[this.current].currentStatus = this.status;
          }
        } else {
          this.$children[this.current].currentStatus = this.status;
        }
      }
    },
    mounted() {
      this.updateStepItem(true);
      this.setNextError();
      this.updateCurrent(true);
    },
  };
</script>
