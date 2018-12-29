<template>
  <transition name="fade">
    <div :class="tagClasses" :style="style.container && style.container">
      <span :class="dotClasses" v-if="showDot" :style="style.dot && style.dot"></span>
      <span :class="textClasses">
        <slot></slot>
      </span>
      <bc-icon v-if="closable" type="times" @click.native.stop="close"></bc-icon>
    </div>
  </transition>
</template>
<script>
  import Icon from '../icon';
  import {
    oneOf,
  } from '../../utils/tools';

  const classPrefix = 'bcui-tag';

  export default {
    name: 'bc-tag',
    props: {
      closable: {
        type: Boolean,
        default: false,
      },
      color: {
        type: String,
      },
      type: {
        validator(value) {
          return oneOf(value, ['border', 'dot']);
        },
      },
      name: {
        type: [String, Number],
      },
    },
    components: {
      'bc-icon': Icon,
    },
    data() {
      return {
        classPrefix,
      };
    },
    computed: {
      style() {
        let color = this.color;
        if (!color) {
          return false;
        }
        if (color.indexOf('#') == 0 || !oneOf(this.color, ['primary', 'warning', 'success'])) {
          if (this.type == 'dot') {
            return {
              dot: {
                backgroundColor: color,
              },
              container: {},
            };
          } else if (this.type == 'border') {
            return {
              dot: {},
              container: {
                borderColor: color,
                color: color,
              },
            };
          } else {
            return {
              dot: {},
              container: {
                borderColor: color,
                backgroundColor: color,
                color: '#fff',
              },
            };
          }
        } else {
          return {};
        }
      },
      tagClasses() {
        return [`${classPrefix}`, {
          [`${classPrefix}--${this.color}`]: !!this.color && oneOf(this.color, ['primary', 'warning', 'success']),
          [`${classPrefix}-${this.type}`]: !!this.type,
          [`${classPrefix}--closable`]: this.closable,
        }];
      },
      textClasses() {
        return `${classPrefix}-text`;
      },
      dotClasses() {
        return `${classPrefix}-dot__inner`;
      },
      showDot() {
        return !!this.type && this.type === 'dot';
      },
    },
    created() {},
    methods: {
      close(event) {
        if (this.name === undefined) {
          this.$emit('on-close', event);
        } else {
          this.$emit('on-close', event, this.name);
        }
      },
    },
    mounted() {},
  };
</script>
