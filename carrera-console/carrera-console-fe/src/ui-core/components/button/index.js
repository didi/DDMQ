import Button from './button.vue';
import ButtonGroup from './button-group.vue';

Button.install = (Vue) => {
  Vue.component(Button.name, Button);
};

export default Button;
export { Button, ButtonGroup };
