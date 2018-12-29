<template>
  <div v-if="showSizer || showElevator" :class="optsClasses">
    <div v-if="showSizer" :class="sizerClasses">
      <bc-select v-model="currentPageSize" :size="size" @on-change="changeSize">
        <bc-option v-for="item in pageSizeOpts" :key="item" :value="item" style="text-align:center;">{{ item }} {{ '/page' }}</bc-option>
      </bc-select>
    </div>
    <div v-if="showElevator" :class="ElevatorClasses">
      {{ 'Goto' }}
      <input type="text" :value="_current" @keyup.enter="changePage"> {{ 'page' }}
    </div>
  </div>
</template>
<script>
  import Select from '../select/select.vue';
  import Option from '../select/option.vue';
  const prefixCls = 'bcui-page';

  function isValueNumber(value) {
    return (/^[1-9][0-9]*$/).test(value + '');
  }
  export default {
    name: 'bc-pagination-option',
    components: {
      'bc-select': Select,
      'bc-option': Option,
    },
    props: {
      pageSizeOpts: Array,
      showSizer: Boolean,
      showElevator: Boolean,
      current: Number,
      _current: Number,
      pageSize: Number,
      allPages: Number,
      isSmall: Boolean,
    },
    data() {
      return {
        currentPageSize: this.pageSize,
      };
    },
    watch: {
      pageSize(val) {
        this.currentPageSize = val;
      },
    },
    computed: {
      size() {
        return this.isSmall ? 'small' : 'default';
      },
      optsClasses() {
        return [
          `${prefixCls}-options`,
        ];
      },
      sizerClasses() {
        return [
          `${prefixCls}-options-sizer`,
        ];
      },
      ElevatorClasses() {
        return [
          `${prefixCls}-options-elevator`,
        ];
      },
    },
    methods: {
      changeSize() {
        this.$emit('on-size', this.currentPageSize);
      },
      changePage(event) {
        let val = event.target.value.trim();
        let page = 0;
        if (isValueNumber(val)) {
          val = Number(val);
          if (val != this.current) {
            const allPages = this.allPages;
            if (val > allPages) {
              page = allPages;
            } else {
              page = val;
            }
          }
        } else {
          page = 1;
        }
        if (page) {
          this.$emit('on-page', page);
          event.target.value = page;
        }
      },
    },
  };
</script>
