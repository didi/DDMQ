<template>
  <div :class="progressClasses">
    <template v-if="type === 'circle'">
      <div :class="circleClassPrefix"
           :style="circleSizeStyle">
        <svg viewBox="0 0 100 100">
          <path class="bcui-progress-circle__track"
                :d="trackPath"
                :stroke="pathColor"
                :stroke-width="relativeStrokeWidth"
                fill="none"></path>
          <path class="bcui-progress-circle__path"
                :d="trackPath"
                stroke-linecap="butt"
                :stroke="strokeColor"
                :stroke-width="relativeStrokeWidth"
                fill="none"
                :style="circlePathStyle"></path>
        </svg>
        <div :class="[circleClassPrefix + '__inner']"
             v-if="showInfo || this.$slots.default">
          <template v-if="showInfo">{{percent}}%</template>
          <template v-if="this.$slots.default">
            <slot></slot>
          </template>
        </div>
      </div>
    </template>
    <template v-else>
      <div :class="barClassPrefix">
        <div :class="[barClassPrefix+ '__inner']"
             :style="pathStyles">
          <div :class="[barClassPrefix + '__bar']"
               :style="barStyles">
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
  const classPrefix = 'bcui-progress';

  export default {
    name: 'bc-progress',
    components: {},
    props: {
      percent: {
        type: Number | String,
        default: 0,
      },
      type: {
        type: String,
        default: 'bar',
      },

      // 是否显示百分比或者图标
      showInfo: {
        type: Boolean,
        default: false,
      },

      width: {
        type: Number,
        default: 126,
      },

      // 线条宽度
      strokeWidth: {
        type: Number,
        default: 4,
      },

      // 线条颜色
      strokeColor: {
        type: String,
        default: '#2d77ee',
      },

      // 背景线条颜色
      pathColor: {
        type: String,
        default: '#f0f0f0',
      },

      colorGradation: {
        type: Array,
      },
    },
    data() {
      return {
        classPrefix,
      };
    },
    watch: {},
    computed: {
      progressClasses() {
        return [classPrefix, {}];
      },

      barClassPrefix() {
        return `${classPrefix}-bar`;
      },

      circleClassPrefix() {
        return `${classPrefix}-circle`;
      },

      pathStyles() {
        let styles = {};

        if (this.pathColor) {
          styles.backgroundColor = this.pathColor;
        }

        if (this.strokeWidth) {
          styles.height = this.strokeWidth;
        }

        return styles;
      },
      barStyles() {
        let styles = {};
        let { percent, strokeColor, colorGradation } = this;

        styles.width = `${this.percent}%`;

        if (strokeColor) {
          styles.backgroundColor = this.strokeColor;
        }

        if (colorGradation) {
          colorGradation.forEach((bound) => {
            let { min, max } = bound;

            if (percent > min && percent <= max) {
              styles.backgroundColor = bound.color;
            }
          });
        }

        return styles;
      },

      relativeStrokeWidth() {
        return ((this.strokeWidth / this.width) * 100).toFixed(1);
      },
      trackPath() {
        const radius = parseInt(50 - parseFloat(this.relativeStrokeWidth) / 2, 10);

        return `M 50 50 m 0 -${radius} a ${radius} ${radius} 0 1 1 0 ${radius *
          2} a ${radius} ${radius} 0 1 1 0 -${radius * 2}`;
      },
      perimeter() {
        const radius = 50 - parseFloat(this.relativeStrokeWidth) / 2;

        return 2 * Math.PI * radius;
      },

      circleSizeStyle() {
        let styles = {
          width: this.width,
          height: this.width,
        };

        return styles;
      },
      circlePathStyle() {
        const perimeter = this.perimeter;

        return {
          strokeDasharray: `${perimeter}px,${perimeter}px`,
          strokeDashoffset: (1 - this.percent / 100) * perimeter + 'px',
          transition: 'stroke-dashoffset 0.6s ease 0s, stroke 0.6s ease',
        };
      },
    },
    methods: {},
    mounted() {},
  };
</script>
