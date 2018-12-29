<style lang="less">
  .autocomplete__list {
    position: relative
  }

  .bc-autocomplete-dropdown-menu {
    width: 100%;
    max-height: 200px;
    overflow: auto;
    margin: 5px 0;
    padding: 5px 0px;
    background-color: #fff;
    box-sizing: border-box;
    border-radius: 4px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, .2);
    position: absolute;
    z-index: 900;
  }

  .bc-autocomplete-dropdown-menu li {
    margin: 0;
    padding: 7px 16px;
    clear: both;
    color: #657180;
    font-size: 12px!important;
    white-space: nowrap;
    list-style: none;
    cursor: pointer;
    transition: background 0.2s ease-in-out;
  }

  .bc-autocomplete-dropdown-menu li:hover {
    background: #f3f3f3;
  }

  .autocomplete li.focus-list {
    background: #28b0b0;
  }
</style>
<template>
  <div class="autocomplete" v-bind:class="{'open':openSuggestion}" v-clickoutside="handleClose">
    <bc-input
      class="bc-autocomplete-input"
      type="text"
      v-model="selection"
      @keydown='inputKeydown'
      @on-focus="focus"
      @input='change' />
    <div class="autocomplete transition autocomplete__list" v-show="open">
      <bc-base-drop class="bc-autocomplete-dropdown-menu">
        <ul>
          <li
            v-for="(suggestion,index) in matches"
            :key="suggestion"
            :class="{'active': isActive(index)}"
            @click="suggestionClick(index)">
            <span>{{ suggestion }}</span>
          </li>
        </ul>
      </bc-base-drop>
    </div>
  </div>
</template>

<script>
  import { findComponentUpward } from '../../utils/tools';
  import Emitter from '../../mixins/emitter';

  export default {
    name: 'bc-autocomplete',
    mixins: [Emitter],
    props: {
      value: {
        type: String | Number,
      },
      initValue: {
        type: String,
      },
      suggestions: {
        type: Array,
        required: true,
      },
      filterKeyName: {
        type: String,
      },
    },

    data() {
      return {
        open: false,
        current: 0,
        selection: '',
      };
    },

    computed: {
      // Filtering the suggestion based on the input
      matches() {
        let matched = this.suggestions.filter((item) => {
          let str = '';

          if (typeof item === 'string') {
            str = item;
          } else {
            str = item[this.filterKeyName];
          }

          if (typeof item === 'string') {
          }

          return str.indexOf(this.selection) >= 0;
        });

        if (!matched.length) {
          this.open = false;

          this.$emit('value', this.selection);
          this.$emit('on-search', this.suggestions, this.selection);
        }
        return matched;
      },

      // The flag
      openSuggestion() {
        return this.selection !== '' &&
          this.matches === '' &&
          this.open === true;
      },
    },

    methods: {

      // When enter pressed on the input
      inputKeydown(e) {
        let key = e.keyCode;

        //  Disable when list isn't showing up
        if (!this.matches) {
          return;
        }
        switch (key) {
          case 40: // down
            this.current++;
            break;
          case 38: //  up
            this.current--;
            break;
          case 13: //  enter
            this.selection = this.matches(this.current);
            this.$emit('value', this.selection);
            this.open = false;
            break;
          case 27: //  esc
            this.open = false;
            break;
        }
      },

      // For highlighting element
      isActive(index) {
        return {
          'focus-list': index == this.current,
        };
      },
      // When the user changes input
      change() {
        if (this.open == false) {
          this.open = true;
          this.current = 0;
        }
      },

      focus () {
        this.$emit('focus', this.selection);
        this.open = true;
      },

      blur () {
        this.$emit('blur', this.selection);

        if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
          this.dispatch('bc-form-item', 'on-form-blur', this.selection);
        }

        setTimeout(() => this.open = false, 200);
      },

      handleClose() {
        this.open = false;
      },

      // When one of the suggestion is clicked
      suggestionClick(index) {
        let matched = this.matches[index];

        this.selection = matched;
        this.open = false;
        this.$emit('value', matched);
        this.$emit('on-select', matched);

        if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
          this.dispatch('bc-form-item', 'on-form-blur', this.currentValue);
        }
      },
    },

    mounted() {
      this.selection = this.initValue || '';
    },
  };
</script>
