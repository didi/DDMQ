---
category: Components
subtitle: 按钮
type: Form
noinstant: true
title: AutoComplete
---

# AutoComplete 自动完成

## 基础用法

::: demo 基本用法

基础的按钮用法.

```html
<template>
  <div>
    <bc-autocomplete
      v-model="match"
      :suggestions="suggestions"
      @on-search="handleSearch"
    ></bc-autocomplete>
    {{match}}
  </div>
</template>
<script>
  export default {
    data() {
      return {
        suggestions: ['zhanglun', 'zhanglun2'],
        match: '',
      };
    },
    methods: {
      handleSearch(suggestions, selection) {

      },

      test() {},
    },
  };
</script>
```
:::
