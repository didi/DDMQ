<style lang="less">
</style>
<script>
  export default {
    name: 'bc-table-cell',
    data() {
      return {

      };
    },
    render: function(createElement) {
      return createElement(
        'div', [
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
      deepCopy(obj){
        if (this.options.deepCopy){
          return JSON.parse(JSON.stringify(obj));
        } else {
          return obj;
        }
      },
    },
    beforeUpdate(){

    },
    props: ['custom','item', 'deepItem', 'rowkey', 'index', 'options', 'table'],
  };
</script>
