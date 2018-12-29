<style>

</style>

<template>
  <div :class="colClasses" :style="colStyles"><slot></slot></div>
</template>

<script>
  import { findComponentUpward } from '../../utils/tools';

  const classPrefix = 'bcui-col';

  export default {
    name: 'BcCol',
    components: {},
    props: {
      offset: {},
      order: {},
      pull: {},
      push: {},
      span: {
        type: Number | String,
      },
      xs: {
        type: Number | Object,
      },
      sm: {
        type: Number | Object,
      },
      md: {
        type: Number | Object,
      },
      lg: {
        type: Number | Object,
      },
      xl: {
        type: Number | Object,
      },
      xxl: {
        type: Number | Object,
      },
    },
    data() {
      return {
        gutter: 0,
      };
    },
    watch: {
    },
    computed: {
      colClasses() {
        let mediaQueryParams = ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].map((size) => {
          let current = this[size];
          let classes = {};

          if (!isNaN(current)) {
            classes[`${classPrefix}-${size}-${current}`] = true;
          } else if (typeof current === 'object') {
            Object.keys(current).map((key) => {
              if (key) {
                classes[`${classPrefix}-${size}-${key}-${current[key]}`] = !!current[key];
              }
            });
          }

          return classes;
        });

        return [
          `${classPrefix}`,
          {
            [`${classPrefix}-${this.span}`]: !!this.span,
            [`${classPrefix}-push-${this.push}`]: !!this.push,
            [`${classPrefix}-pull-${this.pull}`]: !!this.pull,
            [`${classPrefix}-offset-${this.offset}`]: !!this.offset,
          },
          ...mediaQueryParams,
        ];
      },

      colStyles() {
        let style = {};

        if (this.gutter !== 0) {
          style = {
            paddingLeft: this.gutter / 2 + 'px',
            paddingRight: this.gutter / 2 + 'px',
          };
        }

        return style;
      },
    },
    methods: {
      updateGutter () {
        const Row = findComponentUpward(this, 'BcRow');

        if (Row) {
          Row.updateGutter(Row.gutter);
        }
      },
    },
    mounted () {
      this.updateGutter();
    },
    beforeDestroy () {
      this.updateGutter();
    },
  };
</script>
