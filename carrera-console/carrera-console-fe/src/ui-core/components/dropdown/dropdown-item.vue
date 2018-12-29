<style>

</style>

<template>
  <li :class="itemClasses" @click="handleClick"><slot></slot></li>
</template>

<script>
  import { findComponentUpward } from '../../utils/tools';

  const classPrefix = 'bcui-dropdown-item';

  export default {
    name: 'bc-dropdown-item',
    components: {},
    props: {
      name: {
        type: [String, Number]
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      selected: {
        type: Boolean,
        default: false,
      },
      divided: {
        type: Boolean,
        default: false,
      }
    },
    data() {
      return {};
    },
    watch: {
    },
    computed: {
      itemClasses() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--disabled`]: this.disabled,
            [`${classPrefix}--selected`]: this.selected,
            [`${classPrefix}--divided`]: this.divided,
          },
        ];
      },
    },
    methods: {
      handleClick() {
        const $parent = findComponentUpward(this, 'bc-dropdown');
        const hasChildren = this.$parent && this.$parent.$options.name === 'bc-dropdown';

        if (this.disabled) {
          this.$nextTick(() => {
            $parent.currentVisible = true;
          });
        } else if (hasChildren) {
          this.$parent.$emit('on-child-click');
        } else {
          if ($parent && $parent.$options.name === 'bc-dropdown') {
            $parent.$emit('on-hover-click');
          }
        }
        $parent.$emit('on-click', this.name);
        this.$emit('on-click', this.name);
      },
    },
    mounted() {
    },
  };
</script>
