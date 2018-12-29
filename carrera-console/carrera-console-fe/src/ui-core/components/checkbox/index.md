---
category: Components
subtitle: 多选框
type: Form
noinstant: true
title: Checkbox
tag:
 - test
---

# Checkbox 多选框

用于一组可选项多项选择，或者单独用于标记切换某种状态


## 基础用法


::: demo

单独使用, 可以使用 v-model 可以双向绑定数据。

```html
<template>
  <bc-checkbox v-model="value1">是</bc-checkbox>
  <bc-button @click="method1">切换</bc-button>
</template>
<script>
  export default {
    data() {
      return {
        value1: true,
      }
    },
    methods: {
      method1() {
        this.value1 = !this.value1;
      },
    },
  }
</script>

```
:::


## 组


::: demo


```html
<template>
  <bc-checkbox-group v-model="animal">
    <bc-checkbox label="小鸡">小鸡🐥</bc-checkbox>
    <bc-checkbox label="小鸭">小鸭🦆</bc-checkbox>
    <bc-checkbox label="小狗">小狗🐶</bc-checkbox>
  </bc-radio-group>
  <div>{{animal}}</div>
  <bc-button @click="method2">手动设置</bc-button>
</template>
<script>
  export default {
    data() {
      return {
        animal: ['小鸡'],
      };
    },
    methods: {
      method2() {
        let index = this.animal.indexOf('小狗');
        if (index >=0) {
          this.animal.splice(index, 1);
        } else {
          this.animal.push('小狗');
        }
      },
    }
  }
</script>
```
:::

## 禁用状态

::: demo


```html
<template>
  <bc-checkbox-group v-model="valid">
    <bc-checkbox label="禁用" disabled>禁用</bc-checkbox>
    <bc-checkbox label="可选" disabled>可选</bc-checkbox>
  </bc-checkbox-group>
</template>
<script>
  export default {
    data() {
      return {
        valid: ['可选'],
      };
    },
  }
</script>
```
:::


