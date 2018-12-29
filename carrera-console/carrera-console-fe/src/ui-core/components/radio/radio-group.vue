<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
  import { oneOf, findComponentsDownward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-radio-group';

  export default {
    name: 'bc-radio-group',
    mixins: [ Emitter ],
    props: {
      value: {
        type: [String, Number],
        default: '',
      },
      size: {
        validator (value) {
          return oneOf(value, ['small', 'large']);
        },
      },
      type: {
        validator (value) {
          return oneOf(value, ['button']);
        },
      },
      vertical: {
        type: Boolean,
        default: false,
      },
    },
    data () {
      return {
        currentValue: this.value,
        childrens: [],
      };
    },
    computed: {
      classes () {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--${this.size}`]: !!this.size,
            [`${classPrefix}--vertical`]: this.vertical,
            [`${classPrefix}-${this.type}`]: !!this.type,
          },
        ];
      },
    },
    watch: {
      value () {
        this.updateValue();
      },
    },
    methods: {
      updateValue () {
        const value = this.value;
        this.childrens = findComponentsDownward(this, 'bc-radio');
        if (this.childrens) {
          this.childrens.forEach(child => {
            child.currentValue = value == child.label;
            child.group = true;
          });
        }
      },
      change (data) {
        this.currentValue = data.value;
        this.updateValue();
        this.$emit('input', data.value);
        this.$emit('on-change', data.value);
        this.dispatch('bc-form-item', 'on-form-change', data.value);
      },
    },
    mounted () {
      this.updateValue();
    },
  };
</script>
