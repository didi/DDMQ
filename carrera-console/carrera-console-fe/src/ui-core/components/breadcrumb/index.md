---
category: Components
subtitle: 面包屑
type: Navigation
noinstant: true
title: Breadcrumb
tag:
 - test
---

# Breadcrumb 面包屑

显示当前页面在系统层级结构中的位置，并能向上返回。

## 基础用法


面包屑导航的基本用法

::: demo

使用 `href` 设置跳转的链接，使用 `to` 设置跳转的Vue-Router对象

```html
<template>
  <bc-breadcrumb>
    <bc-breadcrumb-item href="/site">首页</bc-breadcrumb-item>
    <bc-breadcrumb-item :route="{name: 'theme'}">主题</bc-breadcrumb-item>
    <bc-breadcrumb-item >面包屑</bc-breadcrumb-item>
  </bc-breadcrumb>
</template>

```
:::

## Breadcrumb Attributes

## Breadcrumb Item Attributes

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| href | 绑定跳转的链接 | String | - | - |
| route | 绑定跳转的 Vue-Router 路由 | Object | - | - |
