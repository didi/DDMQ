<template>
  <div class="bcui-base-dropdown" :style="styles">
    <slot></slot>
  </div>
</template>
<script>
  import Popper from '../../mixins/popper';
  import { getStyle } from '../../utils/tools';

  export default {
    name: 'bc-base-drop',
    props: {
      placement: {
        type: String,
        default: 'bottom-start',
      },
    },
    components: {
    },
    data() {
      return {
        popper: null,
        width: '',
      };
    },
    computed: {
      styles () {
        let style = {};
        if (this.width) {
          style.width = `${this.width}px`;
        }
        return style;
      },
    },

    methods: {
      update() {
        if (this.popper) {
          this.$nextTick(() => {
            this.popper.update();
          });
        } else {
          this.$nextTick(() => {
            this.popper = new Popper(this.$parent.$refs.reference, this.$el, {
              gpuAcceleration: false,
              placement: this.placement,
              boundariesPadding: 0,
              forceAbsolute: true,
              boundariesElement: 'body',
            }, {

              // 详情：https://github.com/FezVrasta/popper.js/issues/62
              onCreate: (popper) => {
                this.resetTransformOrigin(popper);
              },
            });
          });
        }

        // set a height for parent is Modal and Select's width is 100%
        if (this.$parent.$options.name === 'bc-select') {
          this.width = parseInt(getStyle(this.$parent.$el, 'width'));
        }
      },
      destroy() {
        if (this.popper) {
          this.resetTransformOrigin(this.popper);
          setTimeout(() => {
            this.popper.destroy();
            this.popper = null;
          }, 300);
        }
      },
      resetTransformOrigin(popper) {
        let placementMap = {
          top: 'bottom',
          bottom: 'top',
        };

        // popper._popper -> popper.popper
        let placement = popper.popper.getAttribute('x-placement').split('-')[0];
        let origin = placementMap[placement];
        popper.popper.style.transformOrigin = `center ${ origin }`;
      },
    },
    created() {
      this.$on('on-update-popper', this.update);
      this.$on('on-destroy-popper', this.destroy);
    },
    mounted() {
    },
    beforeDestroy() {
      if (this.popper) {
        this.popper.destroy();
      }
    },
  };
</script>
