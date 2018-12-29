<style lang="less">
.sidebar {
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
  width: 197px;
  background: #1b2d4b;
  font-size: 1.4rem;
  color: #dbdee3;
  transition: width 0.3s;
  &-logo {
    position: relative;
    top: 4px;
    // height: 48px;
    // padding-left: 32px;
    // display: flex;
    // align-items: center;
    // transition: width 0.3s;
  }
  &-inner {
    flex: 1 auto;
    overflow: auto;
  }
  &-title {
    cursor: pointer;
  }
}

.sidenav {
  &-common {

    .sidenav-item {
      font-size: 14px;
      height: 42px;
      line-height: 42px;

      a {
        height: 42px;
        line-height: 42px;
        display: block;
        padding: 0px 16px;
        color: #666;
        text-decoration: none;

        &:link {
          color: #666;
        }

        &:hover {
          color: #108ee9;
        }
        &.active {
          color: #108ee9;
          background: fade(#108ee9, 10%);
        }
      }
    }
  }
  &-category {

    &__title {
      font-size: 14px;
      height: 42px;
      line-height: 42px;
      padding: 0 16px;
    }
  }

  &-group {

    &__title {
      color: rgba(0, 0, 0, .43);
      font-size: 12px;
      padding: 10px 20px;
    }
  }

  &-item {
    height: 42px;
    line-height: 42px;

    &__link {
      display: block;
      padding: 0px 34px;
      color: #666;
      text-decoration: none;

      &:link {
        color: #666;
      }

      &:hover {
        color: #108ee9;
      }

      &.active {
        color: #108ee9;
        background: fade(#108ee9, 10%);
      }
    }

    &__title {
      font-size: 14px;
    }

    &__subtitle {
      font-size: 12px;
      opacity: 0.7;
    }
  }
}

.bcui-menu-item,
.bcui-menu-submenu__title {
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  [class^="icon-"],
  [class*=" icon-"] {
    margin-right: 12px;
  }

  &:hover {
    color: #fff;
    background: #265BB0;
  }

  .icon-triangle-bottom {
    display: none;
    margin-right: 0;
    font-size: 10px;
    transform: scale(0.9);
    color: #626F85;
    position: absolute;
    right: 16px;
    width: 10px;
  }
  .bcui-icon {
    display: none;
  }
}

.bcui-menu-submenu--opened {

  .icon-triangle-bottom {
    display: block;
  }

  &:hover .icon-triangle-bottom  {
    color: #fff;
  }

}
.prefix-icon {
  margin-right: 5px;
}

</style>
<template>
  <div :class="classes">
    <div class="sidebar-inner">
      <div class="sidebar-menu">
        <bc-menu :accordion="true"
                 :router="true"
                 mode="vertical"
                 :shrink="shrinkStatus">
            <bc-menu-item name="/"
                          :routeName="[
                            'index',
                          ]">
                        <embed class="sidebar-logo" src="ddmq-logo.svg" width="20" height="20">
                        <span>DDMQ Console</span>
            </bc-menu-item>

            <bc-menu-item name="/topics"
                          :routeName="[
                            'topics','topicDetail', 'topic-group-detail', 'topic-statistic-detail'
                          ]">
                        <i class="icon bcui-icon-topic1 prefix-icon"></i>
                        <span>Topic</span>
            </bc-menu-item>

            <bc-menu-item name="/groups"
                          :routeName="[
                            'groups','consume'
                          ]">
                          <i class="bcui-icon-topic prefix-icon"></i>
                          <span>Consumer Group</span>
                          </bc-menu-item>

            <bc-menu-item name="/subscribes"
                          :routeName="[
                            'subscribes',
                          ]">
                           <i class="icon bcui-icon-shoucang prefix-icon"></i>
                          <span>Subscription</span>
                          </bc-menu-item>
        </bc-menu>
      </div>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'sidebar',
    components: {},
    props: {},
    data () {
      return {
        shrinkStatus: false
      };
    },

    computed: {
      classes () {
        return [
          'sidebar',
          {
            'sidebar--shrink': this.shrinkStatus
          }
        ];
      }
    },

    methods: {
      gotoIndex () {
        this.$router.push({
          name: 'topics'
        });
      }
    },

    created () {
    },
    mounted () {
    }
  };
</script>
