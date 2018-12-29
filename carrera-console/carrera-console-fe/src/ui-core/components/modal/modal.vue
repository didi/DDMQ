<template>
  <span>
    <transition name="fade">
      <div :class="maskClasses"
           v-show="visible"
           @click="mask"></div>
    </transition>
    <div :class="wrapClasses"
         @click="handleWrapClick">
      <transition name="ease">
        <div :class="classes"
             :style="mainStyles"
             v-show="visible">
          <div :class="[classPrefix + '-content']">
            <a :class="[classPrefix + '-close']"
               v-if="closable"
               @click="close">
              <slot name="close">
                <bc-icon type="times"></bc-icon>
              </slot>
            </a>
            <div :class="[classPrefix + '-header']"
                 v-if="showHead">
              <slot name="header">
                <div :class="[classPrefix + '-header-inner']">{{ title }}</div>
              </slot>
            </div>
            <div :class="[classPrefix + '-body']">
              <slot></slot>
            </div>
            <div :class="[classPrefix + '-footer']"
                 v-if="!footerHide">
              <slot name="footer">
                <bc-button @click.native="cancel">{{ cancelText }}</bc-button>
                <bc-button type="primary"
                           :loading="buttonLoading"
                           @click.native="ok">{{ okText }}</bc-button>
              </slot>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </span>
</template>

<script>
  import Icon from '../icon';
  import { Button } from '../button';
  import { getScrollBarSize } from '../../utils/tools';

  const classPrefix = 'bcui-modal';

  export default {
    name: 'bc-modal',
    components: {
      bcIcon: Icon,
      bcButton: Button,
    },
    props: {
      value: {
        type: Boolean,
        default: false,
      },
      closable: {
        type: Boolean,
        default: true,
      },
      maskClosable: {
        type: Boolean,
        default: true,
      },
      title: {
        type: String,
      },
      width: {
        type: [Number, String],
        default: 520,
      },
      okText: {
        type: String,
        default() {
          return 'Submit';
        },
      },
      cancelText: {
        type: String,
        default() {
          return 'Cancel';
        },
      },
      loading: {
        type: Boolean,
        default: false,
      },
      styles: {
        type: Object,
      },
      className: {
        type: String,
      },
      // for instance
      footerHide: {
        type: Boolean,
        default: false,
      },
      scrollable: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        classPrefix,
        wrapShow: false,
        showHead: true,
        buttonLoading: false,
        visible: this.value,
      };
    },
    computed: {
      wrapClasses() {
        return [
          `${classPrefix}-wrap`,
          {
            [`${classPrefix}-hidden`]: !this.wrapShow,
            [`${this.className}`]: !!this.className,
          },
        ];
      },
      maskClasses() {
        return `${classPrefix}-mask`;
      },
      classes() {
        return `${classPrefix}`;
      },
      mainStyles() {
        let style = {};

        const styleWidth = {
          width: `${this.width}px`,
        };

        const customStyle = this.styles ? this.styles : {};

        Object.assign(style, styleWidth, customStyle);

        return style;
      },
    },
    methods: {
      close() {
        this.visible = false;
        this.$emit('input', false);
        this.$emit('on-cancel');
      },
      mask() {
        if (this.maskClosable) {
          this.close();
        }
      },
      handleWrapClick(event) {
        // use indexOf,do not use === ,because bcui-modal-wrap can have other custom className
        const className = event.target.getAttribute('class');
        if (className && className.indexOf(`${classPrefix}-wrap`) > -1) this.mask();
      },
      cancel() {
        this.close();
      },
      ok() {
        if (this.loading) {
          this.buttonLoading = true;
        } else {
          this.visible = false;
          this.$emit('input', false);
        }
        this.$emit('on-ok');
      },
      EscClose(e) {
        if (this.visible && this.closable) {
          if (e.keyCode === 27) {
            this.close();
          }
        }
      },
      checkScrollBar() {
        let fullWindowWidth = window.innerWidth;
        if (!fullWindowWidth) {
          // workaround for missing window.innerWidth in IE8
          const documentElementRect = document.documentElement.getBoundingClientRect();
          fullWindowWidth = documentElementRect.right - Math.abs(documentElementRect.left);
        }
        this.bodyIsOverflowing = document.body.clientWidth < fullWindowWidth;
        if (this.bodyIsOverflowing) {
          this.scrollBarWidth = getScrollBarSize();
        }
      },
      setScrollBar() {
        if (this.bodyIsOverflowing && this.scrollBarWidth !== undefined) {
          document.body.style.paddingRight = `${this.scrollBarWidth}px`;
        }
      },
      resetScrollBar() {
        document.body.style.paddingRight = '';
      },
      addScrollEffect() {
        this.checkScrollBar();
        this.setScrollBar();
        document.body.style.overflow = 'hidden';
      },
      removeScrollEffect() {
        document.body.style.overflow = '';
        this.resetScrollBar();
      },
    },
    mounted() {
      if (this.visible) {
        this.wrapShow = true;
      }

      let showHead = true;

      if (this.$slots.header === undefined && !this.title) {
        showHead = false;
      }

      this.showHead = showHead;

      // ESC close
      document.addEventListener('keydown', this.EscClose);
    },
    beforeDestroy() {
      document.removeEventListener('keydown', this.EscClose);
      this.removeScrollEffect();
    },
    watch: {
      value(val) {
        this.visible = val;
      },
      visible(val) {
        if (val === false) {
          this.buttonLoading = false;
          this.timer = setTimeout(() => {
            this.wrapShow = false;
            this.removeScrollEffect();
          }, 300);
        } else {
          if (this.timer) clearTimeout(this.timer);
          this.wrapShow = true;
          if (!this.scrollable) {
            this.addScrollEffect();
          }
        }
      },
      loading(val) {
        if (!val) {
          this.buttonLoading = false;
        }
      },
      scrollable(val) {
        if (!val) {
          this.addScrollEffect();
        } else {
          this.removeScrollEffect();
        }
      },
    },
  };
</script>
