<template>
  <li :class="submenuClasses"
      @mouseenter="handleMouseenter"
      @mouseleave="handleMouseleave">
    <div class="bcui-menu-submenu__title"
         ref="reference"
         @click="handleClick">
      <slot name="title"></slot>
      <bc-icon type="triangle-bottom"
               :class="['bcui-menu-submenu__expand-icon']"></bc-icon>
    </div>
    <transition name="slide">
      <ul :class="[classPrefix]"
          v-show="opened">
        <slot></slot>
      </ul>
    </transition>
  </li>
</template>
<script>
  import baseDropdown from '../dropdown/dropdown.vue';
  import { findComponentUpward, getStyle } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-menu';

  export default {
    name: 'bc-submenu',
    mixins: [Emitter],
    props: {
      name: {
        type: String,
        required: true,
      },
      trigger: {
        type: String,
        default: 'click',
      },
    },
    components: {
      baseDropdown,
    },
    data() {
      return {
        classPrefix,
        items: {},
        submenus: {},
        rootMenu: findComponentUpward(this, 'bc-menu'),
        parentMenu: findComponentUpward(this, ['bc-menu', 'bc-submenu']),
        dropWidth: parseFloat(getStyle(this.$el, 'width')),
      };
    },
    computed: {
      submenuClasses() {
        let basePrefix = `${classPrefix}-submenu`;
        return [
          basePrefix,
          {
            [`${basePrefix}--active`]: this.active,
            [`${basePrefix}--opened`]: this.opened,
            [`${basePrefix}--disabled`]: this.disabled,
          },
        ];
      },
      submenuDropClasses() {
        return [`${classPrefix}-submenu-dropdown`];
      },
      dropStyle() {
        let style = {};
        if (this.dropWidth) {
          style.minWidth = `${this.dropWidth}px`;
        }
        return style;
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
      mode() {
        return this.rootMenu.mode;
      },
      opened() {
        return this.rootMenu.openedMenus.indexOf(this.name) > -1;
      },
      active: {
        cache: false,
        get() {
          let isActive = false;
          const submenus = this.submenus;
          const items = this.items;
          Object.keys(items).forEach((name) => {
            if (items[name].active) {
              isActive = true;
            }
          });
          Object.keys(submenus).forEach((name) => {
            if (submenus[name].active) {
              isActive = true;
            }
          });
          return isActive;
        },
      },
    },
    methods: {
      addItem(item) {
        this.$set(this.items, item.name, item);
      },
      removeItem(item) {
        delete this.items[item.name];
      },
      addSubmenu(item) {
        this.$set(this.submenus, item.name, item);
      },
      removeSubmenu(item) {
        delete this.submenus[item.name];
      },
      handleClick() {
        if (this.disabled || this.trigger !== 'click') {
          return;
        }
        this.dispatch('bc-menu', 'on-submenu-click', this);
      },
      handleMouseenter() {
        if (this.disabled || this.trigger === 'click') {
          return;
        }
        // if (this.mode === 'vertical') {
        //   return;
        // }
        clearTimeout(this.timeout);

        this.timeout = setTimeout(() => {
          this.rootMenu.openMenu(this.name, this.path);
        }, 300);
      },
      handleMouseleave() {
        if (this.disabled || this.trigger === 'click') {
          return;
        }
        // if (this.mode === 'vertical') {
        //   return;
        // }
        clearTimeout(this.timeout);
        this.timeout = setTimeout(() => {
          this.rootMenu.closeMenu(this.name, this.path);
        }, 300);
      },
    },
    created() {
      this.parentMenu.addSubmenu(this);
      this.rootMenu.addSubmenu(this);
    },

    mounted() {},
    beforeDestroy() {
      this.parentMenu.removeSubmenu(this);
      this.rootMenu.removeSubmenu(this);
    },
  };
</script>
