<template>
  <span :class="siwtchClasses"
        @click="toggle"
        :style="customColor">
    <span :class="innerClasses">
      <slot name="open"
            v-if="checked"></slot>
      <slot name="close"
            v-if="!checked"></slot>
    </span>
  </span>
</template>

<script>
  import { oneOf } from '../../utils/tools';
  import emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-switch';

  export default {
    name: 'bc-switch',
    mixins: [ emitter ],
    props: {
      value: {
        type: [String, Boolean, Number],
        default: false,
      },
      checkedValue: {
        type: [String, Boolean, Number],
        default: true,
      },
      uncheckedValue: {
        type: [String, Boolean, Number],
        default: false,
      },
      checkedColor: {
        type: String,
      },
      uncheckedColor: {
        type: String,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      size: {
        validator (value) {
          return oneOf(value, ['large', 'small']);
        },
      },
    },
    data () {
      return {
        currentValue: this.value,
      };
    },
    computed: {
      siwtchClasses () {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--checked`]: this.currentValue === this.checkedValue,
            [`${classPrefix}--disabled`]: this.disabled,
            [`${classPrefix}--${this.size}`]: !!this.size,
          },
        ];
      },
      innerClasses () {
        return `${classPrefix}__inner`;
      },
      customColor() {
        let style = null;

        if (this.checkedColor && this.currentValue === this.checkedValue) {
          style = {};
          style.backgroundColor = this.checkedColor;
          style.borderColor = this.checkedColor;
        }

        if (this.uncheckedColor && this.currentValue === this.uncheckedValue) {
          style = {};
          style.backgroundColor = this.uncheckedColor;
          style.borderColor = this.uncheckedColor;
        }

        return style;
      },

      checked() {
        return this.currentValue === this.checkedValue;
      },
    },
    methods: {
      toggle () {
        if (this.disabled) {
          return false;
        }

        const checked = this.currentValue === this.checkedValue ? this.uncheckedValue : this.checkedValue;

        this.currentValue = checked;

        this.$emit('input', checked);
        this.$emit('on-change', checked);
        this.dispatch('bc-form-item', 'on-form-change', checked);
      },
    },
    watch: {
      value (val) {
        this.currentValue = val;
      },
    },
  };
</script>
