<template>
  <i :class="iconClasses"></i>
</template>
<script>
  const iconClassPrefix = 'bcui-icon';
  export default {
    name: 'bc-icon',
    props: {
      type: {
        type: String,
      },
    },
    computed: {
      iconClasses() {
        return [
          `${iconClassPrefix}`,
          `${iconClassPrefix}-${this.type}`,
        ];
      },
    },
  };
</script>
