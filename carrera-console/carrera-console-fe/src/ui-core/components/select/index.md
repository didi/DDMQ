---
category: Components
subtitle: 选择器
type: Form
noinstant: true
title: Select
---


# Select 选择器

## 基础用法

基础的选择器用法。


::: demo 基本用法

可以使用 v-model 双向绑定数据。单选时，value 只接受字符串和数字类型，多选时，只接受数组类型，组件会自动根据Option的value来返回选中的数据。

```html
<template>
  <bc-select placeholder="请选择">
    <bc-option
      v-for="item in data1"
      :key="item.value"
      :value="item.value"
      :label="item.label">
      {{item.label}}</bc-option>
  </bc-select>
</template>
<script>
  export default {
    data() {
      return {
        data1: [{
          label: '第一个<msg>',
          value: '1',
        }],
      };
    },
  };
</script>
```
:::

## 多选

::: demo 多选

通过设置属性multiple可以开启多选模式。多选模式下，model 接受数组类型的数据，所返回的也是数组。

```html
<template>
  <bc-select
  multiple
  placeholder="请选择"
  v-model="value2">
    <bc-option
      v-for="item in data2"
      :key="item.value"
      :value="item.value">
      {{item.label}}</bc-option>
  </bc-select>
</template>
<script>
  export default {
    data() {
      return {
        value2: [],
        data2: [{
          label: '第一个',
          value: '1',
        }, {
          label: '第2个',
          value: '2',
        }, {
          label: '第3个',
          value: '3',
        }],
      };
    },
  };
</script>
```
:::

## 远程搜索


::: demo 远程搜索

远程搜索需同时设置 filterable、remote、remote-method、loading 四个 props，其中 loading 用于控制是否正在搜索中，remote-method 是远程搜索的方法。注意：需要给 Option 设置 key。设置初始显示值，需设置 label 属性。


```html
<template>
  <bc-select
  :label="defaultLabel"
  remote
  filterable
  placeholder="请选择"
  v-model="value">
    <bc-option
      v-for="item in data2"
      :key="item.value"
      :value="item.value">
      {{item.label}}</bc-option>
  </bc-select>
</template>
<script>
  export default {
    data() {
      return {
        value: '1',
        defaultLabel: '第一个<msg>',
        data2: [{
          label: '第一个<msg>',
          value: '1',
        }, {
          label: '第2个',
          value: '2',
        }, {
          label: '第3个',
          value: '3',
        }],
      };
    },
  };
</script>
```
:::

## 过滤


::: demo asdfasf

这里是这个例子的描述，下面是源代码

```html
<template>
<div>
  <bc-select
  filterable
  placeholder="请选择"
  @on-change="resetQuery"
  v-model="value4" ref="resetRef">
    <bc-option
      v-for="item in data2"
      :key="item.value"
      :value="item.value">
      {{item.label}}</bc-option>
  </bc-select>
  {{ rule }}
  <bc-button @click="set">test</bc-button>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        value4: '',
        data2: [{
          label: '第一个',
          value: '1',
        }, {
          label: '第2个',
          value: '2',
        }, {
          label: '第3个',
          value: '3',
        }],
        rule: {},
      };
    },
    methods: {
      resetQuery() {
        setTimeout(() => {
          // this.$refs.resetRef.setQuery('');
        }, 300);
      },
      set() {
        console.log(this.rule);
        this.rule = {
          baneL: 'zhanglun',
        };
        console.log(this.rule);
      }
    },
  };
</script>
```
:::

## Select props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---------|----------|-----------|-----------|-----------|
| value | 指定选中项目的 value 值，可以使用 v-model 双向绑定数据。单选时只接受 String 或 Number，多选时只接受 Array | String·Number·Array | - |
| multiple | 是否支持多选 | Boolean | false |
| disabled | 是否禁用 | Boolean | false |
| filterable | 是否支持搜索 | Boolean | false |
| remote | 是否使用远程搜索 | Boolean | false |
| remote-method | 远程搜索的方法 | Function | - |
| loading | 当前是否正在远程搜索 | Boolean | false |
| loading-text | 远程搜索中的文字提示 | String | 加载中 |
| label | 仅在 remote 模式下，初始化时使用。因为仅通过 value 无法得知选项的label，需手动设置。| String·Number·Array | - |
| placeholder | 选择框默认文字 | String | 请选择 |
| not-found-text | 当下拉列表为空时显示的内容 | String | 无匹配数据 |
| label-in-value | 在返回选项时，是否将 label 和 value 一并返回，默认只返回 value | Boolean | false |

## Select Event


| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---------|----------|-----------|-----------|-----------|
| on-change | 选中的值发生变化时触发的时间 | Function | - | - |
