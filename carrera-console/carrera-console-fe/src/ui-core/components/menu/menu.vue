<template>
  <ul :class="menuClasses"
      :style="styles">
    <slot></slot>
  </ul>
</template>
<script>
  import { oneOf, findComponentsDownward, findComponentUpward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  const classPrefix = 'bcui-menu';

  export default {
    name: 'bc-menu',
    mixins: [Emitter],
    props: {
      mode: {
        validator(value) {
          return oneOf(value, ['horizontal', 'vertical']);
        },
      },
      accordion: {
        type: Boolean,
        default: false,
      },
      width: {
        type: String,
      },
      activeName: {
        type: String,
      },
      openedNames: {
        type: Array,
        default() {
          return [];
        },
      },
      router: Boolean,
    },
    components: {},
    data() {
      return {
        items: {},
        submenus: {},
        currentName: this.activeName,
        openedMenus: this.openedNames ? this.openedNames.slice(0) : [],
      };
    },
    watch: {
      activeName(val) {
        const item = this.items[val];
        if (item) {
          this.currentName = item.name;
          this.init();
        } else {
          this.currentName = '';
        }
      },
      openedNames(value) {
        this.openedMenus = value;
      },
      $route(update) {
        if (update) {
          this.updateActiveName();
        }
      },
    },
    computed: {
      menuClasses() {
        let { mode } = this;

        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--${mode}`]: mode,
          },
        ];
      },
      styles() {
        let style = {};
        if (this.mode === 'vertical') {
          style.width = this.width;
        }
        return style;
      },
    },

    methods: {
      // 点击menu item 更新装填
      updateCurrentName() {
        if (!this.currentName) {
          this.currentName = -1;
        }

        // 每次点击menuitem时，
        this.broadcast('bc-submenu', 'on-update-active-name', {
          name: this.currentName,
          ative: false,
        });
        this.broadcast('bc-menu-item', 'on-update-active-name', this.currentName);
      },

      // 更新submenu中展开的keys
      updateOpenKeys(name, namepath) {
        let openedMenus = this.openedMenus;
        const index = openedMenus.indexOf(name);

        if (index > -1) {
          openedMenus.splice(index, 1);
        } else {
          this.openedMenus.push(name);
        }
      },

      updateOpened() {
        const items = findComponentsDownward(this, 'bc-submenu');
        if (items.length) {
          items.forEach((item) => {
            if (this.openedNames.indexOf(item.name) > -1) {
              item.opened = true;
            }
          });
        }
      },

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
      init() {
        const name = this.currentName;
        const activeItem = this.items[name];
        if (!activeItem || this.mode === 'horizontal') {
          return;
        }
        let path = activeItem.path;

        // 展开该菜单项的路径上所有子菜单
        if (path) {
          path.forEach((name) => {
            let submenu = this.submenus[name];
            submenu && this.openMenu(name, submenu.path);
          });
        }
      },

      openMenu(name, path) {
        let openedMenus = this.openedMenus;
        if (openedMenus.indexOf(name) !== -1) {
          return;
        }

        // 将不在该菜单路径下的其余菜单收起
        if (this.accordion) {
          this.openedMenus = openedMenus.filter((name) => {
            return path.indexOf(name) !== -1;
          });
        }

        this.openedMenus.push(name);
      },
      closeMenu(name, path) {
        this.openedMenus.splice(this.openedMenus.indexOf(name), 1);
      },
      handleSubmenuClick(submenu) {
        const { name, path } = submenu;
        let isOpened = this.openedMenus.indexOf(name) !== -1;
        if (isOpened) {
          this.closeMenu(name, path);
          this.$emit('close', name, path);
        } else {
          this.openMenu(name, path);
          this.$emit('open', name, path);
        }
      },
      handleItemClick(item) {
        let { name, path } = item;

        this.currentName = item.name;

        // TODO: 暴露的组件接口
        if (this.mode === 'horizontal') {
          this.openedMenus = [];
        }

        if (this.router) {
          this.routeToItem(item);
        }
      },

      updateActiveName() {
        if (this.$router) {
          const items = findComponentsDownward(this, 'bc-menu-item');

          items.forEach((item) => {
            if (item.routeName.includes(this.$route.name)) {
              const submenu = findComponentUpward(item, 'bc-submenu');
              this.currentName = item.name;

              if (submenu) {
                this.openMenu(submenu.name, submenu.path);
              }
            }
          });
        }
      },

      // router push
      routeToItem(item) {
        let route = item.route || item.name;

        try {
          this.$router.push(route);
        } catch (e) {
          console.error(e);
        }
      },
    },
    created() {
      this.updateActiveName();
    },
    mounted() {
      this.init();
      this.updateActiveName();
      this.$on('on-item-click', this.handleItemClick);
      this.$on('on-submenu-click', this.handleSubmenuClick);
    },
    beforeDestroy() {},
  };
</script>
