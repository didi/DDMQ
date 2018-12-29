export default {

  /**
   * inserted: called when the bound element has been inserted into its parent node (this only guarantees parent node presence, not necessarily in-document).
   * 根据以上官方描述得知并不确定 el 是否插入到document，所以通过 setTimeout 延迟执行 focus 方法
   * @param {*} el
   * @param {*} binding
   */
  inserted(el, binding) {
    const autoFocus = binding.value;
    if (autoFocus) {
      setTimeout(() => el.focus());
    }
  },
};
