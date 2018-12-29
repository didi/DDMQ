---
category: Components
subtitle: 栅格
type: General
noinstant: true
title: Grid
---

# 栅格

24 栅格系统。

## 概述

布局的栅格化系统，我们是基于行（row）和列（col）来定义信息区块的外部框架，以保证页面的每个区域能够稳健地排布起来。下面简单介绍一下它的工作原理：

* 通过row在水平方向建立一组column（简写col）
* 你的内容应当放置于col内，并且，只有col可以作为row的直接元素
* 栅格系统中的列是指1到24的值来表示其跨越的范围。例如，三个等宽的列可以使用.col-8来创建
* 如果一个row中的col总和超过 24，那么多余的col会作为一个整体另起一行排列

## 基础栅格

:::demo 从堆叠到水平排列

使用单一的一组 Row 和 Col 栅格组件，就可以创建一个基本的栅格系统，所有列（Col）必须放在 Row 内。

```html
<style>
  .gutter-box {
    background: #00A0E9;
    padding: 10px 0;
    text-align: center;
    color: #fff;
  }

  .gutter-box {
    margin-top: 8px;
    margin-bottom: 8px;
  }

  .bcui-row>div.gutter-row:nth-child(odd) .gutter-box {
    background: rgba(0, 160, 233, 0.7);
  }

</style>
<template>
  <div class="gutter-example">
    <bc-row >
      <bc-col class="gutter-row" span="24">
        <div :class="'gutter-box'">col-24</div>
      </bc-col>
    </bc-row>
    <bc-row >
      <bc-col class="gutter-row" span="12">
        <div :class="'gutter-box'">col-12</div>
      </bc-col>
      <bc-col class="gutter-row" span="12">
        <div :class="'gutter-box'">col-12</div>
      </bc-col>
    </bc-row>
    <bc-row>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
    </bc-row>
    <bc-row>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
    </bc-row>
    <bc-row>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
    </bc-row>
  </div>
</template>
```
:::

## 间隔

:::demo 区块间隔

可以使用 Row 的 gutter 属性，我们推荐使用 (16+8n)px 作为栅格间隔。(n 是自然数)。

```html
<style>
  .gutter-box {
    background: #00A0E9;
    padding: 10px 0;
    text-align: center;
    color: #fff;
  }

  .gutter-box {
    margin-top: 8px;
    margin-bottom: 8px;
  }

  .bcui-row>div.gutter-row:nth-child(odd) .gutter-box {
    background: rgba(0, 160, 233, 0.7);
  }

</style>
<template>
  <div class="gutter-example">
    <bc-row>
      <bc-col class="gutter-row" span="24">
        <div :class="'gutter-box'">col-24</div>
      </bc-col>
    </bc-row>
    <bc-row :gutter="8" >
      <bc-col class="gutter-row" span="12">
        <div :class="'gutter-box'">col-12</div>
      </bc-col>
      <bc-col class="gutter-row" span="12">
        <div :class="'gutter-box'">col-12</div>
      </bc-col>
    </bc-row>
    <bc-row :gutter="12">
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
    </bc-row>
    <bc-row :gutter="24">
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6">
        <div :class="'gutter-box'">col-6</div>
      </bc-col>
    </bc-row>
    <bc-row :gutter="48">
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
      <bc-col class="gutter-row" span="4">
        <div :class="'gutter-box'">col-4</div>
      </bc-col>
    </bc-row>
  </div>
</template>
```
:::

## 左右偏移

:::demo 区块间隔

使用 offset 可以将列向右侧偏。例如，offset={4} 将元素向右侧偏移了 4 个列（column）的宽度。

```html
<template>
  <div>
    <bc-row>
      <bc-col class="gutter-row" span="8">
        <div :class="'gutter-box'">col-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8" offset="8">
        <div :class="'gutter-box'">col-8  col-offset-8</div>
      </bc-col>
    </bc-row>
    <bc-row>
      <bc-col class="gutter-row" span="6" offset="6">
        <div :class="'gutter-box'">col-6 col-offset-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6" offset="6">
        <div :class="'gutter-box'">col-6 col-offset-6</div>
      </bc-col>
    </bc-row>
  </div>
</template>
```
:::

## 排序

:::demo 栅格排序

通过使用 push 和 pull 类就可以很容易的改变列（column）的顺序。

```html
<template>
  <div>
    <bc-row>
      <bc-col class="gutter-row" span="8" push="8">
        <div :class="'gutter-box'">col-8 col-push-8</div>
      </bc-col>
      <bc-col class="gutter-row" span="8" pull="8">
        <div :class="'gutter-box'">col-8  col-pull-8</div>
      </bc-col>
    </bc-row>
    <bc-row>
      <bc-col class="gutter-row" span="6" offset="6">
        <div :class="'gutter-box'">col-6 col-offset-6</div>
      </bc-col>
      <bc-col class="gutter-row" span="6" offset="6">
        <div :class="'gutter-box'">col-6 col-offset-6</div>
      </bc-col>
    </bc-row>
  </div>
</template>
```
:::


## API

### Row props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| gutter | 栅格间隔，可以写成像素值 | Number | 0 |

## Col Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| span | 栅格占位格数，为 0 时相当于 display: none | number | 0 |
| offset | 栅格左侧的间隔格数，间隔内不可以有栅格 | number  | 0 |
| pull | 栅格向左移动格数  | number  | 0 |
| push | 栅格向右移动格数  | number  | 0 |
