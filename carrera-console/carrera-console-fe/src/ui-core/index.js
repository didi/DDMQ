import { AutoComplete } from './components/autocomplete';
import { Alert } from './components/alert';
import { BaseDrop } from './components/base-drop';
import { Breadcrumb, BreadcrumbItem } from './components/breadcrumb';
import { Dropdown, DropdownMenu, DropdownItem } from './components/dropdown';
import { Button, ButtonGroup } from './components/button';
import { Card } from './components/card';
import { Checkbox, CheckboxGroup } from './components/checkbox';
import { Drawer } from './components/drawer';
import { Icon } from './components/icon';
import { Menu, MenuItem, Submenu, MenuGroup } from './components/menu';
import { Modal } from './components/modal';
import { Input } from './components/input';
import { Form, FormItem } from './components/form';
import { Row, Col } from './components/grid';
import { Select, Option, OptionGroup } from './components/select';
import { Steps, StepsItem } from './components/steps';
import { Switch } from './components/switch';
import { Tab, TabPane } from './components/tab';
// import { Table, TableColunm, ExpandRow } from './components/table';
import { Tag } from './components/tag';
import { Tree } from './components/tree';
import { Tooltip } from './components/tooltip';
import { Pagination } from './components/pagination';
import { Popover } from './components/popover';
import { Progress } from './components/progress';
import { Radio, RadioGroup } from './components/radio';
import Message from './components/message';
import Notice from './components/notice';

import clickoutside from './directives/clickoutside';
import autoFocus from './directives/auto-focus';
import loadingDirective from './components/loading/directive';

const components = {
  AutoComplete,
  Alert,
  BaseDrop,
  Breadcrumb,
  BreadcrumbItem,
  Dropdown,
  DropdownMenu,
  DropdownItem,
  Button,
  ButtonGroup,
  Card,
  Checkbox,
  CheckboxGroup,
  Drawer,
  Icon,
  Menu,
  MenuItem,
  Submenu,
  MenuGroup,
  Modal,
  Input,
  Form,
  FormItem,
  Row,
  Col,
  Select,
  Steps,
  StepsItem,
  Option,
  OptionGroup,
  Switch,
  Tab,
  TabPane,
  // Table,
  // TableColunm,
  // // ExpandRow,
  Tag,
  Tree,
  Tooltip,
  Pagination,
  Popover,
  Progress,
  Radio,
  RadioGroup
};

const install = function (Vue, opts = {}) {
  if (install.installed) {
    return;
  }

  Object.keys(components).forEach((key) => {
    let component = components[key];

    Vue.component(component.name, component);
  });

  Vue.component('bc-message', Message);
  Vue.component('bc-notice', Notice);

  Vue.prototype.$notice = Notice;
  Vue.prototype.$message = Message;
  Vue.prototype.$modal = Modal;

  Vue.directive('clickoutside', clickoutside);
  Vue.directive('auto-focus', autoFocus);
  Vue.directive('loading', loadingDirective);
};

if (typeof widnow !== 'undefined' && window.Vue) {
  install(window.Vue);
}

export default Object.assign(components, {
  install
});
