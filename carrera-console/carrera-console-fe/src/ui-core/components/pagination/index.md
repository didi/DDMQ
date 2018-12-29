---
category: Components
subtitle: 分页
type: Navigation
noinstant: true
title: Pagination
tag:
 - test
---

# Pagination 分页

采用分页的形式分隔长列表，每次只加载一个页面。

## 基础用法


分页的基础用法

::: demo 基础用法

基础的分页，页数过多时会自动折叠。`total`设置总个数，`current`可用来设置当前页面，也可以通过变量动态调整。

```html
<template>
  <bc-pagination
    :total="100"
    :current="2">
  </bc-pagination>
</template>
```
:::

## 设置每页数量

::: demo 设置每页数量

通过 `page-size` 可每页数量。使用`show-sizer`可以动态设置每页数量。

```html
<template>
  <bc-pagination
    :total="100"
    :page-size="20"
    show-sizer>
  </bc-pagination>
</template>
```
:::

## 显示总数

::: demo 显示总数

可显示项目的总数

```html
<template>
  <bc-pagination
    :total="100"
    show-sizer
    show-total>
  </bc-pagination>
</template>
```
:::

## 快速跳转

::: demo 快速跳转

可以快速跳转到指定的页面

```html
<template>
  <bc-pagination
    :total="100"
    show-sizer
    show-total
    show-elevator>
  </bc-pagination>
</template>

```
:::


## Pagination props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
|current | 当前页数 | Number | - |
|total | 数据总数 | Number | 0 |
|page-size | 每页条数 | Number | 10 |
| show-total | 显示总数 | Boolean | false |
| show-elevator | 显示电梯，可以快速切换到某一页 | Boolean | false |
| show-sizer | 显示分页，用来改变page-size | Boolean | false |

## Pagination Events

| 事件名 | 说明 | 参数 |
| ----|----|--- |
| on-change | 页码改变的回调，参数是改变后的页码及每页条数 | Function(page) |
| on-page-size-change | 切换每页条数时的回调，返回切换后的每页条数 Function(pagesize) |



## Pagination Slot

| 名称 | 说明 |
|---|----|
| 无 | 自定义显示总数的内容 |

