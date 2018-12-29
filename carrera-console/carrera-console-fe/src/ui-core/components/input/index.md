---
category: Components
subtitle: 输入框
type: Form
noinstant: true
title: Input
tag:
 - test
---

进行标记和分类的小标签。用于标记事物的属性和维度。


## 基础用法




::: demo


```html
<template>
  <bc-input placeholder="请输入" icon="go" />
</template>

```
:::


## 尺寸


::: demo


```html
<template>
  <div>
    <bc-input size="small" ></bc-input>
    <bc-input ></bc-input>
    <bc-input size="large"></bc-input>
  </div>
</template>
<script>
  export default {
    data() {
      return {};
    },
    methods: {
      closeIt(event, name) {
        alert(event, name);
      }
    }
  }
</script>
```
:::


## AutoFocus


::: demo
当有多个focus时，最后一个才会自动focus，不支持多个


```html
<template>
  <div>
    <bc-input auto-focus placeholder="auto focus"></bc-input>
    <bc-input auto-focus placeholder="auto focus"></bc-input>
  </div>
</template>
<script>
  export default {
    data() {
      return {};
    },
    methods: {
      closeIt(event, name) {
        alert(event, name);
      }
    }
  }
</script>
```
:::

## 事件监听

:::demo 事件监听

支持部分事件

```html
<template>
    <bc-input v-model="value.test" @on-change="handleChange(value)"></bc-input>
</template>
<script>
  export default {
    data() {
      return {
        value: {
          test: 50,
        },
      };
    },
    methods: {
      handleChange(value) {
        console.log(value);
        if (value.test > 100) {
          value.test = 100;
        }
      },
    },

  }
</script>
```

:::

## 复合型输入框

::: demo


```html
<template>
  <div>
    <bc-input
      v-model="inputValue"
      >
    <bc-select
      slot="select"
      v-model="filter"
    >
      <bc-option v-for="(item, i) in searchKeywords" :value="item.key" :key="item.key">{{item.label}}</bc-option>
    </bc-select>
  </bc-input>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        searchKeywords: [
          {
            key: 'name',
            label: '姓名'
          },
          {
            key: 'department',
            label: '部门'
          },
        ],

        filter: '',
        inputValue: '',
      };
    },
  }
</script>
```
:::

## Input  Attributes

