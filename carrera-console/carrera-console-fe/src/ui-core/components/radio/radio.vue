<template>
  <label :class="wrapClasses">
    <span :class="radioClasses">
      <span :class="innerClasses"></span>
       <input
        type="radio"
        :class="inputClasses"
        :disabled="disabled"
        :checked="currentValue"
        @change="change" />
    </span>
    <span class="bcui-radio__label">
      <slot></slot>
      <template v-if="!$slots.default">{{label}}</template>
    </span>
  </label>
</template>
<script>
  import { findComponentUpward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-radio';

  export default {
    name: 'bc-radio',
    mixins: [ Emitter ],
    props: {
      value: {
        type: Boolean,
        default: false,
      },
      label: {
        type: [String, Number],
      },
      disabled: {
        type: Boolean,
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
    },
    data () {
      return {
        currentValue: this.value,
        group: false,
        parent: findComponentUpward(this, 'bc-radio-group'),
      };
    },
    computed: {
      wrapClasses () {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}-group-item`]: this.group,
            [`${classPrefix}--checked`]: this.currentValue,
            [`${classPrefix}--disabled`]: this.disabled,
          },
        ];
      },
      radioClasses () {
        return [
          `${classPrefix}__input`,
          {
            [`${classPrefix}__input--checked`]: this.currentValue,
            [`${classPrefix}__input--disabled`]: this.disabled,
          },
        ];
      },
      innerClasses () {
        return `${classPrefix}__inner`;
      },
      inputClasses () {
        return `${classPrefix}__original-input`;
      },
    },
    watch: {
      value () {
        this.updateValue();
      },
    },
    methods: {
      change (event) {
        if (this.disabled) {
          return false;
        }

        // const checked = event.target.checked;
        const checked = event.target.checked ? this.uncheckedValue : this.checkedValue;

        this.currentValue = checked;
        this.$emit('input', checked);

        if (this.group && this.label !== undefined) {
          this.parent.change({
            value: this.label,
            checked: this.value,
          });
        }
        if (!this.group) {
          this.$emit('on-change', checked);
          this.dispatch('bc-form-item', 'on-form-change', checked);
        }
      },
      updateValue () {
        this.currentValue = this.value;
      },
    },
    mounted () {
      if (this.parent) {
        this.group = true;
      }
      if (!this.group) {
        this.updateValue();
      } else {
        this.parent.updateValue();
      }
    },
  };
</script>
