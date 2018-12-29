import dict from './dict.js';

/**
     * 延迟执行，在延迟的时间内再次出发的时候，取消上次的延迟执行任务
     * @param {Function} func 待执行的函数
     * @param {Number} wait 延迟执行的时间
     * @param {Boolean} immediate 是否支持立即调用
     */

export default {
  data () {
    return {
      dict
    };
  },

  methods: {

    validateForm (name) {
      let isValid = false;

      this.$refs[name].validate((valid) => {
        isValid = valid;
      });

      return isValid;
    },

    /**
     * 字典翻译
     * @param  {String} type  字典类型 以模块划分
     * @param  {String} key   需要翻译的key
     * @param  {String} value 需要翻译的key对应的值
     * @return {String}       返回的翻译结果
     */
    dictTranslate (type, key, value) {
      let list = dict[type][key];

      let result = '';

      list.map((item) => {
        if (value === item.value) {
          result = item.label || item.value;
        }
      });

      return result || value;
    },

    coloringText (type, key, value, classname) {
      let result = '';

      let list = dict[type][key];

      list.map((item) => {
        if (value === item.value) {
          result = item[classname] || item.className;
        }
      });

      return result;
    },

    formatMap (data) {
      if (data.length === 1 && data[0].key === '') {
        return {};
      } else {
        let result = {};
        data.map((item) => {
          this.$set(result, item.key, item.value);
        });
        return result;
      }
    },

    // 将对象的属性转为key、value的形式展示
    formatObject (obj) {
      if (obj && Object.keys(obj).length) {
        let arrays = [];
        let keys = Object.keys(obj).filter((item) => item);
        keys.forEach((item) => {
          arrays.push({
            key: item,
            value: obj[item]
          });
        });
        return arrays;
      } else {
        let array = [{
          key: '',
          value: ''
        }];
        return array;
      }
    },

    // 根据value获取label
    getNameById (id, array) {
      let label = '';
      array.forEach((item) => {
        if (id && item.value === id) {
          label = item.label;
        }
      });
      return label;
    }

  }
};
