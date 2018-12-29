import Vue from 'vue';
import Modal from './modal.vue';
import { Icon } from '../icon';
import { Button } from '../button';
import { camelcaseToHyphen } from '../../utils/tools';

function t(str) {
  return str;
}

const classPrefix = 'bcui-modal-confirm';

Modal.newInstance = (properties) => {
  const _props = properties || {};

  let props = '';
  Object.keys(_props).forEach((prop) => {
    props += ' :' + camelcaseToHyphen(prop) + '=' + prop;
  });

  const div = document.createElement('div');
  div.innerHTML = `
    <bc-modal ${props} v-model="visible" :width="width" :scrollable="scrollable" :class="'bcui-confirm'">
        <div class="${classPrefix}">
            <div class="${classPrefix}-head">
                <div class="${classPrefix}-head-title" v-html="title"></div>
            </div>
            <div class="${classPrefix}-body">
                <div :class="iconTypeCls"><i :class="iconNameCls"></i></div>
                <div v-html="body"></div>
            </div>
            <div class="${classPrefix}-footer">
                <bc-button v-if="showCancel" @click.native="cancel">{{ cancelText }}</bc-button>
                <bc-button :type="buttonType" :loading="buttonLoading" @click.native="ok">{{ okText }}</bc-button>
            </div>
        </div>
    </bc-modal>
    `;
  document.body.appendChild(div);

  let modal = new Vue({
    el: div,
    components: {
      'bc-modal': Modal,
      'bc-button': Button,
      'bc-icon': Icon,
    },
    data: Object.assign(_props, {
      visible: false,
      width: 416,
      title: '',
      body: '',
      iconType: '',
      iconName: '',
      okText: 'Submit',
      cancelText: 'Cancel',
      showCancel: false,
      loading: false,
      buttonLoading: false,
      scrollable: false,
      buttonType: _props.buttonType || 'primary',
    }),
    computed: {
      iconTypeCls() {
        return [`${classPrefix}-body-icon`, `${classPrefix}-body-icon-${this.iconType}`];
      },
      iconNameCls() {
        return ['bcui-icon', `bcui-icon-${this.iconName}`];
      },
    },
    methods: {
      cancel() {
        this.$children[0].visible = false;
        this.buttonLoading = false;
        this.onCancel();
        this.remove();
      },
      ok() {
        if (this.loading) {
          this.buttonLoading = true;
        } else {
          this.$children[0].visible = false;
          this.remove();
        }
        this.onOk();
      },
      remove() {
        setTimeout(() => {
          this.destroy();
        }, 300);
      },
      destroy() {
        this.$destroy();
        document.body.removeChild(this.$el);
        this.onRemove();
      },
      onOk() {},
      onCancel() {},
      onRemove() {},
    },
  });

  modal = modal.$children[0];

  return {
    show(props) {
      modal.$parent.showCancel = props.showCancel;
      modal.$parent.iconType = props.icon;

      switch (props.icon) {
        case 'info':
          modal.$parent.iconName = 'info';
          break;
        case 'success':
          modal.$parent.iconName = 'confirm';
          break;
        case 'warning':
          modal.$parent.iconName = 'alert';
          break;
        case 'error':
          modal.$parent.iconName = 'error';
          break;
        case 'confirm':
          modal.$parent.iconName = 'info';
          break;
      }

      if ('width' in props) {
        modal.$parent.width = props.width;
      }

      if ('title' in props) {
        modal.$parent.title = props.title;
      }

      if ('content' in props) {
        modal.$parent.body = props.content;
      }

      if ('okText' in props) {
        modal.$parent.okText = props.okText;
      }

      if ('cancelText' in props) {
        modal.$parent.cancelText = props.cancelText;
      }

      if ('onCancel' in props) {
        modal.$parent.onCancel = props.onCancel;
      }

      if ('onOk' in props) {
        modal.$parent.onOk = props.onOk;
      }

      // async for ok
      if ('loading' in props) {
        modal.$parent.loading = props.loading;
      }

      if ('scrollable' in props) {
        modal.$parent.scrollable = props.scrollable;
      }

      modal.$parent.buttonType = props.buttonType || 'primary';

      // notice when component destroy
      modal.$parent.onRemove = props.onRemove;

      modal.visible = true;
    },
    remove() {
      modal.visible = false;
      modal.$parent.buttonLoading = false;
      modal.$parent.remove();
    },
    component: modal,
  };
};

export default Modal;
