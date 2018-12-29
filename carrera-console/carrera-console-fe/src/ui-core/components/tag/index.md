---
category: Components
subtitle: 标签
type: Data View
noinstant: true
title: Tag
tag:
 - test
---

# Tag 标签

进行标记和分类的小标签。用于标记事物的属性和维度。


## 基础用法


标签

::: demo

基本标签的用法，设置color决定tag的颜色。color可以是单词，比如：red， orange，也可以是Hex值，比如：#f50，#2db7f5。
```html
<template>

  Color：
  <bc-tag color="orange">orange</bc-tag>
  <bc-tag color="yellow">yellow</bc-tag>
  <bc-tag color="green">green</bc-tag>
  <bc-tag color="cyan">cyan</bc-tag>
  <bc-tag color="purple">purple</bc-tag>

  Hex:
  <bc-tag color="#f50">#f50</bc-tag>
  <bc-tag color="#2db7f5">#2db7f5</bc-tag>
  <bc-tag color="#87d068">#87d068</bc-tag>


  <bc-tag color="primary" :closable="true">primary</bc-tag>
  <bc-tag color="success" :closable="true">primary</bc-tag>
  <bc-tag color="warning" :closable="true">primary</bc-tag>

</template>

```
:::


## 关闭事件

可以通过添加 closable 变为可关闭标签。可关闭标签具有 onClose afterClose 两个事件。

::: demo

可以通过添加 closable 变为可关闭标签。可关闭标签具有onClose和afterClose两个事件。

```html
<template>
  <bc-tag color="#2db7f5" closable="true" @on-close="closeIt">标签一</bc-tag>
</template>
<script>
  export default {
    data() {
      return {};
    },
    methods: {
      closeIt(event, name) {
        alert(event, name);
      }
    }
  }
</script>
```
:::

## Tag Attributes

