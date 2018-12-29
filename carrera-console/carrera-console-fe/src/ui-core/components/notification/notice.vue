<template>
  <transition :name="transitionName">
    <div :class="classes"
         :style="styles"
         ref="content">
      <div :class="contentClasses">
        <div :class="classPrefix + '__custom-content'"
             v-html="content"></div>
        <render-cell :render="renderFn"
                     v-if="renderFn"></render-cell>
        <a :class="[baseClass + '-close']"
           @click="close"
           v-if="closable">
          <i class="bcui-icon bcui-icon-times"></i>
        </a>
      </div>

    </div>
  </transition>
</template>

<script>
  import RenderCell from '../render-cell';

  export default {
    components: {
      RenderCell,
    },
    props: {
      classPrefix: {
        type: String,
      },
      duration: {
        type: Number,
        default: 1.5,
      },
      content: {
        type: String,
        default: '',
      },
      styles: {
        type: Object,
        default: function() {
          return {
            right: '50%',
          };
        },
      },
      closable: {
        type: Boolean,
        default: false,
      },
      className: {
        type: String,
      },
      name: {
        type: String,
        required: true,
      },
      render: {
        type: Function,
      },
      onClose: {
        type: Function,
      },
      transitionName: {
        type: String,
      },
    },
    data() {
      return {
        withDesc: false,
      };
    },
    computed: {
      baseClass() {
        return `${this.classPrefix}`;
      },
      classes() {
        return [
          this.baseClass,
          {
            [`${this.className}`]: !!this.className,
            [`${this.baseClass}--closable`]: this.closable,
            [`${this.baseClass}--with-desc`]: this.withDesc,
          },
        ];
      },
      contentClasses() {
        return `${this.baseClass}__content`;
      },
      renderFn() {
        return this.render || function() {};
      },
    },
    methods: {
      clearCloseTimer() {
        if (this.closeTimer) {
          clearTimeout(this.closeTimer);
          this.closeTimer = null;
        }
      },
      close() {
        this.clearCloseTimer();
        this.onClose();
        this.$parent.close(this.name);
      },
    },
    mounted() {
      this.clearCloseTimer();

      if (this.duration !== 0) {
        this.closeTimer = setTimeout(() => {
          this.close();
        }, this.duration);
      }

      // check if with desc in Notice component
      if (this.classPrefix === 'bcui-notice') {
        this.withDesc =
          this.$refs.content.querySelectorAll(`.${this.classPrefix}__desc`)[0].innerHTML !== '';
      }
    },
    beforeDestroy() {
      this.clearCloseTimer();
    },
  };
</script>
