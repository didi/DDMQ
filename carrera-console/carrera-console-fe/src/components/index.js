import Header from './header/index.vue';
import Sidebar from './sidebar/index.vue';
import FilterInputSearcher from './filter-input-searcher/index.vue';
// import Row from './grid/row.vue';
// import Col from './grid/col.vue';

const components = {
  Sidebar,
  Header,
  FilterInputSearcher
  // Row,
  // Col,
};

const install = function (Vue) {
  if (install.installed) {
    return;
  }

  Object.keys(components).forEach((key) => {
    let component = components[key];

    Vue.component(key, component);
  });
};

if (typeof widnow !== 'undefined' && window.Vue) {
  install(window.Vue);
}

export default Object.assign(components, {
  install
});
