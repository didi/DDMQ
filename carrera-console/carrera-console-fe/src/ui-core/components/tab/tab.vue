<template>
  <div :class="tabClasses">
    <div :class="[classPrefix + '-header']">
      <div :class="navbarClasses">
        <template v-if="scrollable">
          <div
            :class="['bcui-tab-navbar-prev', scrollable.prev ? '' : 'disabled']"
            @click="scrollPrev">
              <i class="bcui-icon-arrow-left"></i>
            </div>
          <div
            :class="['bcui-tab-navbar-next', scrollable.next ? '' : 'disabled']"
            @click="scrollNext">
            <i class="bcui-icon-arrow-right"></i>
          </div>
        </template>
        <div :class="scrollClasses" ref="navScroll">
          <div :class="[classPrefix + '-navbar__inner']" :style="navStyle" ref="navbar">
            <div :class="activeBarClasses" :style="activeBarStyle"></div>
            <div
              :class="createTabItemClass(item)"
              :key="index"
              v-for="(item, index) in navList"
              @click="handleChange(index)">
              <bc-icon v-if="item.icon !== ''" :type="item.icon"></bc-icon>{{item.label}}
              <bc-icon v-if="showClose(item)" type="close" @click.native.stop="handleRemove(index)"></bc-icon></div>
          </div>
        </div>
      </div>
    </div>

    <div :class="[classPrefix + '-content']">
      <slot></slot>
    </div>
  </div>
