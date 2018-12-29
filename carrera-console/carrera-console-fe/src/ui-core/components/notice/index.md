---
category: Components
subtitle: 全局通知
type: FeedBack
noinstant: true
title: Notice
tag:
 - test
---

# Notice 系统通知

全局展示通知提醒信息。

## 用法

::: demo 基本用法

```html
<template>
  <bc-button @click="showNotice1">普通通知</bc-button>
</template>
<script>
  export default {
    methods: {
      showNotice1() {
        this.$notice.info({
          title: 'this is a normal Notice',
          desc: '这里是描述',
        });
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
  <bc-button @click="showSuccessNotice">成功</bc-button>
  <bc-button @click="showErrorNotice">失败</bc-button>
  <bc-button @click="showWarningNotice">警告</bc-button>
</template>
<script>
  export default {
    methods: {
      showSuccessNotice() {
        this.$notice.success({
          title: '操作成功',
          desc: '这里是描述',
        });
      },
      showErrorNotice() {
        this.$notice.error({
          title: '操作失败',
          desc: '这里是描述',
        });
      },
      showWarningNotice() {
        this.$notice.warning({
          title: '警告：非法操作',
          desc: '这里是描述',
        });
      },
    },
  }
</script>
```
:::


## Notice API

组件提供了一些静态方法，使用方式和参数如下：

* notice.success(options)
* notice.error(options)
* notice.info(options)
* notice.warning(options)
* notice.warn(options) // alias of warning
* notice.loading(options)
* notice.close(name) // 手动关闭

options 参数

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
| ---------|----------|---------|--------|--------|
| title | 通知标题 | String | - | - |
| desc | 通知内容 | String | - | - |
| name | 通知实例的标记，多用于手动 | String | - | - |
| duration | 通知显示的持续时间，单位：毫秒 | Number | 4500 | 设置为0时将一直显示，除非手动关闭 |
| onClose | 通知关闭时触发的函数 | Function | - | - |


## 全局设置

还提供了全局配置和全局销毁方法：

```js
notice.config(options)
notice.destroy();

notice.config({
  top: 100,
  duration: 2000,
});
```


| 参数 | 说明 | 类型 | 默认值 | 可选值 |
| ---------|----------|--------- |--------- |--------- |
| duration | 通知显示的持续时间，单位：毫秒 | Number | 4500 | 设置为0时将一直显示，除非手动关闭 |
| top | 通知距离顶部的位置 | Number | 24px | - |


