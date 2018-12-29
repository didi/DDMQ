<style lang="less">
  .filter-inputer-searcher {
    width: 344px;
  }
</style>

<template>
  <div class="filter-inputer-searcher" :style="wrapperStyles">
    <bc-input
      v-model="inputValue"
      @on-enter="handleInputEnter"
      @on-change="handleInputChange"
      >
      <bc-select slot="select"
                 :style="filterStyles"
                 v-model="filter"
                 @on-change="handleFilterChange">
        <bc-option v-for="item in searchKeywords" :value="item.key" :key="item.key">{{item.label}}</bc-option>
      </bc-select>
    </bc-input>
  </div>
</template>

<script>
  const classPrefix = 'filter-inputer-searcher';

  export default {
    name: 'filter-input-searcher',
    components: {},
    props: {
      searchKeywords: {
        type: Array
      },
      width: {
        type: Number | String
      },
      filterWidth: {
        type: Number
      }
    },
    data () {
      return {
        classPrefix,
        filter: '',
        inputValue: '',

        timer: null
      };
    },
    watch: {
      inputValue (update, old) {
        if (!update && old) {
          this.$emit('on-input-change', {});
          this.$emit('input', {});
        }
      }
    },
    computed: {
      wrapperStyles () {
        let styles = {};

        if (this.width) {
          styles.width = `${this.width}px`;
        }

        return styles;
      },

      filterStyles () {
        let styles = {};

        if (this.filterWidth) {
          styles.width = `${this.filterWidth}px`;
        }

        return styles;
      }
    },
    methods: {
      handleInputEnter (event) {
        let { inputValue, timer, filter } = this;

        if (!inputValue || !filter) {
          return;
        }

        if (timer) {
          clearTimeout(timer);
        }

        this.timer = setTimeout(() => {
          let params = this.createFitlerParams();

          this.$emit('on-input-enter', params);
          this.$emit('input', params);
        }, 300);
      },
      handleInputFocus (event) {
        this.$emit('on-input-focus', event);
      },
      handleInputChange (event) {
        let { inputValue, timer, filter } = this;

        if (!inputValue || !filter) {
          return;
        }

        if (timer) {
          clearTimeout(timer);
        }

        this.timer = setTimeout(() => {
          let params = this.createFitlerParams();

          this.$emit('on-input-change', params);
          this.$emit('input', params);
        }, 300);
      },
      handleFilterChange (event) {
        let { inputValue, filter } = this;

        if (!inputValue || !filter) {
          return;
        }

        let params = this.createFitlerParams();

        this.$emit('on-filter-change', params);
        this.$emit('input', params);
      },

      createFitlerParams () {
        let { inputValue, filter } = this;
        let qo = '';

        this.searchKeywords.forEach((item) => {
          if (item.key === filter) {
            qo = item.qo;
          }
        });

        if (!inputValue || !filter) {
          return {};
        }

        return {
          key: filter,
          value: inputValue,
          qo: qo || '='
        };
      }
    },
    mounted () {}
  };
</script>
