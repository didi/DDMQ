<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
  import {
    oneOf,
  } from '../../utils/tools';

  const classPrefix = 'bcui-button-group';

  export default {
    name: 'bc-button-group',
    props: {
      size: {
        validator(value) {
          return oneOf(value, ['small', 'large']);
        },
      },
      shape: {
        validator(value) {
          return oneOf(value, ['circle', 'circle-outline']);
        },
      },
      vertical: {
        type: Boolean,
        default: false,
      },
    },
    components: {},
    data() {
      return {
        classPrefix,
      };
    },
    computed: {
      classes() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--${this.size}`]: !!this.size,
            [`${classPrefix}--${this.shape}`]: !!this.shape,
            [`${classPrefix}--vertical`]: this.vertical,
          },
        ];
      },
    },
    created() {},
    methods: {},
    mounted() {},
  };
</script>
