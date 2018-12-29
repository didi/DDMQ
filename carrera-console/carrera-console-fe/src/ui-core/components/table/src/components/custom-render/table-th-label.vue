<style lang="less">
.bc-table-custom-label{
  padding: 10px 0px
}
</style>
<script>
  export default {
    name: '',
    props: ['dom'],
    components: {
    },
    data() {
      return {

      };
    },
    created() {
    },
    methods: {
    },
    mounted() {
    },
    render() {
      var node = this.$createElement;
      return node(
        'div',
        { attrs: { class: 'bc-table-custom-label' } },
        this.dom
      );
    },
  };
</script>