</template>
<script>
  import { oneOf, getStyle, findComponentDownward } from '../../utils/tools';

  const classPrefix = 'bcui-tab';

  export default {
    name: 'bc-tab',
    components: {},
    props: {
      value: {
        type: [String, Number],
      },
      type: {
        validator(value) {
          return oneOf(value, ['card']);
        },
        type: String,
      },
      animated: {
        type: Boolean,
        default: true,
      },
      size: {
        type: String,
        default: '',
      },
    },
    data() {
      return {
        classPrefix,
        navList: [],
        activeBarWidth: 0,
        activeBarOffset: 0,
        activeTab: this.value,

        // 滚动相关
        navStyle: {
          transform: '',
        },
        scrollable: false,
      };
    },
    computed: {
      tabClasses() {
        return [`${classPrefix}`, {
          [`${classPrefix}--card`]: this.type === 'card',
          [`${classPrefix}--${this.size}`]: !!this.size,
          [`${classPrefix}--no-animation`]: !this.animated,
        }];
      },

      navbarClasses() {
        let baseClass = classPrefix + '-navbar';

        return [`${baseClass}`, {
          [`${baseClass}--card`]: this.type,
          ['scrolled']: !!this.scrollable,
        }];
      },

      scrollClasses() {
        let baseClass = classPrefix + '-navbar-scroll';
        return [`${baseClass}`, {
        }];
      },

      activeBarClasses() {
        let baseClass = `${classPrefix}-activebar`;
        return [baseClass, {
          [`${baseClass}--animated`]: this.animated,
        }];
      },

      activeBarStyle() {
        let  { type, activeBarWidth, activeBarOffset, animated } = this;
        let style = {
          display: 'none',
          width: `${activeBarWidth}px`,
        };

        if (!type) {
          style.display = 'block';
        }

        if (animated) {
          style.transform = `translate3d(${activeBarOffset}px, 0px, 0px)`;
        }

        return style;
      },
    },
    methods: {

      // 获取子元素中的TabPane
      getTabList() {
        return this.$children.filter((item) => {
          return item.$options.name === 'bc-tab-pane';
        });
      },

      // 初始化tab的标签项
      initNavList() {
        this.navList = [];
        this.paneList = this.getTabList();
        this.paneList.forEach((pane, index) => {
          this.navList.push({
            label: pane.label,
            icon: pane.icon || '',
            name: pane.name || index,
            disabled: pane.disabled,
            closable: pane.closable,
          });
          if (index === 0) {
            if (!this.activeTab) {
              this.activeTab = pane.name || index;
            };
          }
        });

        // 更新滚动条
        this.update();

        // 初始化时，更新一次panes的状态
        this.updatePaneStatus();
        this.updateActiveBarStatus();
      },

      /**
       * 创建tab按钮的class
       * @param  {Object} tab对象
       * @return {Array} class名组成的数组
       */
      createTabItemClass(item) {
        let tabItemBaseClass = `${classPrefix}-navbar__item`;
        return [tabItemBaseClass, {
          [`${tabItemBaseClass}--disabled`]: item.disabled,
          [`${tabItemBaseClass}--active`]: item.name === this.activeTab,
        }];
      },

      /**
       * 点击tab执行的操作，出发绑定的自定义事件 on-tab-click
       * @param  {String|Number} 被点击的tab的index
       * @return {Object}
       */
      handleChange(index) {
        const target = this.navList[index];
        if (target.disabled) {
          return false;
        }
        this.activeTab = target.name;

        this.$emit('input', target.name);
        this.$emit('on-tab-click', target.name);
      },

      handleRemove(index) {
        const tabs = this.getTabList();
        const tab = tabs[index];

        // FIXME: vue2.0不支持组件的自销毁，此处需要修改逻辑，手动维护tabpanes
        tab.$destroy();

        if (tab.name === this.activeTab) {
          const newTabs = this.getTabList();
          let activeTab = -1;
          if (newTabs.length) {
            const leftNoDisabledTabs = tabs.filter((item, itemIndex) => !item.disabled && itemIndex < index);
            const rightNoDisabledTabs = tabs.filter((item, itemIndex) => !item.disabled && itemIndex > index);
            if (rightNoDisabledTabs.length) {
              activeTab = rightNoDisabledTabs[0].currentName;
            } else if (leftNoDisabledTabs.length) {
              activeTab = leftNoDisabledTabs[leftNoDisabledTabs.length - 1].currentName;
            } else {
              activeTab = newTabs[0].currentName;
            }
          }
          this.activeTab = activeTab;
          this.$emit('input', activeTab);
        }

        this.$emit('on-tab-remove', tab.currentName);
        this.initNavList();
      },

      showClose (item) {
        if (this.type === 'card') {
          if (item.closable !== null) {
            return item.closable;
          } else {
            return this.closable;
          }
        } else {
          return false;
        }
      },

      /**
       * 更新panes的状态
       * @return {undefined}
       */
      updatePaneStatus() {
        const tabs = this.getTabList();

        tabs.forEach((item) => {
          item.show = (item.name === this.activeTab);
        });
      },

      /**
       * 修改 tab 底部 activebar的状态
       * @return {[type]} [description]
       */
      updateActiveBarStatus() {
        this.$nextTick(() => {
          // 获取到当前激活的tab的索引
          const index = this.navList.findIndex((nav) => {
            return nav.name === this.activeTab;
          });

          if (!this.$refs.navbar) {
            return;
          }

          const prevTabs = this.$refs.navbar.querySelectorAll(`.${classPrefix}-navbar__item`);
          const tab = prevTabs[index];

          this.activeBarWidth = parseFloat(getStyle(tab, 'width'));

          if (index > 0) {
            let offset = 0;
            const gutter = this.size === 'small' ? 0 : parseFloat(getStyle(tab, 'marginRight'));

            for (let i = 0; i < index; i++) {
              offset += parseFloat(getStyle(prevTabs[i], 'width')) + gutter;
            }
            this.activeBarOffset = offset;
          } else {
            this.activeBarOffset = 0;
          }
        });
      },

      // 滚动相关
      getCurrentScrollOffset() {
        const { navStyle } = this;
        return navStyle.transform
          ? Number(navStyle.transform.match(/translateX\(-(\d+(\.\d+)*)px\)/)[1])
          : 0;
      },

      setOffset(value) {
        this.navStyle.transform = `translateX(-${value}px)`;
      },

      scrollNext() {
        const navWidth = this.$refs.navbar.offsetWidth;
        const containerWidth = this.$refs.navScroll.offsetWidth;
        const currentOffset = this.getCurrentScrollOffset();

        if (navWidth - currentOffset <= containerWidth) {
          return;
        }

        let newOffset = (navWidth - currentOffset > containerWidth * 2)
          ? currentOffset + containerWidth
          : (navWidth - containerWidth);

        this.setOffset(newOffset);
      },

      scrollPrev() {
        const containerWidth = this.$refs.navScroll.offsetWidth;
        const currentOffset = this.getCurrentScrollOffset();

        if (!currentOffset) {
          return;
        }

        let newOffset = currentOffset > containerWidth
          ? currentOffset - containerWidth
          : 0;

        this.setOffset(newOffset);
      },

      // 更新滚动
      update() {
        // 兼容有时组件提前销毁的情况
        if (!this.$refs.navbar || !this.$refs.navScroll) {
          return;
        }

        const navWidth = this.$refs.navbar.offsetWidth;
        const containerWidth = this.$refs.navScroll.offsetWidth;
        const currentOffset = this.getCurrentScrollOffset();

        if (containerWidth < navWidth) {
          const currentOffset = this.getCurrentScrollOffset();

          this.scrollable = this.scrollable || {};
          this.scrollable.prev = currentOffset;
          this.scrollable.next = (currentOffset + containerWidth) < navWidth;

          if (navWidth - currentOffset < containerWidth) {
            this.setOffset(navWidth - containerWidth);
          }
        } else {
          this.scrollable = false;

          if (currentOffset > 0) {
            this.setOffset(0);
          }
        }
      },

    },
    watch: {
      value(update) {
        this.activeTab = update;
      },
      activeTab(value, oldvalue) {
        this.updatePaneStatus();
        this.updateActiveBarStatus();
      },
    },
    updated() {
      this.update();
    },
    mounted() {
      this.initNavList();
    },
  };
</script>
