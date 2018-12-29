<template>
  <div :class="classPrefix" v-show="show">
    <slot></slot>
  </div>
</template>
<script>
  const classPrefix = 'bcui-tab-pane';
  export default {
    name: 'bc-tab-pane',
    props: {
      name: {
        type: String,
      },
      label: {
        type: String,
        default: '',
      },
      icon: {
        type: String,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      closable: {
        type: Boolean,
        default: null,
      },
    },
    data() {
      return {
        classPrefix,
        show: false,
        currentName: this.name,
      };
    },
    watch: {
      name (val) {
        this.currentName = val;
        this.updateNav();
      },
      label () {
        this.updateNav();
      },
      icon () {
        this.updateNav();
      },
      disabled () {
        this.updateNav();
      },
    },
    components: {

    },
    created() {

    },
    mounted() {

    },
    methods: {
      updateNav () {
        this.$parent.initNavList();
      },
    },
    destroyed () {
      this.updateNav();
    },
  };
</script>
