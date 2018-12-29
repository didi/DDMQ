<style lang="less">

</style>

<template>
  <div :class="rowClasses" :style="rowStyles">
    <slot></slot>
  </div>
</template>

<script>
  import { findComponentsDownward } from '../../utils/tools';

  const classPrefix = 'bcui-row';

  export default {
    name: 'BcRow',
    components: {},
    props: {
      align: {},
      gutter: {
        type: Number,
        default: 0,
      },
      justify: {},
      type: {},
    },
    data() {
      return {

      };
    },
    watch: {
      gutter (val) {
        this.updateGutter(val);
      },
    },
    computed: {
      rowClasses() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}-${this.type}`]: !!this.type,
            [`${classPrefix}-${this.type}-${this.align}`]: !!this.align,
            [`${classPrefix}-${this.type}-${this.justify}`]: !!this.justify,
            [`${this.className}`]: !!this.className,
          },
        ];
      },

      rowStyles () {
        let style = {};

        if (this.gutter !== 0) {
          style = {
            marginLeft: this.gutter / -2 + 'px',
            marginRight: this.gutter / -2 + 'px',
          };
        }

        return style;
      },
    },
    methods: {
      updateGutter (val) {
        const Cols = findComponentsDownward(this, 'BcCol');

        if (Cols.length) {
          Cols.forEach((child) => {
            if (val !== 0) {
              child.gutter = val;
            }
          });
        }
      },
    },
    mounted() {
    },
  };
</script>
