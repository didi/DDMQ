<template>
  <div :class="formItemClasses">
    <label :class="labelClasses" :style="labelStyles" v-if="label"><slot name="label">{{ label }}&nbsp;</slot><template v-if="tooltip">
      <Tooltip :class="[`${classPrefix}__tooltip`]" :content="tooltip" :placement="tooltipPos">
        <Icon type="-bangzhu"></Icon>
      </Tooltip>
    </template></label>
    <div :class="[classPrefix + '__content']" :style="contentStyles">
      <slot></slot>
      <transition name="fade">
        <div :class="[classPrefix + '__error-tip']" v-if="validateState === 'error' && showMessage && form.showMessage">{{ validateMessage }}</div>
      </transition>
    </div>
  </div>
</template>

<script>
  import AsyncValidator from 'async-validator';
  import Emitter from '../../mixins/emitter';
  import { oneOf } from '../../utils/tools';

  import Tooltip from '../tooltip/tooltip.vue';
  import Icon from '../icon/icon.vue';

  const classPrefix = 'bcui-form-item';

  function getPropByPath(obj, path) {
    let tempObj = obj;
    path = path.replace(/\[(\w+)\]/g, '.$1');
    path = path.replace(/^\./, '');
    let keyArr = path.split('.');
    let i = 0;
    for (let len = keyArr.length - 1; i < len; ++i) {
      let key = keyArr[i];
      if (key in tempObj) {
        tempObj = tempObj[key];
      } else {
        throw new Error('[Warn]: please transfer a valid prop path to form item!');
      }
    }
    return {
      o: tempObj,
      k: keyArr[i],
      v: tempObj[keyArr[i]],
    };
  }

  export default {
    name: 'bc-form-item',
    mixins: [Emitter],
    components: {
      Tooltip,
      Icon,
    },
    props: {
      label: {
        type: String,
        default: '',
      },
      labelWidth: {
        type: Number,
      },
      labelAlign: {
        type: String,
        validator(value) {
          return oneOf(value, ['top', 'left', 'right']);
        },
      },
      tooltip: {
        type: String,
      },
      tooltipPos: {
        validator(value) {
          return oneOf(value, ['top', 'top-start', 'top-end', 'bottom', 'bottom-start', 'bottom-end', 'left', 'left-start', 'left-end', 'right', 'right-start', 'right-end']);
        },
        default: 'top',
      },
      prop: {
        type: String,
      },
      required: {
        type: Boolean,
        default: false,
      },
      rules: {
        type: [Object, Array],
      },
      error: {
        type: String,
      },
      validateStatus: {
        type: Boolean,
      },
      showMessage: {
        type: Boolean,
        default: true,
      },
    },
    data() {
      return {
        classPrefix,
        isRequired: false,
        validateState: '',
        validateMessage: '',
        validateDisabled: false,
        validator: {},
      };
    },

    watch: {
      error(val) {
        this.validateMessage = val;
        this.validateState = 'error';
      },
      validateStatus(val) {
        this.validateState = val;
      },
    },
    computed: {
      formItemClasses() {
        return [
          `${classPrefix}`,
          {
            [`${classPrefix}--required`]: this.required || this.isRequired,
            [`${classPrefix}--error`]: this.validateState === 'error',
            [`${classPrefix}--validating`]: this.validateState === 'validating',
          },
        ];
      },
      labelClasses() {
        let base = classPrefix + '__label';
        const labelAlign = this.labelAlign || this.form.labelAlign;
        return [base, {
          [`${base}--right`]: !!labelAlign,
        }];
      },
      form() {
        let parent = this.$parent;
        while (parent.$options.name !== 'bc-form') {
          parent = parent.$parent;
        }
        return parent;
      },
      fieldValue: {
        cache: false,
        get() {
          const model = this.form.model;
          if (!model || !this.prop) {
            return;
          }
          let path = this.prop;
          if (path.indexOf(':') !== -1) {
            path = path.replace(/:/, '.');
          }
          return getPropByPath(model, path).v;
        },
      },
      labelStyles() {
        let style = {};
        const labelWidth = this.labelWidth || this.form.labelWidth;
        if (labelWidth) {
          style.width = `${labelWidth}px`;
        }
        return style;
      },
      contentStyles() {
        let style = {};
        const labelWidth = this.labelWidth || this.form.labelWidth;
        if (labelWidth) {
          style.marginLeft = `${labelWidth}px`;
        }
        return style;
      },
    },
    created() {},
    methods: {
      getRules() {
        let formRules = this.form.rules;
        const selfRules = this.rules;
        formRules = formRules ? formRules[this.prop] : [];
        return [].concat(selfRules || formRules || []);
      },
      getFilteredRule(trigger) {
        const rules = this.getRules();
        return rules.filter(rule => !rule.trigger || rule.trigger.indexOf(trigger) !== -1);
      },
      validate(trigger, callback = function() {}) {
        const rules = this.getFilteredRule(trigger);
        if (!rules || rules.length === 0) {
          callback();
          return true;
        }
        this.validateState = 'validating';
        let descriptor = {};
        descriptor[this.prop] = rules;
        const validator = new AsyncValidator(descriptor);
        let model = {};
        model[this.prop] = this.fieldValue;
        validator.validate(model, {
          firstFields: true,
        }, (errors) => {
          this.validateState = !errors ? 'success' : 'error';
          this.validateMessage = errors ? errors[0].message : '';
          callback(this.validateMessage);
        });
      },
      resetField() {
        this.validateState = '';
        this.validateMessage = '';
        let model = this.form.model;
        let value = this.fieldValue;
        let path = this.prop;
        if (path.indexOf(':') !== -1) {
          path = path.replace(/:/, '.');
        }
        let prop = getPropByPath(model, path);
        if (Array.isArray(value) && value.length > 0) {
          this.validateDisabled = true;
          prop.o[prop.k] = [];
        } else if (value !== this.initialValue) {
          this.validateDisabled = true;
          prop.o[prop.k] = this.initialValue;
        }
      },
      onFieldBlur() {
        this.validate('blur');
      },
      onFieldChange() {
        if (this.validateDisabled) {
          this.validateDisabled = false;
          return;
        }
        this.validate('change');
      },
    },
    mounted() {
      if (this.prop) {
        this.dispatch('bc-form', 'on-form-item-add', this);
        Object.defineProperty(this, 'initialValue', {
          value: this.fieldValue,
        });
        let rules = this.getRules();
        if (rules.length) {
          rules.every(rule => {
            if (rule.required) {
              this.isRequired = true;
              return false;
            }
          });
          this.$on('on-form-blur', this.onFieldBlur);
          this.$on('on-form-change', this.onFieldChange);
        }
      }
    },
    beforeDestroy() {
      this.dispatch('bc-form', 'on-form-item-remove', this);
    },
  };
</script>
