<template>
  <li :class="menuItemClasses"
      @click.stop="handleMenuItemClick">
    <slot></slot>
  </li>
</template>
<script>
  import { findComponentUpward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-menu';

  export default {
    name: 'bc-menu-item',
    mixins: [Emitter],
    props: {
      name: {
        type: [String, Number],
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      route: {
        type: Object,
      },
      routeName: {
        type: Array,
        default: () => {
          return [];
        },
      },
    },
    components: {},
    data() {
      return {
        rootMenu: findComponentUpward(this, 'bc-menu'),
        parentMenu: findComponentUpward(this, ['bc-menu', 'bc-submenu']),
      };
    },
    computed: {
      menuItemClasses() {
        let basePrefix = `${classPrefix}-item`;
        return [
          basePrefix,
          {
            [`${basePrefix}--active`]: this.active,
          },
        ];
      },
      menuItemLinkClasses() {
        return [`${classPrefix}-item__link`];
      },
      active() {
        return this.name === this.rootMenu.currentName;
      },
      path() {
        let path = [this.name];
        let parent = this.$parent;
        while (parent.$options.name !== 'bc-menu') {
          if (parent.name) {
            path.unshift(parent.name);
          }
          parent = parent.$parent;
        }
        return path;
      },
    },

    methods: {
      handleMenuItemClick() {
        this.dispatch('bc-menu', 'on-item-click', this);
        this.$emit('click', this);
      },
    },
    created() {
      this.parentMenu.addItem(this);
      this.rootMenu.addItem(this);
    },
    mounted() {
      // this.$on('on-update-active-name', (name) => {
      //   if (this.name === name) {
      //     this.active = true;
      //     this.dispatch('bc-submenu', 'on-update-active-name', true);
      //   } else {
      //     this.active = false;
      //   }
      // });
    },
    beforeDestroy() {
      this.parentMenu.removeItem(this);
      this.rootMenu.removeItem(this);
    },
  };
</script>
