<style lang="less">
  @import "./index.less";
</style>

<template>
  <div :class="mainClassPrefix">
    <transition name="drawer-layer">
      <div :class="layerClassPrefix" v-show="visible" @click="handleLayerClick"></div>
    </transition>

    <transition name="drawer-slide">
      <div :class="innerClassPrefix" v-show="visible" @click="handleInnerClick" :style="innerStyles">
        <div class="bc-drawer-content">
          <a :class="[classPrefix + '-close']" v-if="closable" @click="close">
            <slot name="close">
              <bc-icon type="close"></bc-icon>
            </slot>
          </a>
          <div :class="[classPrefix + '-header']"  v-if="showHead">
            <slot name="header">
              <div :class="[classPrefix + '-header__inner']">{{ title }}</div>
            </slot>
          </div>
          <div :class="[classPrefix + '-body']"><slot></slot></div>
          <!-- <div :class="[classPrefix + '-footer']"  v-if="showFoot"><slot></slot></div> -->
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
  const classPrefix = 'bc-drawer';

  export default {
    name: 'bc-drawer',
    components: {},
    props: {
      value: {
        type: Boolean,
        default: false,
      },
      title: {
        type: String,
        default: '',
      },
      closable: {
        type: Boolean,
        default: true,
      },
      maskClosable: {
        type: Boolean,
        default: true,
      },
      loading: {
        type: Boolean,
        default: false,
      },
      width: {
        type: Number,
        default: 0,
      },
      offsetTop: {
        type: Number,
        default: 0,
      },
    },
    data() {
      return {
        classPrefix,
        showHead: true,
        visible: this.value,
      };
    },
    watch: {
      value(update) {
        this.visible = update;
      },
      visible(update) {
        // if (update === false) {
        //   this.buttonLoading = false;
        //   this.timer = setTimeout(() => {
        //     this.removeScrollEffect();
        //   }, 300);
        // } else {

        //   if (this.timer) {
        //     clearTimeout(this.timer);
        //   }

        //   if (!this.scrollable) {
        //     this.addScrollEffect();
        //   }
        // }
      },
    },
    computed: {
      mainClassPrefix() {
        return [
          classPrefix,
          {
            [`${this.customClass}`]: !!this.customClass,
          },
        ];
      },
      layerClassPrefix() {
        return `${classPrefix}-layer`;
      },
      innerClassPrefix() {
        return `${classPrefix}__inner`;
      },

      innerStyles() {
        let style = {};

        if (this.width) {
          style.width = `${this.width}px`;
        }

        if (this.offsetTop) {
          style.top = `${this.offsetTop}px`;
        }

        return style;
      },
    },
    methods: {
      close() {
        this.visible = false;

        // 修改 v-model 绑定的值
        this.$emit('input', false);
        this.$emit('on-hide');
      },

      escClose(e) {
        if (this.visible && this.closable) {
          if (e.keyCode === 27) {
            this.close();
          }
        }
      },

      handleLayerClick() {
        if (this.maskClosable) {
          this.close();
        }
      },
      handleInnerClick() {},

    },
    mounted() {
      let showHead = true;

      if (this.$slots.header === undefined && !this.title) {
        showHead = false;
      }

      this.showHead = showHead;
      // ESC close
      document.addEventListener('keydown', this.escClose);
    },
    beforeDestroy() {
      document.removeEventListener('keydown', this.escClose);
    },
  };
</script>