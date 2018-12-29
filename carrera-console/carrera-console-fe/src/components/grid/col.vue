<style>

</style>

<template>
  <div :class="colClasses" :style="colStyles"><slot></slot></div>
</template>

<script>
  const classPrefix = 'col';

  function findComponentUpward (context, componentName, componentNames) {
    if (typeof componentName === 'string') {
      componentNames = [componentName];
    } else {
      componentNames = componentName;
    }

    let parent = context.$parent;
    let name = parent.$options.name;
    while (parent && (!name || componentNames.indexOf(name) < 0)) {
      parent = parent.$parent;
      if (parent) name = parent.$options.name;
    }
    return parent;
  }

  // // Find component downward
  // function findComponentDownward (context, componentName) {
  //   const childrens = context.$children;
  //   let children = null;

  //   if (childrens.length) {
  //     for (const child of childrens) {
  //       const name = child.$options.name;
  //       if (name === componentName) {
  //         children = child;
  //         break;
  //       } else {
  //         children = findComponentDownward(child, componentName);
  //         if (children) break;
  //       }
  //     }
  //   }
  //   return children;
  // }

  export default {
    name: 'Col',
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
        const Row = findComponentUpward(this, 'Row');

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
