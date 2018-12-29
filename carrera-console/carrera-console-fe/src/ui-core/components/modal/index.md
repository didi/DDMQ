---
category: Components
subtitle: 模态框
type: FeedBack
noinstant: true
title: Modal
tag:
 - test
---

# Modal 模态框

需要用户处理事务，又不希望跳转页面以致打断工作流程时，可以使用 `Modal` 在当前页面正中打开一个浮层，承载相应的操作。

另外当需要一个简洁的确认框询问用户时，可以使用绑定在Vue组件实例上的`$mdal.confirm()` 等方法。


## 基础用法


::: demo 基础用法

最简单的使用方法，通过控制属性visible来显示 / 隐藏对话框。可以使用 v-model 实现双向绑定。默认按键盘ESC键也可以关闭

```html
<template>
  <div>
    <bc-button type="primary" @click="modal1 = true">显示对话框</bc-button>
    <bc-modal
      v-model="modal1"
      title="普通的Modal对话框标题"
      @on-ok="ok"
      @on-cancel="cancel">
      <p>对话框内容</p>
      <p>对话框内容</p>
      <p>对话框内容</p>
    </bc-modal>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        modal1: false,
      };
    },
    methods: {
      ok () {
        this.$message.info('点击了确定');
      },
      cancel () {
        this.$message.info('点击了取消');
      },
    },
  };
</script>
```
:::

## 自定义页头和页脚

::: demo 自定义页头和页脚

Modal 组件提供了自定义样式，通过slot可以自定义modal实例的页头和页脚

```html
<template>
  <div>
    <bc-button type="primary" @click="modal2 = true">自定义页头和页脚</bc-button>
    <bc-modal
      v-model="modal2"
      title="普通的Modal对话框标题"
      @on-ok="ok"
      @on-cancel="cancel">
      <div slot="header" style="color:#f60;text-align:center">
        <bc-icon type="information-circled"></bc-icon>
        <span>删除确认</span>
      </div>
      <div style="text-align:center">
        <p>此任务删除后，下游 10 个任务将无法执行。</p>
        <p>是否继续删除？</p>
      </div>
      <div slot="footer">
        <bc-button type="error" long :loading="modal_loading" @click="del">删除</bc-button>
      </div>
    </bc-modal>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        modal2: false,
        modal_loading: false,
        modal3: false,
        modal4: false,
        modal5: false,
        modal6: false,
        modal7: false,
      };
    },
    methods: {
      del () {
        this.modal_loading = true;

        setTimeout(() => {
          this.modal_loading = false;
          this.modal2 = false;
          this.$message.success('删除成功');
        }, 2000);
      },
      ok () {
        this.$message.info('点击了确定');
      },
      cancel () {
        this.$message.info('点击了取消');
      },
    },
  };
</script>
```
:::

## 设置宽度

::: demo 设置宽度

自定义宽度，单位 px，默认 520px。对话框的宽度是响应式的，当屏幕尺寸小于 768px 时，宽度会变为自动auto。

```html
<template>
  <div>
    <bc-button @click="modal5 = true">设置宽度</bc-button>
    <bc-modal
      v-model="modal5"
      title="自定义宽度"
      :loading="true"
      :width="300">
      <p>自定义宽度，单位 px，默认 520px。</p>
      <p>对话框的宽度是响应式的，当屏幕尺寸小于 768px 时，宽度会变为自动<code>auto</code>。
      </p>
    </bc-modal>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        modal5: false,
      };
    },
  };
</script>
```
:::

## 禁用关闭

:::demo 禁用关闭

可以禁用右上角的关闭按钮（同时也会禁用Esc）。也可以禁用遮罩层的关闭。

```html
<template>
  <div>
    <bc-button @click="modal6 = true">禁用右上角的关闭（包括Esc键）</bc-button>
    <bc-modal
      v-model="modal6"
      title="用右上角的关闭（包括Esc键）"
      :closable="false">
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
    </bc-modal>
    <bc-button @click="modal7 = true">禁用遮罩层关闭</bc-button>
    <bc-modal
      v-model="modal7"
      title="禁用遮罩层关闭）"
      :mask-closable="false">
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
      <p>llalalallalalal。</p>
    </bc-modal>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        modal6: false,
        modal7: false,
      };
    },
  };
</script>
```
:::

