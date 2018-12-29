<style lang="less">
.bcui-checkbox__inner:after{
  left:4px;
}
.bcui-checkbox__input.is-disabled .bcui-checkbox__inner {
    background-color: #eef1f6;
    border-color: #d1dbe5;
    cursor: not-allowed;
}
</style>
<template>
  <span>
      <label class="bcui-checkbox "  :class="{'bcui-checkbox--checked': t_value}">
          <span class="bcui-checkbox__input"  :class="{'bcui-checkbox__input--checked': t_value, 'is-disabled': disabled}">
              <span class="bcui-checkbox__inner"></span> <!---->
              <input type="checkbox" class="bcui-checkbox__original-input"  :disabled="disabled" v-model="t_value">
          </span>
      </label>
  </span >
</template>

<script>
  export default {
    name: 'bc-checkbox',
    props: ['value', 'disabled'],
    data() {
      return {
        t_value: false,
      };
    },
    methods: {
      check_row(){
        if (!this.disabled) {
          this.$emit('row-click', !this.t_value);
        }
      },
    },
    computed: {

    },
    mounted() {
      this.t_value = this.value !== undefined && this.value;
      this.$watch('t_value', function(newvalue, oldvalue){
        this.$emit('input', newvalue);
      });
      this.$watch('value', function(newvalue, oldvalue){
        this.t_value = newvalue;
      });
    },
  };
</script>
