---
category: Components
subtitle: 单选框
type: Form
noinstant: true
title: Radio
tag:
 - test
---

# Radio 单选框

进行标记和分类的小标签。用于标记事物的属性和维度。


## 基础用法


::: demo 单独使用

可以使用`v-model`双向绑定


```html
<template>
  <bc-radio v-model="single">单选项</bc-radio>
</template>
<script>
  export default {
    data() {
      return {
        single: false,
      };
    },
  };
</script>

```
:::


## 组


::: demo

在组合使用时，Radio 使用 label 来自动判断。每个 Radio 的内容可以自定义，如不填写则默认使用 label 的值。

```html
<template>
  <div>
    <bc-radio-group v-model="animal">
      <bc-radio label="小鸡">小鸡🐥</bc-radio>
      <bc-radio label="小鸭">小鸭🦆</bc-radio>
      <bc-radio label="小狗">小狗🐶</bc-radio>
    </bc-radio-group>
    <div>{{animal}}</div>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        animal: '小鸡',
      };
    },
    methods: {
      closeIt(event, name) {
        alert(event, name);
      },
    },
  };
</script>
```
:::

## 禁用状态

::: demo

通过设置disabled属性来禁用单选框。

```html
<template>
  <div>
    <div>
      <bc-radio v-model="single" disabled>禁用</bc-radio>
    </div>
    <div>
      <bc-radio-group v-model="valid">
        <bc-radio label="禁用" disabled>禁用</bc-radio>
        <bc-radio label="可选" disabled>可选</bc-radio>
      </bc-radio-group>
    </div>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        single: false,
        valid: '可选',
      };
    },
  };
</script>
```
:::

## 返回值的使用

:::demo

返回值是`bc-radio`绑定的值，可以是 `String`、`Number` 或者 `Boolean`。

```html
<template>
  <div>
    <bc-radio-group v-model="selected">
      <bc-radio :label="0">香蕉</bc-radio>
      <bc-radio label="1">苹果</bc-radio>
      <bc-radio :label="2">哈密瓜</bc-radio>
    </bc-radio-group>
    <span>选择值：{{selected}}，类型: {{typeof selected}}</span>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        selected: '',
      };
    },
  };
</script>
```

:::

## 按钮样式

::: demo

多用于表单中。


```html
<template>
  <div>
    <bc-radio-group v-model="fruit" type="button">
      <bc-radio label="香蕉">香蕉</bc-radio>
      <bc-radio label="苹果" disabled>苹果</bc-radio>
      <bc-radio label="哈密瓜">哈密瓜</bc-radio>
    </bc-radio-group>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        fruit: '香蕉',
      };
    },
    methods: {
    },
  };
</script>
```
:::


## Radio props


| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| value | 只在单独使用时有效。可以使用`v-model`双向绑定数据 | String\|Number\|Boolean | - |
| label | 只在组合使用时有效。指定当前选项的`value`值，组合会自动判断当前选择的项目 | String\|Number\|Boolean | - |
| disabled | 是否禁用当前项`Boolean` | false |
| size | 单选框的尺寸，可选值为 large、small、default 或者不设置 | String | - |

