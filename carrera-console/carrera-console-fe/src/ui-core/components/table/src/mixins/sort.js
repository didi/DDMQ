export default {
  methods: {
    filter (store, key, value){
      let data = store;
      let keyword = value;
      if (keyword === '' || keyword === undefined || keyword.length === 0) {
        return data;
      } else if (typeof keyword  === 'string') {
        keyword = keyword.toLowerCase();
      } else if (Array.isArray(keyword)) {
        keyword.forEach((node, i)=>{
          node = node + '';
          keyword[i] = node.toLowerCase();
        });
      }
      function inStr(data) {
        data = data + '';
        data = data.toLowerCase();
        if (Array.isArray(keyword)){
          return keyword.includes(data);
        } else {
          return data === keyword;
        }
      }
      function inObj(data) {
        return inArr(Object.values(data));
      }
      function inArr(data) {
        return !!data.filter(item => {
          if (typeof item === 'string') {
            return inStr(item);
          }
          if (Object.prototype.toString.call(item) === '[object Object]') {
            return inObj(item);
          }
          return false;
        }).length;
      }
      let alldata = data.filter(item => {
        let tmp = item[key];
        if (tmp === undefined || tmp === '') return false;
        if (typeof tmp === 'number') return inStr(tmp);
        if (typeof tmp === 'string') return inStr(tmp);
        if (Array.isArray(tmp)) return inArr(tmp);
        if (Object.prototype.toString.call(tmp) === '[object Object]') return inObj(tmp);
        return false;
      });
      return alldata;
    },
    sort (store, key, order){
      let data = store;
      let result = null;
      if (order === 'desc') {
        result = -1;
      } else if (order === 'asc') {
        result = 1;
      } else {
        result = 0;
      }
      data.sort((a, b) => {
        if (a[key] == null || b[key] == null) {
          return 0;
        }
        let x = a[key];
        let y = b[key];
        if ((typeof x === 'string' && !isNaN(x)) || (typeof y === 'string' && !isNaN(y))){
          x = parseFloat(x);
          y = parseFloat(y);
        }
        if (x > y) {
          return result;
        }
        if (x < y) {
          return -result;
        }
        return 0;
      });
      return data;
    },
  },
};
