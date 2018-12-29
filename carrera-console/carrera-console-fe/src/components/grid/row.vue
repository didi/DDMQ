<style lang="less">

</style>

<template>
  <div :class="rowClasses" :style="rowStyles">
    <slot></slot>
  </div>
</template>

<script>
  // Find components downward
  function findComponentsDownward (context, componentName) {
    return context.$children.reduce((components, child) => {
      if (child.$options.name === componentName) components.push(child);
      const foundChilds = findComponentsDownward(child, componentName);
      return components.concat(foundChilds);
    }, []);
  }

  // Find components upward
  // function findComponentsUpward (context, componentName) {
  //   let parents = [];
  //   if (context.$parent) {
  //     if (context.$parent.$options.name === componentName) parents.push(context.$parent);
  //     return parents.concat(findComponentsUpward(context.$parent, componentName));
  //   } else {
  //     return [];
  //   }
  // }

  const classPrefix = 'row';

  export default {
    name: 'Row',
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
        const Cols = findComponentsDownward(this, 'Col');

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
