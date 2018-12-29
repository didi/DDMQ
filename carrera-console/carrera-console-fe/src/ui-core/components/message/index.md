---
category: Components
subtitle: 全局消息
type: FeedBack
noinstant: true
title: Message
tag:
 - test
---

# Message 全局消息

全局展示操作反馈信息。常用于主动操作后的反馈提示

## 用法

::: demo 基本用法

```html
<template>
  <bc-button @click="showMessage1">普通消息</bc-button>
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

:::demo 其他提示类型

包括成功、失败、警告。

```html
<template>
  <bc-button @click="showSuccessMessage">成功</bc-button>
  <bc-button @click="showErrorMessage">失败</bc-button>
  <bc-button @click="showWarningMessage">警告</bc-button>
</template>
<script>
  export default {
    methods: {
      showSuccessMessage() {
        this.$message.success('操作成功');
      },
      showErrorMessage() {
        this.$message.error('操作失败');
      },
      showWarningMessage() {
        this.$message.warning('警告：非法操作');
      },
    },
  }
</script>
```
:::


## Message API

组件提供了一些静态方法，使用方式和参数如下：

* message.success(content, duration, onClose)
* message.error(content, duration, onClose)
* message.info(content, duration, onClose)
* message.warning(content, duration, onClose)
* message.warn(content, duration, onClose) // alias of warning
* message.loading(content, duration, onClose)



| 参数 | 说明 | 类型 | 默认值 | 可选值 |
| ---------|----------|---------|--------|--------|
| content | 消息内容 | String | - | - |
| duration | 消息显示的持续时间，单位：毫秒 | Number | 1500 | 设置为0时将一直显示，除非手动关闭 |
| name | 消息实例的标记，多用于手动 | String | - | - |
| onClose | 消息关闭时触发的函数 | Function | - | - |


## 全局设置

还提供了全局配置和全局销毁方法：

```js
message.config(options)
message.destroy();

message.config({
  top: 100,
  duration: 2000,
});
```


| 参数 | 说明 | 类型 | 默认值 | 可选值 |
| ---------|----------|--------- |--------- |--------- |
| duration | 消息显示的持续时间，单位：毫秒 | Number | 1500 | 设置为0时将一直显示，除非手动关闭 |
| top | 消息距离顶部的位置 | Number | 24px | - |


