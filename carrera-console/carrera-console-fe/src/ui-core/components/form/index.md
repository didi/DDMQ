---
category: Components
subtitle: 表单
type: Form
noinstant: true
title: Form
tag:
 - form
---

# Form 表单

具有数据收集、校验和提交功能的表单，包含复选框、单选框、输入框、下拉选择框等元素。


## 基础用法

:::demo 行内表单

设置属性 `inline`, 表单元素可以水平排列

```html
<template>
  <bc-form inline>
    <bc-form-item>
      <bc-input placeholder="输入用户名"></bc-input>
    </bc-form-item>
    <bc-form-item>
      <bc-input placeholder="输入密码"></bc-input>
    </bc-form-item>
    <bc-form-item>
      <bc-button type="primary">登录</bc-button>
    </bc-form-item>
  </bc-form>
</template>
```
:::

## Label的位置

:::demo

默认在顶部，设置 `label-width` 之后，label将在左侧

```html
<template>
  <div>
    <bc-form>
      <bc-form-item label="姓名">
        <bc-input></bc-input>
      </bc-form-item>
    </bc-form>
    <bc-form :label-width="100">
      <bc-form-item label="姓名">
        <bc-input></bc-input>
      </bc-form-item>
    </bc-form>
  </div>
</template>
```

:::



## 自定义宽度和偏移量

:::demo

Form默认宽度是`500px`，可以通过`width`自定义宽度，可以是数字或者百分比。设置`offset`可以设置Form的左侧偏移量

```html
<template>
  <div>
    <p>设置宽度为300</p>
    <bc-form :width="300">
      <bc-form-item label="姓名">
        <bc-input></bc-input>
      </bc-form-item>
    </bc-form>
    <p>默认宽度，同时设置左侧偏移量为100</p>
    <bc-form :offset="100">
      <bc-form-item label="姓名">
        <bc-input></bc-input>
      </bc-form-item>
    </bc-form>
  </div>
</template>
```

:::


## 表单控件

:::demo 常用用法

在 `Form` 内，每个表单域由 `FormItem` 组成，可包含的控件有：Input、Radio、Checkbox、Switch、Select。给 `FormItem` 设置属性 `label` 可以显示表单域的标签，需要给 `Form` 设置 `label-width`。

```html
<template>
  <bc-form
    :label-width="80">
    <bc-form-item label="输入框">
      <bc-input></bc-input>
    </bc-form-item>
    <bc-form-item label="选择器">
      <bc-select></bc-select>
    </bc-form-item>
    <bc-form-item>
      <bc-button type="primary">提交</bc-button>
      <bc-button>取消</bc-button>
    </bc-form-item>
  </bc-form>
</template>
```

:::

## 表单验证

:::demo 表单验证


```html
<template>
  <bc-form
    ref="formValidate"
    :model="formValidate"
    :rules="formValidateRules"
    :label-width="120">
    <bc-form-item label="姓名" prop="username">
      <bc-input v-model="formValidate.username"></bc-input>
    </bc-form-item>
    <bc-form-item label="邮箱" prop="mail">
      <bc-input v-model="formValidate.mail"></bc-input>
    </bc-form-item>
    <bc-form-item label="地址" prop="address">
      <bc-select v-model="formValidate.address">
        <bc-option value="xixigu">address1</bc-option>
        <bc-option value="jiangcun">address2</bc-option>
      </bc-select>
    </bc-form-item>
    <bc-form-item label="描述" prop="comment">
      <bc-input type="textarea" :rows="3" v-model="formValidate.comment"></bc-input>
    </bc-form-item>
    <bc-form-item>
      <bc-button type="primary" @click="submitValidate">提交</bc-button>
      <bc-button type="ghost" @click="resetValidate('formValidate')">重置</bc-button>
    </bc-form-item>
  </bc-form>
</template>
<script>
  export default {
    data() {
      return {
        formValidate: {
          username: '',
          mail: '',
          address: '',
          comment: '',
        },
        formValidateRules: {
          username: { required: true, message: '用户名不能为空' },
          mail: [
            { required: true, message: '邮箱不能为空' },
            { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
          ],
          address: { required: true, message: '地址不能为空' },
          comment: { required: true, message: '描述不能为空' },
        },
      };
    },
    methods: {
      submitValidate() {
        this.$refs['formValidate'].fields.forEach((item) => {
          console.log(item.getRules());
        });
        this.$refs['formValidate'].validate((valid, error) => {
          console.log(valid);
          console.log(error);
          if (valid) {
            this.$message.success('提交成功!');
          } else {
            this.$message.error('表单验证失败!');
          }
        });
      },
      resetValidate(name) {
        this.$refs[name].resetFields();
      },
    },
  };
</script>
```

