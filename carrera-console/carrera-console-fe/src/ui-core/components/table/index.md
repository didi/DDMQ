---
category: Modules
subtitle: 表格
type: Data View
noinstant: true
title: Table
---

# Table 表格

多在有大量结构化的数据需要展现时使用；同时支持对数据进行排序、搜索、分页、自定义操作等复杂行为。

## 基础用法

:::demo 基础用法

使用`tableData`绑定数据，。在`bc-table-column`中用`field`和`index`属性来对应对象中的键名即可填入数据，用`label`属性来定义表格的列名。可以使用width属性来定义列宽。

```html
<template>
  <bc-table
   :tableData="list"
   >
   <bc-table-column field="id" index="id" label="ID"></bc-table-column>
   <bc-table-column field="name" index="name" label="姓名"></bc-table-column>
   <bc-table-column field="email" index="email" label="邮箱"></bc-table-column>
  </bc-table>
</template>
<script>
  export default {
    data() {
      return {
        list: [
          {id: 1, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
        ],
      };
    },
  };
</script>
```
:::

## 自定义单元格内容

:::demo 自定义渲染

通过 `Scoped slot` 可以获取到 record。record包含当前这一列的数据信息。

```html
<template>
  <bc-table
   :tableData="list"
   >
   <bc-table-column field="id" index="id" label="ID"></bc-table-column>
   <bc-table-column field="name" index="name" label="姓名"></bc-table-column>
   <bc-table-column field="email" index="email" label="邮箱">
    <template slot-scope="scope">
      <div>
        <bc-tag>{{scope.record.email}}</bc-tag>
      </div>
    </template>
  </bc-table-column>
  </bc-table>
</template>
<script>
  export default {
    data() {
      return {
        list: [
          {id: 1, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 2, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 3, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 4, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
        ],
      };
    },
  };
</script>
```
:::

## 展开行

:::demo

使用`bc-expand-row`作为展开内容的容器，可以通过`on-row-expand`监听展开操作。

```html
<template>
  <bc-table
   :tableData="list"
   >
    <bc-table-column field="id" index="id" label="ID"></bc-table-column>
    <bc-table-column field="name" index="name" label="姓名"></bc-table-column>
    <bc-table-column field="email" index="email" label="邮箱">
      <template slot-scope="scope">
        <div>
          <bc-tag>{{scope.record.email}}</bc-tag>
        </div>
      </template>
    </bc-table-column>
    <!-- 显示创建用于呈现展开内容的组件 -->
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
        list: [
          {id: 1, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 2, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 3, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 4, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
        ],
      };
    },
  };
</script>
```
:::

## 单选和多选

:::demo

通过设置`bc-table`组件的`check`属性开启列表选中功能，设置`checkAll`为`true`开启列表全选功能。同时提供两个``

```html
<template>
  <bc-table
    :tableData="list"
    :check="true"
    :checkAll="true"
    @on-check="handleRowCheck"
    @on-check-all="handleRowCheckAll"
   >
    <bc-table-column field="id" index="id" label="ID"></bc-table-column>
    <bc-table-column field="name" index="name" label="姓名"></bc-table-column>
    <bc-table-column field="email" index="email" label="邮箱">
      <template slot-scope="scope">
        <div>
          <bc-tag>{{scope.record.email}}</bc-tag>
        </div>
      </template>
    </bc-table-column>
    <!-- 显示创建用于呈现展开内容的组件 -->
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
        list: [
          {id: 1, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 2, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 3, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
          {id: 4, name: 'zhanglun', email: 'zhanglun1410@gmail.com'},
        ],
      };
    },
    methods: {
      handleRowCheck(checked, list) {
        alert(checked);
        alert(list);
      },
      handleRowCheckAll(checked, data) {
        alert(checked);
        alert(data);
      },

    },

    mounted() {
    },
  };
</script>
```
:::
