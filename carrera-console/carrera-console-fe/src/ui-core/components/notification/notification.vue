<template>
  <div :class="classes" :style="styles">
    <Notice v-for="notice in notices"
      :key="notice.name"
      :classPrefix="classPrefix"
      :styles="notice.styles"
      :content="notice.content"
      :render="notice.render"
      :duration="notice.duration"
      :closable="notice.closable"
      :name="notice.name"
      :transition-name="notice.transitionName"
      :on-close="notice.onClose">
    </Notice>
  </div>
</template>

<script>
  import Notice from './notice.vue';

  const classPrefix = 'bcui-notification';
  const now = Date.now();
  let seed = 0;

  function getUuid() {
    return 'bcuiNotification_' + now + '_' + (seed++);
  }

  export default {
    name: '',
    components: {
      Notice,
    },
    props: {
      classPrefix: {
        type: String,
        default: classPrefix,
      },
      styles: {
        type: Object,
        default: function() {
          return {
            top: '65px',
            left: '50%',
          };
        },
      },
      content: {
        type: String,
      },
      className: {
        type: String,
      },
    },
    data() {
      return {
        notices: [],
      };
    },
    computed: {
      classes() {
        return [
          `${this.classPrefix}-container`,
          {
            [`${this.className}`]: !!this.className,
          },
        ];
      },
    },
    methods: {
      add(notice) {
        const name = notice.name || getUuid();

        let _notice = Object.assign({
          styles: {
            right: '50%',
          },
          content: '',
          duration: 1500,
          closable: false,
          name: name,
        }, notice);

        this.notices.push(_notice);
      },
      close(name) {
        const notices = this.notices;

        for (let i = 0; i < notices.length; i++) {
          if (notices[i].name === name) {
            this.notices.splice(i, 1);
            break;
          }
        }
      },
    },
  };
</script>
