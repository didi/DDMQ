<style scoped lang="less">
.bcui-pagination {
    white-space: nowrap;
    padding: 2px 5px;
    margin: 12px 0px;
    color: rgb(72, 93, 106);
    font-size: 0;
}
 *:focus {
  outline: none;
}
.bcui-page-size-button{
   display: inline-block;
   margin-right: 20px;
   position: relative;
   .text-link {
     cursor: pointer
   }
   .bcui-pager{
     position: absolute;
     top: 0px;
     right: 0px;
     border-bottom: 1px solid #d1dee5;
   }
   .bcui-pager li {
     border-bottom: 0;
   }
}
</style>
<template>
  <!-- 分页 -->
  <div  :class="getClass">
    <div class="bcui-page-size-button">
      <span class="">每页</span>
        <span class="text-link" @click="pageSizeBox()">{{pageSize}}</span>
        <ul class="bcui-pager page-size-box" v-show="pageSizeBoxVisible" >
          <template v-if="customSize === null">
            <li v-for="(item, i) in pageSizes" @click="pageSizeChange(item)"> {{item}} </li>
            <li  @click="goCustomSize"> 自定义 </li>
          </template>
          <template v-else >
             <li @keyup.enter="updateCustomSize" class="custom-size-dom"  contenteditable="true" style="width:80px"> {{customSize}} </li>
             <li @click="updateCustomSize" style="padding:3px 8px"> ↵ </li>
           </template>
        </ul>
      <span class="">条</span>
    </div>
    <button class="btn-prev" @click="privatePageChange(current-1)"><</button>
    <ul class="bcui-pager">
      <li class="number" :class="{ active:n === current}" @click="privatePageChange(n)" v-for="n in getShowPage()">{{n}}</li>
    </ul>

    <button class="btn-next" @click="privatePageChange(current+1)">></button>
    <span class="bcui-pagination__jump">前往
      <input type="number" v-model="searchPage" @keyup.13="onEnter" min="1" class="bcui-pagination__editor">页
    </span>
    <span class="bcui-pagination__total">共 {{dataSize}} 条 </span>
  </div>
  <!-- 分页结束 -->
</template>

<script>
  import  './pagination.css';
  const ClassPrefix = 'bcui-pagination';
  export default {
    name: 'bc-pagination',
    props: {
      dataSize: {
        type: Number,
        default: 0,
      },
      pageSize: {
        type: Number,
        default: 10,
      },
      pageSizes: {
        type: Array,
        default: function(){
          return [10, 20, 50];
        },
      },
      pageChange: {
        type: Function,
      },
      theme: {
        type: String,
      },
      currentPage: {
        type: Number,
      },
    },
    data() {
      return {
        customSize: null,
        searchPage: 1,
        pageSizeBoxVisible: false,
        current: 1,
      };
    },
    methods: {
      getallPage: function(){
        return Math.ceil(this.dataSize / this.pageSize) || 1;
      },
      goCustomSize() {
        this.customSize = 0;
      },
      updateCustomSize(){
        let dom = this.$el.querySelector('.custom-size-dom');
        let tmp = dom.innerText;
        if (tmp && parseInt(tmp, 10)) {
          this.pageSizeChange(parseInt(tmp, 10));
        } else {
          this.pageSizeChange(this.pageSize);
        }
      },
      onEnter(){
        let allPage = this.getallPage();
        if (!this.searchPage){
          this.current = 1;
        } else if (this.searchPage > allPage){
          this.current = allPage;
        } else {
          this.current = parseInt(this.searchPage);
        }
        this.searchPage = this.current;
        this.privatePageChange(this.current);
      },
      pageSizeBox(){
        this.pageSizeBoxVisible = true;
      },
      pageSizeChange(n){
        this.pageSizeBoxVisible = false;
        this.customSize = null;
        this.$emit('update:pageSize', parseInt(n, 10));
      },
      getShowPage(){
        let allPage = this.getallPage();
        let showPage = [];
        let firstPage;
        let lastPage;
        if (this.current < 4){
          firstPage = 1;
          lastPage = allPage > 5 ? 5 : allPage;
        } else if (this.current > allPage - 3){
          firstPage = allPage - 4 > 0 ? allPage - 4 : 1;
          lastPage = allPage;
        } else {
          firstPage = this.current - 2;
          lastPage = this.current + 2;
        }
        for (let i = firstPage; i < (lastPage + 1); i++){
          showPage.push(i);
        }
        return showPage;
      },
      privatePageChange(n){
        let allPage = this.getallPage();
        if (n < 1){
          return;
        } else if (n > allPage) {
          n = allPage;
        }
        this.$emit('on-page-change', n);
        this.$emit('update:currentPage', n);
        if (this.$props.pageChange){
          this.$props.pageChange(n);
        }
        this.current = n;
      },
    },
    beforeCreate(){
      // this.headerProps = {};
    },
    beforeUpdate(){
    },
    computed: {
      getClass: function(){
        return [`${ClassPrefix}`, {
          [`${ClassPrefix}--dark`]: this.theme === 'console',
        }];
      },
    },
    mounted(){
      this.current = this.currentPage || 1;
      this.$on('current', this.privatePageChange);
      this.$watch('currentPage', function(newValue){
        this.current = this.currentPage || 1;
      });
    },
  };
</script>
