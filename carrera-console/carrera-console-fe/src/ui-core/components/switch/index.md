---
category: Components
subtitle: 开关
type: Form
noinstant: true
title: Switch
tag:
 - test
---

# Switch 开关

在两种状态间切换时用到的开关选择器。


## 基础用法

::: demo asdfasdf

```html
<template>
  <bc-switch v-model="value" @on-change="change"></bc-switch>
</template>
<script>
  export default {
    data() {
      return {
        value: true,
        checkedValue: 100,
        checkedValue2: '100',
      };
    },
    methods: {
      change(value) {
        this.$message.info('changed!' + value + 'typeof: ' + typeof value);
      },
    },
  }
</script>

```
:::

## 尺寸


::: demo 开关的尺寸

提供是三种尺寸： `large`, `default` 和 `small`


```html
<template>
  <div>
    <bc-switch size="large"></bc-switch>
    <bc-switch></bc-switch>
    <bc-switch size="small"></bc-switch>
  </div>
</template>

```
:::

## 文字


::: demo 开关的尺寸

提供是三种尺寸： `large`, `default` 和 `small`


```html
<template>
  <div>
    <bc-switch size="large">
      <span slot="open">开启</span>
      <span slot="close">关闭</span>
    </bc-switch>
    <bc-switch>
      <span slot="open">开</span>
      <span slot="close">关</span>
    </bc-switch>
    <bc-switch size="small"></bc-switch>
  </div>
</template>

```
:::

## 禁用


::: demo 禁用开关

设置`disabled`


```html
<template>
  <div>
    <bc-switch size="large" :disabled="true">
      <span slot="open">开启</span>
      <span slot="close">关闭</span>
    </bc-switch>
    <bc-switch :disabled="true">
      <span slot="open">开</span>
      <span slot="close">关</span>
    </bc-switch>
    <bc-switch size="small" :disabled="true"></bc-switch>
  </div>
</template>

```
:::


## 自定义值

::: demo 禁用开关

默认情况下，switch 组件返回的是Boolean，如果有需要可以添加`checkedValue` 和 `uncheckedValue` 来定义不同状态返回的值


```html
<template>
  <div>
    <bc-switch
      v-model="checkedValue"
      :checked-value="100"
      :unchecked-value="10"
      @on-change="change">
      <span slot="open">开</span>
      <span slot="close">关</span>
    </bc-switch>
    <bc-switch
      v-model="checkedValue2"
      checked-value="100"
      unchecked-value="10"
      @on-change="change">    <span slot="open">开</span>
      <span slot="close">关</span></bc-switch>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        checkedValue: 100,
        checkedValue2: '100',
      };
    },
    methods: {
      change(value) {
        this.$message.info('changed!' + value + 'typeof: ' + typeof value);
      },
    },
  };
</script>
```
:::



## 自定义颜色

::: demo

可以添加`checkedColor` 和 `uncheckedColor` 来定义不同状态返回的值


```html
<template>
  <div>
    <bc-switch
      v-model="checkedValue"
      :checked-value="100"
      :unchecked-value="10"
      checked-color="red"
      @on-change="change">
      <span slot="open">开</span>
      <span slot="close">关</span>
    </bc-switch>
    <bc-switch
      v-model="checkedValue2"
      checked-value="100"
      unchecked-value="10"
      unchecked-color="orange"
      @on-change="change">
      <span slot="open">开</span>
      <span slot="close">关</span></bc-switch>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        checkedValue: 100,
        checkedValue2: '100',
      };
    },
    methods: {
      change(value) {
        this.$message.info('changed!' + value + 'typeof: ' + typeof value);
      },
    },
  };
</script>
```
:::

