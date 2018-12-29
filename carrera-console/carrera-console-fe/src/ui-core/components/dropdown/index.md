---
category: Components
subtitle: 下拉菜单
type: Navigation
noinstant: true
title: Dropdown
tag:
 - nav
---

# Dropdown 下拉菜单

当页面上的操作命令过多时，用此组件可以收纳操作元素。点击或移入触点，会出现一个下拉菜单。可在列表中进行选择，并执行相应的命令。

## 基础用法

:::demo 基础用法

需要配合 bc-dropdown-menu 和 bc-dropdown-item 两个组件来使用，并且给列表设置具名 slot 为 list。触发对象可以是链接、按钮等各种元素。

```html
<template>
  <div>
    <bc-dropdown>
      <bc-icon type="setting-more"></bc-icon>
      <bc-dropdown-menu slot="list">
        <bc-dropdown-item>驴打滚</bc-dropdown-item>
        <bc-dropdown-item>炸酱面</bc-dropdown-item>
        <bc-dropdown-item disabled>豆汁儿</bc-dropdown-item>
        <bc-dropdown-item>冰糖葫芦</bc-dropdown-item>
        <bc-dropdown-item divided>北京烤鸭</bc-dropdown-item>
      </bc-dropdown-menu>
    </bc-dropdown>
    <bc-dropdown>
      <bc-button type="primary">
        下拉菜单<bc-icon type="xiala"></bc-icon>
      </bc-button>
      <bc-dropdown-menu slot="list">
        <bc-dropdown-item>驴打滚</bc-dropdown-item>
        <bc-dropdown-item>炸酱面</bc-dropdown-item>
        <bc-dropdown-item disabled>豆汁儿</bc-dropdown-item>
        <bc-dropdown-item>冰糖葫芦</bc-dropdown-item>
        <bc-dropdown-item divided>北京烤鸭</bc-dropdown-item>
      </bc-dropdown-menu>
    </bc-dropdown>
  </div>
</template>
```
:::

## 触发方式

:::demo

默认hover触发，可以给bc-dropdown 设置 trigger 为 click 将触发方式修改为点击触发。也可以设置为 custom，此时需要手动设置 visible 属性来控制。

```html
<template>
  <div>
    <bc-dropdown trigger="click">
      <span>
        更多<bc-icon type="setting-more"></bc-icon>
      </span>
      <bc-dropdown-menu slot="list">
        <bc-dropdown-item>驴打滚</bc-dropdown-item>
        <bc-dropdown-item>炸酱面</bc-dropdown-item>
        <bc-dropdown-item disabled>豆汁儿</bc-dropdown-item>
        <bc-dropdown-item>冰糖葫芦</bc-dropdown-item>
        <bc-dropdown-item divided>北京烤鸭</bc-dropdown-item>
      </bc-dropdown-menu>
    </bc-dropdown>
    <bc-dropdown trigger="click">
      <span>
        更多2<bc-icon type="setting-more"></bc-icon>
      </span>
      <bc-dropdown-menu slot="list">
        <bc-dropdown-item>驴打滚</bc-dropdown-item>
        <bc-dropdown-item>炸酱面</bc-dropdown-item>
        <bc-dropdown-item disabled>豆汁儿</bc-dropdown-item>
        <bc-dropdown-item>冰糖葫芦</bc-dropdown-item>
        <bc-dropdown-item divided>北京烤鸭</bc-dropdown-item>
      </bc-dropdown-menu>
    </bc-dropdown>
  </div>
</template>
```
:::

## 事件

:::demo

在 bc-dropdown 上支持两个事件：on-click和on-visible-change。当bc-dropdown-item 点击时，会触发on-click 事件，参数是 item 的 name。当菜单状态变化时，会触发 on-visible-change 事件

```html
<template>
  <div>
    <bc-dropdown trigger="click" @on-click="handleClick">
      <span>
        更多<bc-icon type="setting-more"></bc-icon>
      </span>
      <bc-dropdown-menu slot="list">
        <bc-dropdown-item name="驴打滚">驴打滚</bc-dropdown-item>
        <bc-dropdown-item>炸酱面</bc-dropdown-item>
        <bc-dropdown-item disabled>豆汁儿</bc-dropdown-item>
        <bc-dropdown-item>冰糖葫芦</bc-dropdown-item>
        <bc-dropdown-item divided>北京烤鸭</bc-dropdown-item>
      </bc-dropdown-menu>
    </bc-dropdown>
  </div>
</template>
<script>
  export default {
    methods: {
      handleClick(name) {
        alert(name);
      },
    },
  }
</script>
```
:::
