<style lang="less">
</style>
<script>
  export default {
    name: 'bc-expand-row',
    data() {
      return {

      };
    },
    render: function(createElement) {
      let tagName = this.type || 'div';
      return createElement(
        tagName, [
          this.custom({
            record: this.item,
            // _record: JSON.parse(JSON.stringify(this.item)),
            rowkey: this.rowkey,
            text: this.item[this.rowkey],
            index: this.index,
            table: this.table,
          })]);
    },
    methods: {

    },
    beforeUpdate(){

    },
    props: ['custom', 'item', 'type', 'rowkey', 'index', 'options', 'table'],
  };
</script>
