<style lang="less">
.bcui-table__header{
  width:100%
}
.bcui-table__body{
  width:100%;
  // .bcui-checkbox__inner:after{
  //   left:4px;
  // }


}
.bc-table, .bcui-table__body {
  .bcui-th-cell{
    cursor:pointer;
    display: block;
    padding-left: 14px;
    white-space: nowrap;
    .bcfont {
      vertical-align: middle;
    }
    .bcui-th-icon{

    }
  }
  .overflow-v {
    overflow: visible !important;
    position: relative;
  }
  .overflow-v:hover .bcui-classify{
    display: block;
  }
  .bcui-classify {
    position: absolute;
    display: none;
    min-width: 160px;
    z-index: 1;
    background-color: #fff;
    border: 1px solid #d1d9e5;
    border-radius: 3px;
    padding: 6px;
    .bcfont {
      margin-right: 10px;
    }
    &-label {
      padding-left: 10px;
      padding-right: 20px;
      margin: 6px 0px;
      .text-link{
        vertical-align: text-bottom;
      }
    }
  }
  .bcui-th-checkbox{
     vertical-align: super;

  }
  .bcui-tr-checkbox{
    width: 40px;
    text-align: center;

  }
  .bcui-td-expand .bcfont {
    vertical-align: top;
    transition: all 0.3s;

  }
  .rotate90 .bcfont {
    transform: rotate(90deg);
  }
  .bcui-table--noborder{
    border: 0
  }

  .pagi-footer{
    text-align:right
  }
  .bcui-check-col{
    width: 40px;
  }
  .bcui-expand-col{
    width: 40px;
  }
  .bcui-td-expand, .bcui-td-check {
    cursor: pointer;
    line-height: 1;
    text-align: center;
  }
  .bcui-tr-expand{
    background-color: #fbfbfb;
    .expand-box {
      padding: 0px 10px
    }
  }
  .empty-text{
    width: 100%;
    height: 120px;
    font-size: 24px;
    color: #eef3f6;
    text-align: center;
    line-height:120px;
  }
}
</style>
<template>
  <div :class="getClass">
    <div style="display:none" ref="fieldbox"><slot></slot></div>
    <!-- 表格 -->
    <div>
      <!--  表头 -->
      <div class="bcui-table__header-wrapper">
        <table class="bcui-table__header">
        </table>
      </div>
      <!-- 表头结束 -->
      <!-- 表体 -->
      <div class="bcui-table__body-wrapper">
        <table  class="bcui-table__body"  ref='test'>
          <colgroup>
            <col v-if="check" class="bcui-check-col" >
            <col v-if="expandProps['expand']" class="bcui-expand-col" >
            <col v-if="option.show" v-for="option in getHeaderProps"  :width="option.width" >
          </colgroup>
          <thead>
            <tr>
              <!-- 全选按钮 -->
              <th v-if="check" class="bcui-table-selection-column">
                <div class="bcui-th-cell">
                  <bc-checkbox  v-if="checkAll" ref="check-all"  v-model="checkAllStatus"  @input="(v)=>{}">
                  </bc-checkbox>
                </div>
            </th>
            <!-- 展开按钮 -->
            <th v-if="expandProps['expand'] && expandProps['icon']" class="bcui-th-expand">
            </th>

            <th  v-if="item.show" v-for="item in getHeaderProps"  :class="item.classify ? 'overflow-v': ''"  :style="_thStyleObject(item)" >
                <div class="bcui-th-cell" :class="item.classify ? 'overflow-v': ''"  @click="_clickTh(item)" >
                  <div v-if="item['label_DOM']"><bc-th-label :dom="item['label_DOM']"></bc-th-label></div>
                  <span v-else>{{item.label}}
                    <i v-if="item.icon" :class="'bcui-th-icon bcfont bcui-icon-' + item.icon"></i>
                    <span v-if="item.sort" :class="['bcui-table-column-sorter', `sort-${item.$$sortType}`]" @click="_changeSort(item)">

                      <i v-if="item.$$sortType === 'desc'" :class="['bcui-th-icon', 'icon', 'icon-triangle-bottom']" ></i>
                      <i v-else-if="item.$$sortType === 'asc'" :class="['bcui-th-icon', 'icon', 'icon-triangle-top']" ></i>
                      <i v-else :class="['bcui-th-icon', 'icon', 'icon-triangle-bottom']" ></i>
                    </span>
                    <span v-if="item.classify" @click="()=>{}">
                      <i  class="bcui-th-icon bcfont bcui-icon-caidan" ></i>
                      <div class="bcui-classify " v-if="filterKey[item.field].length">
                        <div>
                          <div class="bcui-classify-label"   @click="filterTable(dom,  filterKey[item.field], item.field)" v-for="dom in filterKey[item.field]" >
                            <i  :class="dom.check ?'bcfont text-link bcui-icon-dui' : 'bcfont bcui-icon-xuxianxuankuang'" ></i><span > {{dom.label}}</span>
                          </div>
                        </div>
                      </div>
                    </span>
                  </span>
                </div>
              </th>
            </tr>
          </thead>
          <tbody v-if="tableData.length">
          <template v-for="(rowData, index) in getShowData">
            <tr class='bcui-tr-data' :class="customTrClass(index,rowData,getShowData)" :key="index" >
              <!-- 选定 -->
              <td v-if="check" class="bcui-td-check bcui-table-selection-column">
                <div class="cell">
                  <bc-checkbox :disabled="rowData['$$check-disabled']" v-model="rowData['$$check']"  @input="(v)=>{
                      _checkRow(rowData,index,v, getShowData);
                  }"></bc-checkbox>
              </div></td>
              <!-- 展开 -->
              <td v-if="expandProps['expand'] && expandProps['icon']" @click='_expandRow(rowData,index)'><div class="bcui-td-expand" >
                  <span :class="{'rotate90':rowData['$$expand']}"><i :class="'bcfont bcui-icon-arrow-right-double'"></i></span>
              </div></td>

              <td  v-if="option.show"  v-for="(option,key) in getHeaderProps" :style="'max-width:'+ option['maxWidth']">
                <div class="cell" v-if="option['custom_DOM']">
                  <bc-table-cell
                      :custom="option['custom_DOM']"
                      :options="option"
                      :item="rowData"
                      :rowkey="option.field"
                      :index="index"
                      :table="tableData">
                  </bc-table-cell>
                </div>
                <div class="cell" v-else>
                  <span>{{rowData[option.field]}}</span>
                </div>
              </td>
            </tr>
            <template v-if="expandProps['custom_DOM']" >
              <tr v-show="rowData['$$expand']" class='bcui-tr-expand'>
                <td :colspan="getTrCount" class=''>
                  <template >
                    <div class='expand-box '>
                      <bc-expand-row
                        :custom="expandProps['custom_DOM']"
                        :item="rowData"
                        :index="index"
                        :table="tableData">
                      </bc-expand-row>
                    </div>
                  </template>
                </td>
              </tr>
              </template>
              <template v-else-if="expandProps['td_DOM']" >
                  <bc-expand-row
                    v-show="rowData['$$expand']"
                    class='bcui-tr-expand'
                    :custom="expandProps['td_DOM']"
                    :item="rowData"
                    :index="index"
                    type="tr"
                    :table="tableData">
                  </bc-expand-row>
              </template>
          </template>
          </tbody>

        </table>

        <div v-if="!getShowData.length" class="empty-text">
          <i class="bcfont bcui-icon-information-warning" style="font-size:24px"></i>
          <span>{{emptyText}}</span>
        </div>
      </div>
      <!-- 表体结束 -->
    </div>
    <!-- 表格结束 -->
    <!-- 分页 -->
    <div class="pagi-footer">
      <bc-pagination v-if="pagination === true" ref="pagi" :theme="this.colorType" :currentPage.sync="currentPage" :dataSize="dataSize" :pageSize.sync="pageSize"  ></bc-pagination>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue';
  import Pagination from './pagination.vue';
  import Checkbox from './checkbox.vue';
  import tableThLabel from './custom-render/table-th-label.vue';
  import tableExpandRow  from './custom-render/table-expand-row.vue';
  import tableCell from './custom-render/table-cell.vue';
  // import tableCell from './tableCell';
  import '../styles/table.less';
  import sort from '../mixins/sort';
  const ClassPrefix = 'bcui-table';
  let renderClass = function(index, record, table){
    return `bcui-tr-${index}`;
  };
  export default {
    name: 'bc-table',
    mixins: [sort],
    props: {
      tableData: Array,
      pagination: Boolean,
      border: {
        type: Boolean,
        default: true,
      },
      check: {
        type: Boolean,
        default: false,
      },
      checkAll: {
        type: Boolean,
        default: false,
      },
      expand: {
        type: Boolean,
        default: false,
      },
      rowEdit: {
        type: Boolean,
        default: false,
      },
      customTrClass: {
        type: Function,
        default: renderClass,
      },
      emptyText: {
        type: String,
        default: '数据为空',
      },
      theme: {
        type: String,
        default: 'light',
      },
      colorType: {
        type: String,
        default: 'odin',
      },
      lockedMinWidth: {
        type: Boolean,
        default: false,
      },
    },
    components: {
      'bc-th-label': tableThLabel,
      'bc-table-cell': tableCell,
      'bc-expand-row': tableExpandRow,
      [Pagination.name]: Pagination,
      [Checkbox.name]: Checkbox,
    },
    data() {
      return {
        checkAllaction: false,
        console: console,
        pageSize: 10,
        dataSize: 0,
        currentPage: 1,
        tableStore: [],
        showData: [],
        filerTable: [],
        headerProps: {},
        expandProps: {},
        filterField: '',
        filterKey: {
          '$$change': false,
        },
      };
    },
    methods: {
      // 点击表头
      _clickTh(item){
        item['_vnode'].$emit('on-click', item.field, item.params);
      },
      _thStyleObject(item){
        let style = {};

        if (item.width) {
          style['width'] = item.width;
        } else if (typeof item.label === 'string' && this.lockedMinWidth === true) {
          style['minWidth'] = `${(item.label.length * 10) + 60}px`;
        }
        return style;
      },
      _classifyDom(key, opt, table) {
        let tableStore = table || this.tableStore;
        // let tableFilter = table || [];
        // tableStore = this.filter(tableStore, key, searchValue.length ? searchValue : '');
        let array = [];
        let classifyDom = [];
        let filterList = this.filterKey[key];
        // if (this.filterField === key) {
        //   classifyDom = filterList;
        //   return classifyDom;
        // };
        if (opt === 'normal') {
          tableStore.forEach((node)=>{
            if (node[key] && !array.includes(node[key])) {
              let tmp = {
                value: node[key],
                label: node[key],
                field: key,
              };

              let status = false;
              if (Array.isArray(filterList) && filterList.length) {
                filterList.forEach((filter)=>{
                  if (filter.value == node[key]) {
                    status = filter.check;
                  }
                });
              }

              this.$set(tmp, 'check', status);
              classifyDom.push(tmp);
              array.push(node[key]);
            }
          });
        } else {
          Array.isArray(opt) && opt.forEach((node)=>{
            let tmp = {
              value: node['value'],
              label: node['label'],
              field: key,
            };
            let status = node.check !== undefined ? node.check : false;
            if (Array.isArray(filterList) && filterList.length) {
              filterList.forEach((filter)=>{
                if (filter.value == node[key]) {
                  status = filter.check;
                }
              });
            }
            this.$set(tmp, 'check', status);
            classifyDom.push(tmp);
          });
        }

        return classifyDom;
      },
      // 展开某行
      _expandRow(rowData){
        let initExpand = rowData['$$expand'];
        if (this.expandProps['accordion']) {
          this.tableStore.forEach((node)=>{
            Vue.set(node, '$$expand', false);
          });
        }
        rowData['$$expand'] = !initExpand;
        // rowData['$$expand'] = !rowData['$$expand'];
        this.$emit('on-row-expand', rowData['$$expand'], rowData);
      },
      // 选择某行
      _checkRow(rowData, index, v, table){
        if (this.checkAllaction  === false) {
          this.$nextTick(() => {
            this.$emit('on-check', rowData['$$check'], rowData, index);
          });
          this._tableCheckToggle(table);
        }
      },
      _tableCheckToggle(table){
        let pass = true;
        table.forEach((node)=>{
          if (node['$$check'] !== true){
            pass = false;
          };
        });
        this.$emit('on-check-toggle', table, pass);
      },
      // 更改排序
      _changeSort(item){
        let sortList = ['none', 'desc', 'asc'];
        let type =  sortList[(sortList.indexOf(item['$$sortType']) + 1)] || 'none';
        this.sortTable(item.field, type);
      },
      // 更新表格数据
      _upTableData(data){
        this.tableStore = JSON.parse(JSON.stringify(data));
        // this.tableStore = this.tableStore;
        this.tableStore.forEach((node)=>{
          if (this.expandProps['expand'] === true){
            Vue.set(node, '$$expand', node['$$expand'] === true);
          }
          if (this.rowEdit === true){
            Vue.set(node, '$$edit', node['$$edit'] === true);
          }
          if (this.check === true){
            Vue.set(node, '$$check', node['$$check'] === true);
            Vue.set(node, '$$check-disabled', node['$$check-disabled'] === true);
          }
        });
        if (Object.keys(this.filterKey).length) {
          this._initFilterKey(this.tableStore);
        }
      },
      _initFilterKey(table, key){
        let headerProps = this.getHeaderProps;
        for (let key in headerProps) {
          let item = headerProps[key];
          if (headerProps[key]['classify']) {
            let domList = this._classifyDom(key, item['classify'], table);
            this.filterKey[key] =  domList;
          }
        }
      },
      _clearSort(){
        let headerProps = this.headerProps;
        for (let tmpkey in headerProps){
          this.headerProps[tmpkey]['$$sortType'] = false;
        };
      },
      _clearFilter(){
        this.filterKey = {
          '$$change': false,
        };
      },
      // 对外方法

      // pai
      sortTable(key, type){
        let data = this.tableStore;
        let headerProps = this.headerProps;
       //  this._clearSort();
        headerProps[key]['$$sortType'] = type;
        this.tableStore = this.sort(data, key, type);
        this.$emit('on-table-sort', key, type);
      },
      filterTable(dom, array, key){
        let v = dom.check;
        // array.forEach((node)=>{
        //   node.check = false;
        // });
        this.filterField = key;
        dom.check = !v;
        this.$forceUpdate();
        this.filterKey['$$change'] = !this.filterKey['$$change'];
        this.$emit('on-table-filter', dom);
      },
      expandTable(opt, value){
        let array;
        let v = value;
        if (opt === 'all'){
          array = 'all';
        } else if (Array.isArray(opt)){
          array = opt;
        } else {
          array = [];
        }
        let showData = this.getShowData;
        let tmp = [];
        if (array === 'all') {
          showData.forEach((item,i)=>{
            Vue.set(item, '$$expand', v);
            tmp.push(item);
          });
        } else {
          showData.forEach((item,i)=>{
            if (array.includes(i)) {
              Vue.set(item, '$$expand', v);
              tmp.push(item);
            }
          });
        }
        return tmp;
      },
      checkTable(opt, value){
        let array;
        let v =  value;
        if (value === undefined) {
          v = true;
        }
        if (opt === 'all'){
          array = 'all';
          this.checkAllStatus = v;
        } else if (Array.isArray(opt)){
          array = opt;
          array.forEach((node)=>{
            node['$$check'] = v;
          });
        }
      },
      getCheckRow(){
        let showData = this.getShowData;
        let tmp = [];
        showData.forEach((item,i)=>{
          if (item['$$check']) {
            tmp.push(item);
          }
        });
        return tmp;
      },
    },
    watch: {
    },
    computed: {
      checkAllStatus: {
        get: function () {
          let data = this.getShowData;
          let pass = false;
          pass = data.every((node)=>{
            return node['$$check'] === true;
          });
          return pass && !!data.length;
        },
        set: function (v) {
          this.checkAllaction = true;
          let data = this.getShowData;
          let pass = true;
          let allcheck = true;
          data.forEach((node, i)=>{
            if (i === 0) {
              allcheck = node['$$check'];
            }
            if (node['$$check'] !== allcheck){
              pass = false;
            };
          });
          if (pass || v) {
            data.forEach((node, i)=>{
              node['$$check'] = v;
            });
            this.$emit('on-check-all', v, data);
          }
          this._tableCheckToggle(data);
          this.$nextTick(() => {
            this.checkAllaction = false;
          });
        }
      },
      getShowData: function(){
        let pageSize = this.pageSize;
        let currentPage = this.currentPage;
        let showData;
        let tableStore = this.tableStore;
        let filterKey = this.filterKey;
        let searchValue = [];
        for (let key in filterKey) {
          let pass = false;
          Array.isArray(filterKey[key]) && filterKey[key].forEach((node)=>{
            if (node.check === true) {
              pass = true;
              searchValue.push(node.value);
            }
          });
          if (key !== '$$change' && pass) {
            tableStore = this.filter(tableStore, key, searchValue.length ? searchValue : '');
          }
        }
        // this['_initFilterKey'](tableStore);
        this.dataSize = tableStore.length;
        // let headerProps = this.headerProps;
        if (tableStore.length > pageSize && this.pagination){
          showData = tableStore.slice((currentPage * pageSize - pageSize), (currentPage * pageSize));
        } else {
          showData = tableStore;
        };
        if (showData.length === 0) {
          this.$nextTick(()=>{
            this.currentPage = 1;
          });
        }
        // this.tableStore = JSON.parse(JSON.stringify(showData))
        return showData;
      },
      getTrCount: function() {
        let num = 0;
        if (this.check){
          num += 1;
        }
        if (this.expandProps['expand']) {
          num += 1;
        }
        num += Object.keys(this.headerProps).length;
        return num;
      },
      getHeaderProps: function() {
        let headerProps = this.headerProps;
        for (let key in headerProps){
          Vue.set(headerProps[key], '$$sortType', headerProps[key]['sortType']);
        }
        return headerProps;
      },

      getClass: function(){
        return [`${ClassPrefix}`, {
          [`${ClassPrefix}--noborder`]: !this.border,
          [`${ClassPrefix}--light`]: this.theme === 'light',
        }];
      },
    },
    beforeUpdate(){

    },
    mounted(){
      this._upTableData(this.tableData);
      this.$watch('tableData', function(newVal, oldVal) {
        this._upTableData(newVal);
      });
    },
  };
</script>
