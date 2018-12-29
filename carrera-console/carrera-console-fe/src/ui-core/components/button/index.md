---
category: Components
subtitle: 按钮
type: General
noinstant: true
title: Button
---

# Button 按钮

## 基础用法

::: demo 基本用法

基础的按钮用法.

```html
<template>
  <div>
    <bc-button>默认按钮</bc-button>
    <bc-button type="primary">主要按钮</bc-button>
    <bc-button type="text">文字按钮</bc-button>
  </div>
</template>
```
:::

## 禁用状态

::: demo

设置`disabled`为`true`，可将按钮设置为不可用状态。如果写入操作，页面上显示禁用状态的同时，请求接口值之前一定需要做校验，同时后端也需要对参数校验，避免通过控制台修改disable状态来发起恶意提交。

```html
<template>
  <div>
    <bc-button :disabled="true">默认按钮</bc-button>
    <bc-button type="primary" :disabled="true">主要按钮</bc-button>
    <bc-button type="text" :disabled="true">文字按钮</bc-button>
  </div>
</template>
```
:::

## 尺寸

::: demo 不同尺寸的按钮

除了默认的大小，按钮还有：long，large，small三种。Long 可以和large、small组合使用。

```html
<template>
  <div>
    <bc-button size="large">Large</bc-button>
    <bc-button>Normal</bc-button>
    <bc-button size="small">Small</bc-button>
    <br>
    <br>
    <bc-button long>Long</bc-button>
    <br>
    <br>
    <bc-button long size="large">Large Long</bc-button>
  </div>
</template>
<script>
  export default {
    data(){
      return {};
    },
    methods: {
      test() {
      },
    }
  };
</script>
```
:::

## 其他颜色

::: demo

按钮的其他类型。

```html
<template>
  <div>
    <bc-button type="primary">primary 按钮</bc-button>
    <bc-button type="info">info 按钮</bc-button>
    <bc-button type="success">success 按钮</bc-button>
    <bc-button type="warning">warning 按钮</bc-button>
    <bc-button type="error">error 按钮</bc-button>
  </div>
</template>
```
:::

## 按钮图标

::: demo 添加图标

设置 `icon` 可以在按钮的文字前面添加一个图标

```html
<template>
  <div>
    <bc-button icon="backup"></bc-button>
    <bc-button icon="arrow-left-double"></bc-button>
    <bc-button type="primary" icon="attend">关注</bc-button>
  </div>
</template>
```
:::

## 文字按钮

::: demo 没有边框和背景色的按钮

设置` type` 为 `text`。

```html
<template>
  <div>
    <bc-button type="text">文字按钮</bc-button>
    <bc-button type="text" disabled>文字按钮</bc-button>
  </div>
</template>
```
:::

## 加载中的按钮

::: demo 加载

设置`loading`可以在按钮的文字前面添加一个loadin图标，通常用于提交表单处。

```html
<template>
  <div>
    <bc-button :loading="true">加载中</bc-button>
    <bc-button size="small" :loading="true">加载中</bc-button>
    <br>
    <br>
    <bc-button long :loading="true" type="primary">提交中</bc-button>
  </div>
</template>
```
:::
