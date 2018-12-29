<template>
  <ul :class="classPrefix">
    <li>
      <div :class="{bold: isParent}">
        <span @click="toggle" :class="expandArrowClasses" v-if="model.isParent || model.depth == 0">
          <icon type="arrow-right"></icon>
        </span>
        <span v-if="model.depth == 0">
          <icon type="leftroot"></icon>
        </span>
        <span v-if="model.depth > 0 && !model.isParent">
          <icon type="node" ></icon>
        </span>
        <span @click="handleNodeClick(model)" :class="nodeNameClasses">{{model.name}}</span>
        <!-- <span v-if="isParent">[{{open ? '-' : '+'}}]</span> -->
      </div>
      <transition name="move">
        <ul v-show="open" :class="classPrefix" v-if="isParent">
          <bc-tree-node v-for="model in model.children" :key="model" :model="model">
          </bc-tree-node>
          <!--<li class="add" @click="addChild">+</li>-->
        </ul>
      </transition>
    </li>
  </ul>
</template>

<script>
  import Emitter from '../../mixins/emitter';
  import Icon from '../icon';
  const classPrefix = 'bcui-treenode';
  export default {
    name: 'bc-tree-node',
    props: {
      model: Object,
    },
    mixins: [Emitter],
    components: {
      Icon,
    },
    data() {
      return {
        // open: false,
        open: this.model.open,
        classPrefix,
      };
    },
    computed: {
      isParent() {
        return this.model.children &&
          this.model.children.length;
      },
      expandArrowClasses() {
        let base = `${classPrefix}-arrow`;
        return [base, {
          [`${base}--open`]: !!this.open,
        }];
      },
      nodeNameClasses() {
        let base = `${classPrefix}-name`;
        return [base, {
          // [`${base}--open`]: !!this.open,
        }];
      },
    },
    methods: {
      toggle: function() {
        if (this.isParent) {
          this.open = !this.open;
          // TODO: 遍历收起所有的子树
        }
      },
      changeType: function() {
        if (!this.isParent) {
          this.$set(this.model, 'children', []);
          this.addChild();
          this.open = true;
        }
      },
      addChild: function() {
        this.model.children.push({
          name: 'new stuff',
        });
      },
      handleNodeClick(model) {
        this.dispatch('bc-tree', 'on-node-click', this.model);
      },
    },
  };
</script>
