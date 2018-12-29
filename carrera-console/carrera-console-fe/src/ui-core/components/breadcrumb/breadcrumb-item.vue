<template>
  <div class="bcui-breadcrumb__item">
    <a
      v-if="href"
      :href="href"
      class="bcui-breadcrumb__item-link">
      <slot>🍞默认值</slot>
    </a>
    <router-link
      v-else-if="route"
      :to="route"
      class="bcui-breadcrumb__item-link">
      <slot>🍞默认值</slot>
    </router-link>
    <span
      v-else
      class="bcui-breadcrumb__item-text">
      <slot>🍞默认值</slot>
    </span>
    <span class="bcui-breadcrumb__separator">/</span>
  </div>
</template>
<script>
  export default {
    name: 'bc-breadcrumb-item',
    props: {
      href: {
        type: String,
      },
      route: {
        type: Object,
      },
    },
    data() {
      return {};
    },
    created() {
    },
    components: {

    },
    methods: {

    },
  };
</script>
