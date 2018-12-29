---
category: Components
subtitle: 气泡
type: FeedBack
noinstant: true
title: Popover
tag:
 - gsxs
---

# Popover 气泡菜单


## 基础用法

:::demo 基础用法

浮层大大小由内容决定

```html
<template>
  <bc-popover trigger="hover" title="提示标题" content="提示内容, 提示内容, 提示内容">
    <bc-button typer="primary">hover</bc-button>
  </bc-popover>
</template>
```
:::

## 手动关闭

:::demo 手动关闭窗口

通过 `v-model` 来手动关闭

```html
<template>
  <bc-popover v-model="visible" title="提示标题">
    <bc-button typer="primary">click</bc-button>
    <div slot="content">
      <span @click="toggle">close it</span>
    </div>
  </bc-popover>
</template>
<script>
  export default {
    data() {
      return {
        visible: false,
      };
    },
    methods: {
      toggle() {
        this.visible = false;
      }
    },
  }
</script>
```

:::
## 触发方式

:::demo 两种触发方式

鼠标移入、点击

```html
<template>
  <div>
    <bc-popover trigger="hover" title="提示标题" content="提示内容">
      <bc-button typer="primary">hover</bc-button>
    </bc-popover>
    <bc-popover trigger="click" title="提示标题" content="提示内容">
      <bc-button typer="primary">click</bc-button>
    </bc-popover>
  </div>
</template>
```
:::

## 位置

::: demo 支持多个角度

支持12种位置。

```html
<style lang="less">
  .popover-pos-demo {
    .top, .bottom {
      margin-left: 50px;
      .bcui-popover {
        margin: 0 4px;
      }
    }
    .horizontal {
      width: 282px;
      .bcui-popover {
        margin: 4px 0;
      }
    }
  }
</style>
<template>
  <div class="popover-pos-demo cleafix">
    <div class="top">
      <bc-popover title="提示标题" content="提示内容" placement="top-start">
        <bc-button>上左</bc-button>
      </bc-popover>
      <bc-popover title="提示标题" content="提示内容" placement="top">
        <bc-button>上边</bc-button>
      </bc-popover>
      <bc-popover title="提示标题" content="提示内容" placement="top-end">
        <bc-button>上右</bc-button>
      </bc-popover>
    </div>
    <div class="horizontal clearfix">
      <div class="left pull-left">
        <bc-popover title="提示标题" content="提示内容" placement="left-start">
          <bc-button>左上</bc-button>
        </bc-popover><br>
        <bc-popover title="提示标题" content="提示内容" placement="left">
          <bc-button>左边</bc-button>
        </bc-popover><br>
        <bc-popover title="提示标题" content="提示内容" placement="left-end">
          <bc-button>左下</bc-button>
        </bc-popover>
      </div>
      <div class="right pull-right">
        <bc-popover title="提示标题" content="提示内容" placement="right-start">
          <bc-button>右上</bc-button>
        </bc-popover><br>
        <bc-popover title="提示标题" content="提示内容" placement="right">
          <bc-button>右边</bc-button>
        </bc-popover><br>
        <bc-popover title="提示标题" content="提示内容" placement="right-end">
          <bc-button>右下</bc-button>
        </bc-popover>
      </div>
    </div>
    <div class="bottom">
      <bc-popover title="提示标题" content="提示内容" placement="bottom-start">
        <bc-button>下左</bc-button>
      </bc-popover>
      <bc-popover title="提示标题" content="提示内容" placement="bottom">
        <bc-button>下边</bc-button>
      </bc-popover>
      <bc-popover title="提示标题" content="提示内容" placement="bottom-end">
        <bc-button>下右</bc-button>
      </bc-popover>
    </div>
  </div>
</template>
<script>
  export default {}
</script>
```

:::


## API

### Popover Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| value | 是否显示，可使用 v-model 双向绑定数据。 | Boolean | false |
| trigger | 触发方式，可选值为hover（悬停）click（点击）| String | click |
| placement | 提示框出现的位置 | top | `top` `top-start` `top-end` `bottom` `bottom-start` `bottom-end` `left` `left-start` `left-end` `right` `right-start` `right-end`  |
| title | 标题 | String | - |
| content | 内容 | String | - |


### Popover Slot

| 名称 | 说明 |
| ---|--- |
| title | 自定义标题 |
| content | 自定义内容 |


