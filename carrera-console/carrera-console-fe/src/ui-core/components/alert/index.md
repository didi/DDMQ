---
category: Components
subtitle: 全局消息
type: FeedBack
noinstant: true
title: Alert
tag:
 - test
---

# Alert ⚠️警告

警告提示，展现需要关注的信息。当某个页面需要向用户显示警告的信息时使用。

非浮层的静态展现形式，始终展现，不会自动消失，用户可以点击关闭。

## 基础用法

::: demo 基本用法

基本使用方法，有四种样式可以选择info、success、warning、error。样式决定的是背景色，边框和icon的颜色。

```html
<style>
  .code-block__instance .bcui-alert {
    margin-bottom: 18px;
  }
</style>
<template>
  <div>
    <bc-alert>An info prompt</bc-alert>
    <bc-alert type="success">A success prompt</bc-alert>
    <bc-alert type="warning">A warning prompt</bc-alert>
    <bc-alert type="error">An error prompt</bc-alert>
  </div>
</template>
<script>
  export default {
    methods: {
      showMessage1() {
        this.$message.info('this is a normal message');
      },
    },
  }
</script>
```
:::

## 自定义描述信息

:::demo 自定义

自定义\<slot name="description"\>描述内容。

```html
<template>
  <div>
    <bc-alert>
      An info prompt
      <template slot="description">Content of prompt. Content of prompt. Content of prompt. Content of prompt. </template>
  </bc-alert>
  <bc-alert type="success">
      A success prompt
      <template slot="description">Content of prompt. Content of prompt. Content of prompt. Content of prompt. </template>
  </bc-alert>
  <bc-alert type="warning">
      A warning prompt
      <template slot="description">
      Content of prompt. Content of prompt. Content of prompt.
  </template>
  </bc-alert>
  <bc-alert type="error">
    An error prompt
    <span slot="description">
      Custom error description copywriting. <bc-icon type="help-circled" size="14"></bc-icon>
    </span>
  </bc-alert>
  </div>
</template>
<script>
  export default {
  }
</script>
```
:::

## 自定义图标

:::demo

根据 type 属性自动添加不同图标，或者自定义 `iconTpye`。

```html
<template>
  <div>
    <bc-alert show-icon>An info prompt</bc-alert>
    <bc-alert type="success" show-icon>A success prompt</bc-alert>
    <bc-alert type="warning" show-icon>A warning prompt</bc-alert>
    <bc-alert type="error" show-icon>An error prompt</bc-alert>
    <bc-alert show-icon>
        An info prompt
        <template slot="description">Content of prompt. Content of prompt. Content of prompt. Content of prompt. </template>
    </bc-alert>
    <bc-alert type="success" show-icon>
        A success prompt
        <span slot="description">Content of prompt. Content of prompt. Content of prompt. Content of prompt. </span>
    </bc-alert>
    <bc-alert type="warning" show-icon>
        A warning prompt
        <template slot="description">
        Content of prompt. Content of prompt. Content of prompt.
    </template>
    </bc-alert>
    <bc-alert type="error" show-icon>
        An error prompt
        <span slot="description">
            Custom error description copywriting.
        </span>
    </bc-alert>
    <bc-alert show-icon iconType="quxiao">
        Custom icon
        <template slot="description">Custom icon copywriting. Custom icon copywriting. Custom icon copywriting. </template>
    </bc-alert>
  </div>
</template>
```
:::


##  API


### Alert Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
| ---------|----------|--------- |--------- |--------- |
| type | 警告提示的类型 | String | info | `info`、`success`、`warning`、`error` |
| closeable | 是否可关闭，默认不显示关闭按钮 | boolean | false | - |
| closeText |	自定义关闭按钮 | String |	- | - |
| showIcon | 是否显示辅助图标	| Boolean |	true | - |
| iconType | 自定义图标类型，showIcon 为 true 时有效 | String | - |

### Alert Slots

| 名称 | 说明 |
|-----|-----|
| description |	警告提示的辅助性文字介绍 |

### Alert Event

| 事件名称 | 说明 | 回调参数 |
|----|----|----|
| on-close | 关闭时触发的回调函数 (e: MouseEvent) => void | - |