## 实例方法

在Modal组件上上封装了一些简单高效的实例方法，创建一次性的轻量级对话框。实例以隐式创建 Vue 组件的方式在全局创建一个对话框，并在消失时移除，所以同时只能操作一个对话框。

:::demo 基本用法

四种基本的对话框，只提供一个确定按钮。也可以通过 `buttonType` 设置对应的按钮样式

```html
<template>
  <div>
    <bc-button @click="instance('info')">消息</bc-button>
    <bc-button @click="instance('success')">成功</bc-button>
    <bc-button @click="instance('warning')">警告</bc-button>
    <bc-button @click="instance('error')">错误</bc-button>
  </div>
</template>
<script>
  export default {
    methods: {
      instance (type) {
        const title = '对话框的标题';
        const content = '<p>一些对话框内容</p><p>一些对话框内容</p>';
        switch (type) {
          case 'info':
            this.$modal.info({
              title: title,
              content: content,
              buttonType: 'info',
            });
            break;
          case 'success':
            this.$modal.success({
              title,
              content,
              buttonType: 'success',
            });
            break;
          case 'warning':
            this.$modal.warning({
              title: title,
              content: content,
              buttonType: 'warning',
            });
            break;
          case 'error':
            this.$modal.error({
              title: title,
              content: content,
              buttonType: 'error',
            });
            break;
          default:
            break;
        }
      },
    },
  };
</script>
```

:::

:::demo 自定义确认框

通过API可以自定义确认框的内容和按钮文本。

```html
<template>
  <div>
    <bc-button @click="confirm">标准</bc-button>
    <bc-button @click="custom">自定义按钮文字</bc-button>
    <bc-button @click="async">异步关闭</bc-button>
  </div>
</template>
<script>
  export default {
    methods: {
      confirm () {
        this.$modal.confirm({
          title: '确认对话框标题',
          content: '<p>一些对话框内容</p><p>一些对话框内容</p>',
          showCancel: true,
          onOk: () => {
            this.$message.info('点击了确定');
          },
          onCancel: () => {
            this.$message.info('点击了取消');
          },
        });
      },
      custom () {
        this.$modal.confirm({
          title: '确认对话框标题',
          content: '<p>一些对话框内容</p><p>一些对话框内容</p>',
          okText: 'OK',
          cancelText: 'Cancel',
        });
      },
      async () {
        this.$modal.confirm({
          title: '确认对话框标题',
          content: '<p>对话框将在 2秒 后关闭</p>',
          showCancel: true,
          loading: true,
          onOk: () => {
            setTimeout(() => {
              this.$modal.remove();
              this.$message.info('异步关闭了对话框');
            }, 2000);
          },
        });
      },
    },
  }
</script>
```
:::

## API

### Modal Props

| 参数 | 说明 | 类型 | 默认值 | 可选值 |
|---|----|----|-----|----|
| value | 对话框是否显示，可使用 v-model 双向绑定数据。 | Boolean | false |
| title | 对话框标题，如果使用 slot 自定义了页头，则 title 无效 | String | - |
| closable | 是否显示右上角的关闭按钮，关闭后 Esc 按键也将关闭 | Boolean | true |
| mask-closable | 是否允许点击遮罩层关闭 | Boolean | true |
| loading | 点击确定按钮时，确定按钮是否显示 loading 状态，开启则需手动设置visible来关闭对话框 | Boolean | false |
| scrollable | 页面是否可以滚动 | Boolean | false |
| ok-text | 确定按钮文字 | String | 确定 |
| cancel-text | 取消按钮文字 | String | 取消 |
| width | 对话框宽度，对话框的宽度是响应式的，当屏幕尺寸小于 768px 时，宽度会变为自动auto。也可以添加px作为单位，此时是String | Number | String | 520 |

### Modal Events

| 事件名 | 说明 | 参数 | 返回值 |
| ----|----|---|----|
| on-ok | 点击确定的回调 | - | - |
| on-cancel | 点击取消的回调 | - | - |



### Modal Slot

| 名称 | 说明 |
| ---|--- |
| header | 自定义页头 |
| footer | 自定义页脚内容 |
| close | 自定义右上角关闭内容 |
| 无 | 对话框主体内容 |

