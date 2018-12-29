---
category: Components
subtitle: 侧边滑层
type: FeedBack
noinstant: true
title: Drawer
tag:
 - nav
---

# Drawer 下拉菜单


## 基础用法

::: demo 基础用法

最简单的使用方法，通过控制属性visible来显示 / 隐藏对话框。可以使用 v-model 实现双向绑定。默认按键盘ESC键也可以关闭

```html
<template>
  <div>
    <bc-button type="primary" @click="modal1 = true">侧边滑层</bc-button>
    <bc-button type="primary" @click="title = '普通的Drawer对话框标题'">设置标题</bc-button>
    <bc-drawer
      v-model="modal1"
      :title="title"
      @on-ok="ok"
      @on-cancel="cancel">
      <p>对话框内容</p>
      <p>对话框内容</p>
      <p>对话框内容</p>
    </bc-drawer>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        modal1: false,
        title: '',
      }
    },
    methods: {
      ok () {
        this.$message.info('点击了确定');
      },
      cancel () {
        this.$message.info('点击了取消');
      }
    }
  }
</script>
```
:::
