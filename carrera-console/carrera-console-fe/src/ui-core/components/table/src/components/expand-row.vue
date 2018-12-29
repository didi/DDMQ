<style >

</style>
<template>
<div >
  HiddenRow
</div>
</template>

<script>
import Vue from 'vue';
export default {
  name: 'bc-expand-row',
  props: {
    expand: Boolean,
    accordion: {
      type: Boolean,
      default: true,
    },
    icon: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {

    };
  },
  methods: {
    updateProps(){
      this.$parent.expandProps = this.$props;
    },
    delProps(){

    },
  },
  beforeCreate(){

  },
  mounted(){
    if (this.$scopedSlots.default){
      this.$props['custom_DOM'] = this.$scopedSlots.default;
    } else if (this.$scopedSlots.td){
      this.$props['td_DOM'] = this.$scopedSlots.td;
    }
    this.$props['_vnode'] = this;
    this.updateProps();
  },
  destroyed(){
    this.delProps();
  },
};
</script>
