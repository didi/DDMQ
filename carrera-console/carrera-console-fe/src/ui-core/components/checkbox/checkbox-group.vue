<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
  import { findComponentsDownward, oneOf } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-checkbox-group';

  export default {
    name: 'bc-checkbox-group',
    mixins: [Emitter],
    props: {
      value: {
        type: Array,
        default () {
          return [];
        }
      },
      size: {
        validator(value) {
          return oneOf(value, ['small', 'large', 'default']);
        }
      }
    },
    data() {
      return {
        currentValue: this.value,
        childrens: [],
      };
    },
    computed: {
      classes() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--${this.size}`]: !!this.size,
          },
        ];
      },
    },
    mounted() {
      this.updateModel(true);
    },
    methods: {
      updateModel(update) {
        const value = this.value;
        this.childrens = findComponentsDownward(this, 'bc-checkbox');
        if (this.childrens) {
          this.childrens.forEach(child => {
            child.model = value;
            if (update) {
              child.currentValue = value.indexOf(child.label) >= 0;
              child.group = true;
            }
          });
        }
      },
      change(data) {
        this.currentValue = data;
        this.$emit('input', data);
        this.$emit('on-change', data);
        this.dispatch('bc-form-item', 'on-form-change', data);
      },
    },
    watch: {
      value() {
        this.updateModel(true);
      },
    },
  };
</script>
