<template>
  <div :class="cardClasses" :style="cardStyles">
    <div :class="`${classPrefix}__head`" v-if="title || this.$slots.title">
        <div :class="`${classPrefix}__head-title`">
          <template v-if="this.$slots.title"><slot name="title"></slot></template>
          <template v-else-if="title">{{title}}</template>
        </div>
    </div>
    <div :class="`${classPrefix}__extra`" v-if="this.$slots.extra"><slot name="extra"></slot></div>
    <div :class="`${classPrefix}__body`">
      <slot></slot>
    </div>
  </div>
</template>

<script>
  const classPrefix = 'bcui-card';

  export default {
    name: 'bc-card',
    props: {
      title: {
        type: String,
      },
      extra: {
        type: String,
      },
      bordered: {
        type: Boolean,
        default: true,
      },
      hoverable: {
        type: Boolean,
        default: false,
      },
      width: {
        type: Number | String,
      },
    },
    components: {

    },
    data() {
      return {
        classPrefix,
      };
    },
    computed: {
      cardClasses() {
        return [
          `${classPrefix}`, {
            [`${classPrefix}--bordered`]: !!this.bordered,
            [`${classPrefix}--hoverable`]: !!this.hoverable,
          },
        ];
      },
      cardStyles() {
        if (this.width) {
          let width = this.width;

          if (typeof width === 'number') {
            width += 'px';
          }
          return {
            width,
          };
        }
      },
    },
    methods: {},
    mounted(){},
  };
</script>
