---
category: Components
subtitle: 加载
type: FeedBack
noinstant: true
title: Loading
tag:
 - test
---

# Loading 加载

页面局部处于等待异步数据或正在渲染过程时，合适的加载动效会有效缓解用户的焦虑。。

## 基础用法


::: demo 基础用法

最简单的使用方法，通过给一个元素加上 `v-loading` 指令，通过设置值来控制loading的显示与隐藏

```html
<template>
  <bc-button type="primary" @click="isLoading1 = true" :loading="isLoading1">加载</bc-button>
  <div style="height: 300px" v-loading="isLoading1">
    <p>对话框内容</p>
    <p>对话框内容</p>
    <p>对话框内容</p>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        isLoading1: false
      }
    },
  }
</script>
```
:::

## 自定义 Loading 文本

:::demo 自定义

当使用`v-loading`添加loading时，可以使用`loading-text`来设置loading的文字提示。

```html
<template>
  <bc-button type="primary" @click="isLoading2 = true" :loading="isLoading2">加载</bc-button>
  <div style="height: 300px" v-loading="isLoading2" :loading-text="loadingText">
    <p>对话框内容</p>
    <p>对话框内容</p>
    <p>对话框内容</p>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        isLoading2: false,
        loadingText: '拼命加载中',
      }
    },
  }
</script>
```
:::

## Options
