<style lang="less" scoped>
.app {
  height: 100%;
}
.layout {
  display: flex;
  flex: auto;
  flex-direction: column;
  height: 100%;
  overflow: hidden;

  &-has-sidebar {
    flex-direction: row;
  }

  &-content {
    height: 100%;
    overflow: auto;
    padding: 0px 40px 50px 40px;
  }
}
</style>

<template>
  <div id="app" class="layout layout-has-sidebar">
    <sidebar></sidebar>
    <div class="layout">
      <div class="layout-content">
        <router-view></router-view>
      </div>
    </div>
  </div>

</template>

<script>
  import Sidebar from './components/sidebar/index.vue';

  export default {
    name: 'app',
    data () {
      return {
      };
    },

    components: { Sidebar },

    methods: {
    }
  };
</script>
