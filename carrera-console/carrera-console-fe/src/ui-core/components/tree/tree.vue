<template>
  <div :class="classPrefix">
    <tree-node v-for="item in model" :key="item" :model="item">
    </tree-node>
  </div>
</template>

<script>
  import TreeNode from './tree-node.vue';

  const classPrefix = 'bcui-tree';

  export default {
    name: 'tree',
    props: {
      model: {
        type: Array,
      },
    },
    components: {
      TreeNode,
    },
    data() {
      return {
        classPrefix,
      };
    },
    computed: {},
    methods: {},
    mounted() {
      this.$on('on-node-click', (model) => {
        this.$emit('on-click', model);
      });
    },
  };
</script>
