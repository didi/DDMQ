import Table from './components/table.vue';
import TableColunm from './components/table-colunm.vue';
import ExpandRow from './components/expand-row.vue';
const install = function(Vue) {
  if (install.installed) {
    return;
  }
  Vue.component(Table.name, Table);
  Vue.component(TableColunm.name, TableColunm);
  Vue.component(ExpandRow.name, ExpandRow);
};
if (typeof window !== 'undefined' && window.Vue) {
  install(window.Vue);
}
export default Object.assign(Table, { install });
export  { Table };
