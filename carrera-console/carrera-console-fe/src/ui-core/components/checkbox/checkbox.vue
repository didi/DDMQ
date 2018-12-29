
<template>
  <label :class="wrapClasses">
    <span :class="checkboxClasses">
      <span :class="innerClasses"></span>
      <input v-if="group" type="checkbox" :class="inputClasses" :disabled="disabled" :value="label" v-model="model" :name="name"
        @change="change">
      <input v-if="!group" type="checkbox" :class="inputClasses" :disabled="disabled" :checked="currentValue" :name="name" @change="change">
    </span>
    <span class="bcui-checkbox__label">
      <slot>
        <span v-if="showSlot">{{ label }}</span>
      </slot>
    </span>
  </label>
</template>
<script>
  import { findComponentUpward, oneOf } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-checkbox';

  export default {
    name: 'bc-checkbox',
    mixins: [Emitter],
    props: {
      disabled: {
        type: Boolean,
        default: false,
      },
      value: {
        type: [String, Number, Boolean],
        default: false,
      },
      trueValue: {
        type: [String, Number, Boolean],
        default: true,
      },
      falseValue: {
        type: [String, Number, Boolean],
        default: false,
      },
      label: {
        type: [String, Number, Boolean]
      },
      indeterminate: {
        type: Boolean,
        default: false,
      },
      size: {
        validator(value) {
          return oneOf(value, ['small', 'large', 'default']);
        }
      },
      name: {
        type: String,
      }
    },
    data() {
      return {
        model: [],
        currentValue: this.value,
        group: false,
        showSlot: true,
        parent: findComponentUpward(this, 'bc-checkbox-group'),
      };
    },
    computed: {
      wrapClasses() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}-group-item`]: this.group,
            [`${classPrefix}--checked`]: this.currentValue,
            [`${classPrefix}--disabled`]: this.disabled,
            [`${classPrefix}--${this.size}`]: !!this.size,
          }
        ];
      },
      checkboxClasses() {
        return [
          `${classPrefix}__input`,
          {
            [`${classPrefix}__input--checked`]: this.currentValue,
            [`${classPrefix}__input--disabled`]: this.disabled,
            [`${classPrefix}__input--indeterminate`]: this.indeterminate,
          }
        ];
      },
      innerClasses() {
        return `${classPrefix}__inner`;
      },
      inputClasses() {
        return `${classPrefix}__original-input`;
      },
    },
    watch: {
      value(val) {
        if (val !== this.trueValue && val !== this.falseValue) {
          throw 'Value should be trueValue or falseValue.';
        }
        this.updateModel();
      },
    },
    methods: {
      change(event) {
        if (this.disabled) {
          return false;
        }
        const checked = event.target.checked;

        this.currentValue = checked;

        let value = checked ? this.trueValue : this.falseValue;

        this.$emit('input', value);

        if (this.group) {
          this.parent.change(this.model);
        } else {
          this.$emit('on-change', value);
          this.dispatch('bc-form-item', 'on-form-change', value);
        }
      },
      updateModel() {
        this.currentValue = this.value === this.trueValue;
      }
    },
    mounted() {
      this.parent = findComponentUpward(this, 'bc-checkbox-group');

      if (this.parent) {
        this.group = true;
      }

      if (!this.group) {
        this.updateModel();
        this.showSlot = this.$slots.default !== undefined;
      } else {
        this.parent.updateModel(true);
      }
    },

  };

</script>
