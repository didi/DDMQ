---
category: Components
subtitle: 卡片
type: Data View
noinstant: true
title: Card
---

# Card 卡片

## 基础用法

::: demo 基本用法

卡片的基本用法，常用于后台概览页面。

```html
<template>
  <div>
    <bc-card :width="300">
    没有标题的卡片
    </bc-card>
    </br>
    </br>
    <bc-card title="卡片标题" :width="300">
    </bc-card>
  </div>

</template>
```
:::


## 自定义

::: demo 自定义标题、额外操作

通过`slot="title"`可以自定义卡片标题的内容。通过`slot="extra"`可以自定义卡片右上角的额外操作区域。

```html
<template>
  <div>
    <bc-card>
      <div slot="title">
        <bc-icon type="group"></bc-icon>自定义标题
      </div>
    </bc-card>
    </br>
    </br>
    <bc-card>
      <div slot="title">
        <bc-icon type="group"></bc-icon>自定义标题
      </div>
      <span slot="extra">更多操作</span>
    </bc-card>
  </div>
</template>
```

:::

## 自定义样式

:::demo 无边框和无阴影

可以通过设置 `bordered: false` 来控制是否添加边框，可以通过设置 `hoverable: true` 来控制是否有阴影。

```html
<template>
  <div>
    <bc-card :bordered="false" :hoverable="true">
      <div slot="title">
        <bc-icon type="group"></bc-icon>不添加边框，添加hover
      </div>
      <div slot="body">指定body</div>
    </bc-card>
    </br>
    </br>
    <bc-card>
      <div slot="title">
        <bc-icon type="group"></bc-icon>自定义标题
      </div>
      <span slot="extra">更多操作</span>
    </bc-card>
  </div>
</template>
```
:::
