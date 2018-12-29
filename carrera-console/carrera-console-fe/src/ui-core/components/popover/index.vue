<template>
  <div
    :class="[wrapperClasses]"
    v-clickoutside="handleClose"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave">
    <div
      :class="[classPrefix + '-rel']"
      ref="reference"
      @click="handleClick">
      <slot></slot>
    </div>
    <transition :name="transition">
      <Drop
        v-show="show"
        :placement="placement"
        :class="['bcui-popover__inner']"
        ref="drop"
        @mouseenter.native="handleMouseEnter"
        @mouseleave.native="handleMouseLeave">
        <div :class="[classPrefix + '__body']">
          <div :class="[classPrefix + '__arrow']"></div>
            <div :class="[classPrefix + '__inner-content']">
              <div :class="[classPrefix + '__title']">
                <slot name="title">{{title}}</slot>
              </div>
              <div :class="[classPrefix + '__content']">
                <slot name="content">{{content}}</slot>
              </div>
            </div>
        </div>
      </Drop>
    </transition>
  </div>
</template>
<script>
  import Drop from './popover-drop.vue';
  import clickoutside from '../../directives/clickoutside';
  import { oneOf, findComponentUpward } from '../../utils/tools';

  const classPrefix = 'bcui-popover';

  export default {
    name: 'bc-popover',
    directives: { clickoutside },
    props: {
      placement: {
        validator(value) {
          return oneOf(value, ['top', 'top-start', 'top-end', 'bottom', 'bottom-start', 'bottom-end', 'left', 'left-start', 'left-end', 'right', 'right-start', 'right-end']);
        },
        default: 'top',
      },
      value: {
        type: Boolean,
        default: false,
      },
      title: {
        type: String,
      },
      content: {
        type: String,
      },

      // 触发方式
      trigger: {
        validator(value) {
          return oneOf(value, ['click', 'hover', 'focus', 'custom']);
        },
        default: 'click',
      },
    },
    components: {
      Drop,
    },
    data() {
      return {
        classPrefix,
        show: this.value,
        timer: null,
      };
    },
    watch: {
      value (update) {
        this.show = update;
      },
      show (update) {
        if (update) {
          this.$refs.drop.update();
        } else {
          this.$refs.drop.destroy();
        }

        this.$emit('on-visible-change', update);

        // v-model
        this.$emit('input', update);
      },
    },
    computed: {

      // 不同位置使用不同动画
      transition () {
        return ['bottom-start', 'bottom', 'bottom-end'].indexOf(this.placement) > -1 ? 'slide-up' : 'fade';
      },
      wrapperClasses() {
        return [
          `${classPrefix}`,
          `${classPrefix}-placement-${this.placement}`,
        ];
      },
    },

    methods: {
      handleClick() {
        let { trigger } = this;

        if (trigger === 'custom' || trigger !== 'click') {
          return false;
        }

        this.show = !this.show;
      },

      handleMouseEnter() {
        let { trigger } = this;

        if (trigger === 'custom' || trigger !== 'hover') {
          return false;
        }

        if (this.timer) {
          clearTimeout(this.timer);
        }

        this.timer = setTimeout(() => {
          this.show = true;
        }, 150);
      },

      handleMouseLeave() {
        let { trigger } = this;

        if (trigger === 'custom' || trigger !== 'hover') {
          return false;
        }

        if (this.timer) {
          clearTimeout(this.timer);

          this.timer = setTimeout(() => {
            this.show = false;
          }, 150);
        }
      },

      handleFocus() {

      },

      handleBlur() {

      },

      handleClose() {
        if (this.trigger === 'custom') {
          return false;
        }

        if (this.trigger !== 'click') {
          return false;
        }
        this.show = false;
      },

      hasParent() {
        const $parent = findComponentUpward(this, 'bc-dropdown');

        if ($parent) {
          return $parent;
        } else {
          return false;
        }
      },

    },
    created() {
      this.$on('on-update-popper', this.update);
      this.$on('on-destroy-popper', this.destroy);
    },
    mounted() {
      this.$on('on-click', (key) => {
        const $parent = this.hasParent();
        if ($parent) {
          $parent.$emit('on-click', key);
        }
      });
      this.$on('on-hover-click', () => {
        const $parent = this.hasParent();

        if ($parent) {
          this.$nextTick(() => {
            if (this.trigger === 'custom') {
              return false;
            }

            this.show = false;
          });
          $parent.$emit('on-hover-click');
        } else {
          this.$nextTick(() => {
            if (this.trigger === 'custom') {
              return false;
            }

            this.show = false;
          });
        }
      });
      this.$on('on-child-click', () => {
        this.$nextTick(() => {
          if (this.trigger === 'custom') {
            return false;
          }

          this.show = true;
        });

        const $parent = this.hasParent();

        if ($parent) {
          $parent.$emit('on-child-click');
        }
      });
    },
  };
</script>
