---
category: Components
subtitle: 文字提示
type: FeedBack
noinstant: true
title: Tooltip
tag:
  - gsxs
---

# Tooltip 文字提示

简单的文字提示气泡框。

鼠标移入则显示提示，移出消失，气泡浮层不承载复杂文本和操作。

可用来代替系统默认的 title 提示，提供一个按钮/文字/操作的文案解释。

## 基础用法

:::demo 基础用法

浮层大大小由内容决定

```html
<template>
  <div>
    <bc-tooltip content="存心说谎固不可，开口赌咒亦不可。——史典">
      <span>存心说谎固不可，开口赌咒亦不可。——史典</span>
    </bc-tooltip>
    <br>
    <bc-tooltip content="吵架最激烈不过一分钟，而那一分钟你说出的话，是你用一百分钟都弥补不回来的。唉，真的是这样…">
      <span>吵架...</span>
    </bc-tooltip>
  </div>
</template>
```

:::

## 位置

::: demo 支持多个角度

支持 12 种位置。

```html
<style lang="less">
  .tooltip-pos-demo {
    .top, .bottom {
      margin-left: 50px;
      .bcui-tooltip {
        margin: 0 4px;
      }
    }
    .horizontal {
      width: 282px;
      .bcui-tooltip {
        margin: 4px 0;
      }
    }
  }
</style>
<template>
  <div class="tooltip-pos-demo cleafix">
    <div class="top">
      <bc-tooltip content="孩子，人傻不能复生。" placement="top-start">
        <bc-button>上左</bc-button>
      </bc-tooltip>
      <bc-tooltip content="看不见的东西固然可怕，但人心不是更可怕吗" placement="top">
        <bc-button>上边</bc-button>
      </bc-tooltip>
      <bc-tooltip content="要在江湖混，最好是光棍" placement="top-end">
        <bc-button>上右</bc-button>
      </bc-tooltip>
    </div>
    <div class="horizontal clearfix">
      <div class="left pull-left">
        <bc-tooltip content="世上无难事，只要肯放弃" placement="left-start">
          <bc-button>左上</bc-button>
        </bc-tooltip><br>
        <bc-tooltip content="万事开头难，中间难，结尾难" placement="left">
          <bc-button>左边</bc-button>
        </bc-tooltip><br>
        <bc-tooltip content="提示内容" placement="left-end">
          <bc-button>左下</bc-button>
        </bc-tooltip>
      </div>
      <div class="right pull-right">
        <bc-tooltip content="提示内容" placement="right-start">
          <bc-button>右上</bc-button>
        </bc-tooltip><br>
        <bc-tooltip content="提示内容" placement="right">
          <bc-button>右边</bc-button>
        </bc-tooltip><br>
        <bc-tooltip content="提示内容" placement="right-end">
          <bc-button>右下</bc-button>
        </bc-tooltip>
      </div>
    </div>
    <div class="bottom">
      <bc-tooltip content="提示内容" placement="bottom-start">
        <bc-button>下左</bc-button>
      </bc-tooltip>
      <bc-tooltip content="提示内容" placement="bottom">
        <bc-button>下边</bc-button>
      </bc-tooltip>
      <bc-tooltip content="提示内容" placement="bottom-end">
        <bc-button>下右</bc-button>
      </bc-tooltip>
    </div>
  </div>
</template>
<script>
  export default {};
</script>
```

:::


## API

### Tooltip Props

| 参数      | 说明                                          | 类型    | 默认值                                                                                                                            | 可选值 |
| --------- | --------------------------------------------- | ------- | --------------------------------------------------------------------------------------------------------------------------------- | ------ |
| value     | 是否显示，可使用 v-model 双向绑定数据。       | Boolean | false                                                                                                                             |
| trigger   | 触发方式，可选值为 hover（悬停）click（点击） | String  | click                                                                                                                             |
| placement | 提示框出现的位置                              | top     | `top` `top-start` `top-end` `bottom` `bottom-start` `bottom-end` `left` `left-start` `left-end` `right` `right-start` `right-end` |
| title     | 标题                                          | String  | -                                                                                                                                 |
| content   | 内容                                          | String  | -                                                                                                                                 |

### Tooltip Slot

| 名称    | 说明                                           |
| ------- | ---------------------------------------------- |
| content | 自定义内容（可解析 html 标签，slot="content"） |