:::

## 表单字段提示

:::demo 字段提示


```html
<template>
  <bc-form
    ref="formValidate"
    :model="formValidate"
    :rules="formValidateRules"
    :label-width="120">
    <bc-form-item label="姓名" prop="username" tooltip="这里是姓名">
      <bc-input v-model="formValidate.username"></bc-input>
    </bc-form-item>
    <bc-form-item label="邮箱" prop="mail">
      <bc-input v-model="formValidate.mail"></bc-input>
    </bc-form-item>
    <bc-form-item label="地址" prop="address">
      <bc-select v-model="formValidate.address">
        <bc-option value="xixigu">西溪谷国际商务中心</bc-option>
        <bc-option value="jiangcun">蒋村商务中心</bc-option>
      </bc-select>
    </bc-form-item>
    <bc-form-item label="描述" prop="comment">
      <bc-input type="textarea" :rows="3" v-model="formValidate.comment"></bc-input>
    </bc-form-item>
    <bc-form-item>
      <bc-button type="primary" @click="submitValidate">提交</bc-button>
      <bc-button type="ghost" @click="resetValidate('formValidate')">重置</bc-button>
    </bc-form-item>
  </bc-form>
</template>
<script>
  export default {
    data() {
      return {
        formValidate: {
          username: '',
          mail: '',
          address: '',
          comment: '',
        },
        formValidateRules: {
          username: { required: true, message: '用户名不能为空' },
          mail: [
            { required: true, message: '邮箱不能为空' },
            { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
          ],
          address: { required: true, message: '地址不能为空' },
          comment: { required: true, message: '描述不能为空' },
        },
      };
    },
  };
</script>
```
:::


## API

### Form Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| model 表单数据对象 | Object | - |
| rules | 表单验证规则，具体配置查看 [async-validator](https://github.com/yiminghe/async-validator) | Object | - |
| inline | 是否开启行内表单模式 | Boolean | false |
| label-width | 表单域标签的宽度，所有的 FormItem 都会继承 Form 组件的 label-width 的值 | Number | - |


### Form Methods

| 方法名 | 说明 | 参数 | 返回值 |
| ----|----|---|----|
| validate | 对整个表单进行校验，参数为检验完的回调，会返回一个 Boolean 表示成功与失败，支持 Promise | (callback) | Boolean |
| validateField | 对部分表单字段进行校验的方法，参数1为需校验的 prop，参数2为检验完回调，返回错误信息 | ( prop, callback) | - |
| resetFields | 对整个表单进行重置，将所有字段值重置为空并移除校验结果 | - | - |

### FormItem props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| prop | 对应表单域 model 里的字段 | String | - |
| label | 标签文本 | String | - |
| label-width | 表单域标签的的宽度 | Number | - |
| label-for | 指定原生的 label 标签的 for 属性，配合控件的 element-id 属性，可以点击 label 时聚焦控件。 | String  - |
| required |  是否必填，如不设置，则会根据校验规则自动生成 | Boolean | - |
| rules | 表单验证规则 | Object | Array | - |


