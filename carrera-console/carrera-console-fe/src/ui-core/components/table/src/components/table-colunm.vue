<style >

</style>
<template>
  <div >HiddenColunm</div>
</template>

<script>
  import Vue from 'vue';

  export default {
    name: 'bc-table-column',
    props: {
      field: String,
      width: String,
      index: String,
      type: String,
      deepCopy: Boolean,
      show: {
        type: Boolean,
        default: true,
      },
      maxWidth: {
        type: String,
        default: '',
      },
      icon: {
        type: String,
      },
      params: [Object],
      label: [String, Function],
      classify: [String, Array],
      sort: [Boolean],
      sortType: {
        type: String,
        default: 'none',
      },
    },
    data() {
      return {};
    },
    methods: {
      updateProps() {
        Vue.set(this.$parent.headerProps, this.$props.field, this.$props);
      },
      delProps() {
        Vue.delete(this.$parent.headerProps, this.$props.field);
      },
    },
    beforeCreate() {},
    mounted() {
      if (this.$scopedSlots.default) {
        this.$props['custom_DOM'] = this.$scopedSlots.default;
      }

      if (this.$slots.label) {
        this.$props['label_DOM'] = this.$slots.label;
      }
      this.$props['_vnode'] = this;
      this.updateProps();
    },
    destroyed() {
      this.delProps();
    },
  };
</script>
