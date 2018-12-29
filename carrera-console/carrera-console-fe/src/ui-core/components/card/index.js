import Card from './card.vue';

Card.install = (Vue) => {
  Vue.component(Card.name, Card);
};

export { Card };
export default Card;
