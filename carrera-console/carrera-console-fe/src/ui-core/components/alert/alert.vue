<template>
  <transition name="fade">
    <div v-if="!closed" :class="wrapClasses">
      <span :class="iconClasses" v-if="showIcon">
        <slot name="icon"><Icon :type="iconType || defaultIconType"></Icon></slot>
      </span>
      <span :class="messageClasses"><slot></slot></span>
      <span :class="descClasses"><slot name="description"></slot></span>
      <a :class="closeClasses" v-if="closable" @click="close">
        <slot name="close">
          <Icon type="ios-close-empty"></Icon>
        </slot>
      </a>
    </div>
  </transition>
</template>
<script>
  import Icon from '../icon';
  import { oneOf } from '../../utils/tools';

  const classPrefix = 'bcui-alert';

  export default {
    name: 'bc-alert',
    components: { Icon },
    props: {
      type: {
        validator(value) {
          return oneOf(value, ['success', 'info', 'warning', 'error']);
        },
        default: 'info',
      },
      closable: {
        type: Boolean,
        default: false,
      },
      showIcon: {
        type: Boolean,
        default: false,
      },
      banner: {
        type: Boolean,
        default: false,
      },
      iconType: {
        type: String,
      },
    },
    data() {
      return {
        closed: false,
        desc: false,
        description: false,
      };
    },
    computed: {
      wrapClasses() {
        return [
          `${classPrefix}`,
          `${classPrefix}--${this.type}`,
          {
            [`${classPrefix}--with-icon`]: this.showIcon,
            [`${classPrefix}--with-description`]: !!this.description,
            [`${classPrefix}--with-banner`]: this.banner,
          },
        ];
      },
      messageClasses() {
        return `${classPrefix}__message`;
      },
      descClasses() {
        return `${classPrefix}__description`;
      },
      closeClasses() {
        return `${classPrefix}__close`;
      },
      iconClasses() {
        return `${classPrefix}__icon`;
      },
      defaultIconType() {
        let type = '';

        switch (this.type) {
          case 'success':
            type = 'information-success';
            break;
          case 'info':
            type = 'information-circle';
            break;
          case 'warning':
            type = 'information-warning';
            break;
          case 'error':
            type = 'information-fail';
            break;
        }

        return type;
      },
    },
    methods: {
      close(e) {
        this.closed = true;
        this.$emit('on-close', e);
      },
    },
    mounted() {
      this.description = this.$slots.description !== undefined;
    },
  };
</script>
