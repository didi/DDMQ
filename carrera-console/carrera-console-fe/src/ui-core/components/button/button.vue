<template>
  <button :type="htmlType" :class="buttonClass" :disabled="disabled" @click="handleClick">
    <bc-icon class="bcui-load-loop" type="loading" v-if="loading"></bc-icon>
    <bc-icon :type="icon" v-if="icon && !loading"></bc-icon>
    <span v-if="showSlot" ref="slot"><slot></slot></span>
  </button>
</template>
<script>
  import { Icon } from '../icon';

  const buttonClassPrefix = 'bcui-button';

  export default {
    name: 'bc-button',
    components: {
      bcIcon: Icon,
    },
    props: {
      type: {
        type: String,
      },
      shape: {
        type: String,
      },
      size: {
        type: String,
      },
      ghost: {
        type: Boolean,
        default: false,
      },
      loading: Boolean,
      disabled: Boolean,
      htmlType: {
        type: String,
        default: 'button',
      },
      icon: String,
      long: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        showSlot: true,
      };
    },
    created() {

    },
    computed: {
      buttonClass() {
        return [`${buttonClassPrefix}`, {
          [`${buttonClassPrefix}--${this.type}`]: !!this.type,
          [`${buttonClassPrefix}--long`]: this.long,
          [`${buttonClassPrefix}--${this.shape}`]: !!this.shape,
          [`${buttonClassPrefix}--${this.size}`]: !!this.size,
          [`${buttonClassPrefix}--ghost`]: !!this.ghost,
          [`${buttonClassPrefix}--loading`]: this.loading != null && this.loading,
          [`${buttonClassPrefix}--icon-only`]: !this.showSlot && (!!this.icon || this.loading),
        }];
      },
    },
    methods: {
      handleClick(event) {
        this.$emit('click', event);
      },
    },
    mounted() {
      this.showSlot = this.$slots.default !== undefined;
    },
  };
</script>
