<template>
  <div :class="inputfieldClasses" :style="inputStyles">
    <template v-if="type !== 'textarea'">
      <div
        :class="[classPrefix + '__prepend']"
        v-if="prepend"
        v-show="slotReady"
        ref="prepend">
        <slot name="prepend"></slot>
      </div>
      <div v-if="slotSelect"
        :class="[classPrefix + '__select']">
        <slot name="select"></slot>
      </div>
      <icon
        class="bcui-inputfield__icon"
        :type="icon"
        v-if="icon && !loading && iconPosition === 'start'" @click.native="handleIconClick"></icon>
      <icon class="bcui-inputfield__icon" type="loading" v-if="loading"></icon>
      <icon
        class="bcui-inputfield__icon"
        :type="icon"
        v-if="icon && !loading && iconPosition === 'end'" @click.native="handleIconClick"></icon>
      <input
        :type="type"
        :class="inputClasses"
        :placeholder="placeholder"
        :disabled="disabled"
        :maxlength="maxlength"
        :readonly="readonly"
        :name="name"
        :value="currentValue"
        :number="number"
        v-auto-focus="autoFocus"
        @keyup.enter="handleEnter"
        @focus="handleFocus"
        @blur="handleBlur"
        @input="handleInput"
        @change="handleChange">
      <div
        :class="[classPrefix + '__append']"
        v-if="append"
        v-show="slotReady"
        ref="append">
        <slot name="append"></slot>
      </div>
    </template>
    <textarea v-else
      ref="textarea"
      :class="textareaClasses"
      :style="textareaStyles"
      :placeholder="placeholder"
      :disabled="disabled"
      :rows="rows"
      :maxlength="maxlength"
      :readonly="readonly"
      :name="name"
      :value="value"
      v-auto-focus="autoFocus"
      @keyup.enter="handleEnter"
      @focus="handleFocus"
      @blur="handleBlur"
      @input="handleInput">
    </textarea>
  </div>
</template>

<script>
  import { Icon } from '../icon';
  import { findComponentUpward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';
  const classPrefix = 'bcui-inputfield';

  export default {
    name: 'bc-input',
    mixins: [Emitter],
    components: {
      Icon,
    },
    props: {
      type: {
        type: String,
        default: 'text',
      },
      value: {
        type: [String, Number],
        default: '',
      },
      size: {
        validator(value) {
          return ['small', 'large'].indexOf(value) != -1;
        },
      },
      placeholder: {
        type: String,
        default: '',
      },
      maxlength: {
        type: Number,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      icon: String,
      iconPosition: {
        type: String,
        default: 'end',
      },
      loading: Boolean,
      autosize: {
        type: [Boolean, Object],
        default: false,
      },
      autoFocus: {
        type: Boolean,
        default: false,
      },
      rows: {
        type: Number,
        default: 8,
      },
      readonly: {
        type: Boolean,
        default: false,
      },
      name: {
        type: String,
      },
      number: {
        type: Boolean,
        default: false,
      },
      width: {
        type: [ Number, String ],
        default: '',
      },
    },
    data() {
      return {
        currentValue: this.value,
        classPrefix,
        prepend: true,
        append: true,
        slotSelect: false,
        slotReady: false,
        textareaStyles: {},
      };
    },
    computed: {
      inputfieldClasses() {
        return [`${classPrefix}`, {
          [`${classPrefix}--${this.size}`]: !!this.size,
          [`${classPrefix}--extra`]: this.prepend || this.append || this.slotSelect,
        }];
      },
      inputClasses() {
        let inputClassPrefix = classPrefix + '__input';

        return [`${inputClassPrefix}`, {
          [`${inputClassPrefix}--${this.size}`]: !!this.size,
          [`${inputClassPrefix}--iconed`]: !!this.icon,
          [`${inputClassPrefix}--extra`]: this.prepend || this.append || this.slotSelect,
          [`${inputClassPrefix}--disabled`]: this.disabled,
        }];
      },
      textareaClasses() {
        let textareaClassPrefix = classPrefix + '__textarea';
        return [
          `${textareaClassPrefix}`,
          {
            [`${textareaClassPrefix}-disabled`]: this.disabled,
          },
        ];
      },
      inputStyles() {
        let style = {};
        let width = this.width.toString().indexOf('%') > 0 ? this.width : this.width + 'px';
        const userStyle = {
          width,
        };
        const customStyle = this.styles ? this.styles : {};

        Object.assign(style, userStyle, customStyle);

        return style;
      },
    },
    methods: {
      handleEnter(event) {
        this.$emit('on-enter', event);
      },
      handleIconClick(event) {
        this.$emit('on-icon-click', event);
      },
      handleFocus(event) {
        this.$emit('on-focus', event);
      },
      handleBlur(event) {
        this.$emit('on-blur', event);
        if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
          this.dispatch('bc-form-item', 'on-form-blur', this.currentValue);
        }
      },
      handleInput(event) {
        let value = event.target.value;
        if (this.number) value = Number.isNaN(Number(value)) ? value : Number(value);
        this.$emit('input', value);
        this.setCurrentValue(value);
        this.$emit('on-change', event);
      },
      handleChange(event) {
        this.$emit('on-input-change', event);
      },
      setCurrentValue(value) {
        if (value === this.currentValue) return;
        this.$nextTick(() => {
          // this.resizeTextarea();
        });
        this.currentValue = value;
        if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
          this.dispatch('bc-form-item', 'on-form-change', value);
        }
      },
      // resizeTextarea() {
      //   const autosize = this.autosize;
      //   if (!autosize || this.type !== 'textarea') {
      //     return false;
      //   }
      //   const minRows = autosize.minRows;
      //   const maxRows = autosize.maxRows;
      //   this.textareaStyles = calcTextareaHeight(this.$refs.textarea, minRows, maxRows);
      // }
    },
    watch: {
      value(val) {
        this.setCurrentValue(val);
      },
    },
    mounted() {
      if (this.type !== 'textarea') {
        this.prepend = this.$slots.prepend !== undefined;
        this.append = this.$slots.append !== undefined;
        this.slotSelect = this.$slots.select !== undefined;
      } else {
        this.prepend = false;
        this.append = false;
      }

      this.slotReady = true;
    },
  };
</script>
