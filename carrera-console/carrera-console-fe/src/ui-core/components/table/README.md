## bc-v-table

## Usage
```html
<template>
<bc-table :tableData="tableData" :border="false" :pagination="false">
      <bc-table-column
        field="providerCuuid"
        index="providerCuuid"
        label="目标"
        :params="{a:1}"
        icon="sorting-asc"
        @on-click="onThClick"
      >
        <template scope="scope">
          <div >{{scope.record.providerCuuid + '文字自定义'}}</div>
        </template>
      </bc-table-column>
        <bc-table-column
        field="priority"
        index="priority"
        label="优先级"
        width="100px"
      >

      </bc-table-column>
        <bc-table-column
        field="status"
        index="status"
        label="状态1"
        width="100px"
        classify="normal"
      >

      </bc-table-column>
      
      <bc-table-column
        field="status"
        index="status"
        label="状态2"
        width="100px"
        :classify="[{label:'已启用', value:1}, {label:'已禁用', value:0}]"
      >
      </bc-table-column>
      
      <bc-table-column
        index="opt"
        label="操作"
        :deepCopy="true"
        width="120px"
      >
        <template scope="scope">
          <div>
            <el-button size="small" type="text" @click="buttonClick" >删除</el-button>
          </div>
        </template>
      </bc-table-column>
      <bc-expand-row :expand="true" :accordion="false">
        <template scope="scope">
          <div>{{scope.record}}</div>
        </template>
      </bc-expand-row>
    </bc-table>
</template>
<script>
  export default {
    data() {
      return {
        tableData: [{
          providerCuuid: 'test',
          priority: '20',
          status: 'running',
        }],
      }
    },
    method: {
      buttonClick(){

      }
      onThClick(){

      }
    }
  };
</script>
```

## API

### bc-table props

| name      | type | description | default                                 |
| --------- | ---- | ------- | -----------                                |
| tableData  | Array | 数据列表 |                                   |
| border   | Boolean | 表格边框 | true |
| pagination  | Boolean | 前端分页 | true |
| check  | Boolean | 列表选中功能 | false |
| checkAll  | Boolean | 页面全选功能 | false |
| rowEdit  | Boolean | 表格渲染切换,此项设为true的时候,会为每行数据添加'$$edit'的属性,通过此属性的更改从而触发视图改变 | false |
| lockedMinWidth | Boolean | 锁定表头数据的最小宽度 | false |
| empty-text   | String | 数据为空时的显示文字 |  |
| customTrClass   | function | 自定义tr的class | function(index, record, table){ return `bcui-tr-${index}`;}; |



### bc-table events

| name      | type | description | default                                 |
| --------- | ---- | ------- | -----------                                |
| on-row-expand  | Function | 展开事件 |                                   |
| on-check  | Function | 选定事件 |
| on-check-all  | Function | 全选事件 |                                   |


### bc-table methods

| 方法名      | description | 参数 | default                                 |
| --------- | ---- | ------- | -----------                                |
| expandTable  | 展开某行 | Array/'all',  Boolean|                                   |                                   |
| checkTable  | 选中某行 | Array/'all', Boolean |                                   |                                 |
| getShowData  | 获取当前表格的数据 | 当前所展示的表格数据 |                                   |                                 |

### 每行数据私有属性
| 属性      | description |
| --------- | ---- |
| $$edit  | 更改此属性触发表格重绘 |
| $$expand  | 更改此属性触发数据展开 |
| $$check  | 更改此属性触发数据选中 |

### bc-table-column props

| name      | type | description | default                                 |
| --------- | ---- | ------- | -----------                                |
| field  | String | 数据对应字段 |                                   |
| label   | String | header显示文字 |  |
| show      | Boolean | 是否展示某一列。不要使用 v-if、v-show，有坑。 | ' |
| width      | String | 列宽 | ' |
| icon      | String | 图标 | ' |
| classify      | String | 前端筛选 | 'normal' |
| sort      | Boolean | 前端排序 | false |
| sortType      | String | 默认排序方式 'none'/'desc'/'asc' | 'none' |


### bc-table-column events

| name      | type | description | default                                 |
| --------- | ---- | ------- | -----------                                |
| on-click  | Function | 点击表头事件 |                                   |

###  <template slot="label">
自定义表头

###  <template slot-scope="scope">
 在scope="scope"内部放入表格自定义内容,
 其中scope.record 为该行数据

### bc-expand-row props

| name      | type | description | default                                 |
| --------- | ---- | ------- | -----------                                |
| expand  | Boolean | 列表展开功能 |                                   |
| accordion  | Boolean | 是否手风琴模式 | true                                  |                                  |
| icon  | Boolean | 展开图标 | false                                  |




