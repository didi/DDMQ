---
category: Components
subtitle: 标签页
type: Navigation
noinstant: true
title: Tabs
tag:
 - test
 - nav
---

# Tab 标签页

选项卡切换组件

## 基础用法


选项卡的基本用法

::: demo 基本用法

通过v-model绑定

```html
<template>
  <bc-tab v-model="name">
    <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
    <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
    <bc-tab-pane name="third" label="长一点的标签"></bc-tab-pane>
  </bc-tab>
</template>
<script>
  export default {
    data() {
      return {
        name: 'first',
      };
    },
  };
</script>
```
:::

::: demo 卡片样式

设置组件属性`type`为`card`切换至卡片样式。

```html
<template>
  <div>
    <bc-button @click="setValue">set</bc-button>
    <bc-tab v-model="value" type="card">
      <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三"></bc-tab-pane>
    </bc-tab>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        value: 'third',
      };
    },
    methods: {
      setValue(){
        this.value = 'second';
      },
    },
  };
</script>

```
:::


## 监听事件

::: demo 点击事件

监听tab点击事件

```html
<template>
  <bc-tab v-model="tab" @on-tab-click="clickTabs">
    <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
    <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
    <bc-tab-pane name="third" label="标签三"></bc-tab-pane>
  </bc-tab>
</template>
<script>
  export default {
    data() {
      return {
        tab: 'second',
      };
    },
    methods: {
      clickTabs(name) {
        alert(name);
      },
    },
  };
</script>
```
:::

## 禁用

::: demo 禁用


```html
<template>
  <div>
    <bc-button @click="change">切换</bc-button>
    <bc-tab v-model="tab">
     <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三" :disabled="isDisabled"></bc-tab-pane>
    </bc-tab>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        isDisabled: true,
        tab: 'first',
      };
    },
    methods: {
      change() {
        this.isDisabled = !this.isDisabled;
      },
    },
  };
</script>
```
:::

## 关闭标签

::: demo 关闭标签

当 `type=card` 时，可以为标签增加关闭按钮。点击关闭按钮，标签页小时，同时触发 `on-tab-remove` 事件。


```html
<template>
  <div>
    <bc-tab v-model="tab" type="card" @on-tab-remove="handleTabRemove">
     <bc-tab-pane name="first" label="标签一" :closable="isClosable">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二" :closable="isClosable">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三" :closable="isClosable"></bc-tab-pane>
    </bc-tab>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        isClosable: true,
        tab: 'first',
      };
    },
    methods: {
      change() {
        this.isClosable = !this.isClosable;
      },
      handleTabRemove() {
        alert('removed!');
      },
    },
  };
</script>
```
:::

## 添加图标

::: demo 添加图标

有图标的标签。


```html
<template>
  <div>
    <bc-tab v-model="tab" type="card" @on-tab-remove="handleTabRemove">
      <bc-tab-pane name="first" label="标签一" icon="link">链接</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二" icon="taocan">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三" icon="group1"></bc-tab-pane>
    </bc-tab>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        isClosable: true,
        tab: 'first',
      };
    },
    methods: {
      change() {
        this.isClosable = !this.isClosable;
      },
      handleTabRemove() {
      },
    },
  };
</script>
```
:::



## 滚动

::: demo 点击事件

```html
<template>
  <div>
    <bc-tab v-model="tab">
      <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="first2" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second3" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third2" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="first3" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second2" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir1" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="second233" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir13" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="second23" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir144" label="标签三"></bc-tab-pane>
    </bc-tab>
    <bc-tab v-model="tab2" type="card">
      <bc-tab-pane name="first" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="first2" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second3" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="third2" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="first3" label="标签一">第一个tab pane</bc-tab-pane>
      <bc-tab-pane name="second2" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir1" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="second233" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir13" label="标签三"></bc-tab-pane>
      <bc-tab-pane name="second23" label="标签二">第二个tab pane</bc-tab-pane>
      <bc-tab-pane name="thir144" label="标签三"></bc-tab-pane>
    </bc-tab>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        tab: 'first',
        tab2: 'second',
      };
    },
    methods: {
    },
  },
</script>
```
:::


## API

### Tab Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| value 当前激活 tab 面板的 name，可以使用 v-model 双向绑定数据 String  默认为第一项的 name
| type | 页签的基本样式，可选值为 line 和 card  String | line |
| size | 尺寸，可选值为 default 和 small，仅在 type="line" 时有效 | String | default |
| closable | 是否可以关闭页签，仅在 type="card" 时有效. **注意：目前关闭时并不会销毁对应的pane！！** | Boolean | false |
| animated | 是否使用 CSS3 动画 | Boolean | true |
| closable | 是否可以关闭页签，仅在 type="card" 时有效 Boolea | false |

### Tab Event

| 事件名 | 说明 | 参数 | 返回值 |
|---|----|----|----|
| on-tab-click | tab 被点击时触发 | name | - |
| on-tab-remove | tab 被关闭时触发 | name | - |

## Tab Pane Attributes

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| name | 用于标识当前面板，对应 value，默认为其索引值 | String | - |
| label |选项卡头显示文字 | String | Function | 空 |
| icon | 选项卡图标 | String | - |
| disabled | 是否禁用该选项卡 | Boolean | false |
| closable | 是否可以关闭页签，仅在 type="card" 时有效 | Boolean | null |
