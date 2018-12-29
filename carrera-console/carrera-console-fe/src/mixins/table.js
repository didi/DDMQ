export default {
  data () {
    return {
      checkedTableItems: []
    };
  },
  methods: {
    // 表格中checkbox 全选单选的控制
    handleCheckTableAll (checked, rows, name = 'checkedTableItems') {
      this[name] = checked ? [].concat(rows) : [];
    },

    handleCheckTableRow (checked, row, index, name = 'checkedTableItems') {
      let list = this[name];

      if (list.includes(row) && !checked) {
        let index = list.indexOf(row);

        list = list.splice(index, 1);
      }

      if (!list.includes(row) && checked) {
        list.push(row);
      }
    },

    handleCheckSingleTableRow (checked, row, index, name = 'checkedTableItems') {
      let list = this.$refs['table'].getShowData;

      if (checked) {
        list.forEach((item, i) => {
          item.$$check = i === index;
        });

        this[name] = [row];
      } else {
        this[name] = [];
      }
    },

    clearCheckTableRows (name = 'checkedTableItems') {
      this[name].forEach((item) => {
        item.$$check = false;
      });

      this[name] = [];
    }
  }
};
