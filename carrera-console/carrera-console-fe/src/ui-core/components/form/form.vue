<template>
  <div :class="formClasses" :style="formStyles">
    <slot></slot>
  </div>
</template>

<script>
  import {
    oneOf,
  } from '../../utils/tools';
  const classPrefix = 'bcui-form';
  export default {
    name: 'bc-form',
    props: {
      model: {
        type: Object,
      },
      rules: {
        type: Object,
      },
      width: {
        type: [Number, String],
      },
      offset: {
        type: [Number, String],
        default: 0,
      },
      labelWidth: {
        type: Number,
      },
      labelAlign: {
        type: String,
        validator(value) {
          return oneOf(value, ['top', 'left', 'right']);
        },
        default: 'right',
      },
      inline: {
        type: Boolean,
        default: false,
      },
      showMessage: {
        type: Boolean,
        default: true,
      },
    },
    components: {},
    data() {
      return {
        classPrefix,
        fields: [],
      };
    },
    computed: {
      formClasses() {
        return [
          `${classPrefix}`,
          `${classPrefix}-label--${this.labelAlign}`, {
            [`${classPrefix}--inline`]: this.inline,
          },
        ];
      },
      formStyles() {
        let style = {};
        let userStyle = {};

        if (this.width) {
          let width = this.width.toString().indexOf('%') > 0 ? this.width : this.width + 'px';
          userStyle.width = width;
        }

        if (this.offset > 0) {
          let offset = this.offset.toString().indexOf('%') > 0 ? this.offset : this.offset + 'px';
          userStyle['margin-left'] = offset;
        }

        const customStyle = this.styles ? this.styles : {};

        Object.assign(style, userStyle, customStyle);

        return style;
      },
    },
    watch: {
      rules() {
        this.validate();
      },
    },
    created() {
      this.$on('on-form-item-add', (field) => {
        if (field) this.fields.push(field);
        return false;
      });
      this.$on('on-form-item-remove', (field) => {
        if (field.prop) this.fields.splice(this.fields.indexOf(field), 1);
        return false;
      });
    },
    methods: {
      resetFields() {
        this.fields.forEach(field => {
          field.resetField();
        });
      },
      validate(callback) {
        let valid = true;
        let count = 0;

        this.fields.forEach(field => {
          field.validate('', errors => {
            if (errors) {
              valid = false;
            }
            if (typeof callback === 'function' && ++count === this.fields.length) {
              callback(valid, errors);
            }
          });
        });
      },
      validateField(prop, cb) {
        const field = this.fields.filter(field => field.prop === prop)[0];
        if (!field) {
          throw new Error('[Warn]: must call validateField with valid prop string!');
        }
        field.validate('', cb);
      },
    },
    mounted() {},
  };
</script>
