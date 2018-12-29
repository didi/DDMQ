export default {
  name: 'bc-table-cell',
  render: function(createElement) {
    return createElement(
      'div',   // tag name 标签名称
      this.custom // 子组件中的阵列
    );
  },
  props: ['custom']
};
