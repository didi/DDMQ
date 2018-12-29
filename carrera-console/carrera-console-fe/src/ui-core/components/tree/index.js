import Tree from './tree.vue';

Tree.install = (Vue) => {
  Vue.component(Tree.name, Tree);
};

export default Tree;
export { Tree };
