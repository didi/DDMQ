import Notification from '../notification';

const classPrefix = 'bcui-message';
const iconClassPrefix = 'bcui-icon';
const prefixKey = 'bcui_message_key_';

let defaultDuration = 1500;
let top;
let messageInstance;
let name = 1;

const iconTypes = {
  info: 'info',
  success: 'confirm',
  warning: 'alert',
  error: 'error',
  loading: 'loading',
};

function getMessageInstance() {
  messageInstance =
    messageInstance ||
    Notification.newInstance({
      classPrefix,
      styles: {
        top: `${top}px`,
      },
    });

  return messageInstance;
}

function notice (type, options = {}) {
  const content = typeof options === 'string' ? options : options.content;
  const duration = options.duration === 0 ? 0 : defaultDuration;
  const onClose = options.onClose || function() {};
  const render = options.render || function() {};
  const closable = options.closable || false;
  const iconType = iconTypes[type];

  // if loading
  const loadCls = type === 'loading' ? ' bcui-load-loop' : '';

  let instance = getMessageInstance();

  instance.notice({
    name: `${prefixKey}${name}`,
    duration,
    styles: {},
    transitionName: 'move-up',
    content: `
            <div class="${classPrefix} ${classPrefix}__${type}">
                <i class="${iconClassPrefix} ${iconClassPrefix}-${iconType}${loadCls}"></i>
                <span>${content}</span>
            </div>
        `,
    render,
    closable,
    onClose,
  });

  // 用于手动消除
  return (function() {
    let target = name++;

    return function() {
      instance.remove(`${prefixKey}${target}`);
    };
  })();
}

export default {
  info(options) {
    return notice('info', options);
  },
  success(options) {
    return notice('success', options);
  },
  warning(options) {
    return notice('warning', options);
  },
  warn(options) {
    return notice('warning', options);
  },
  error(options) {
    return notice('error', options);
  },
  loading(options) {
    return notice('loading', options);
  },
  config(options) {
    if (options.top) {
      top = options.top;
    }
    if (options.duration) {
      defaultDuration = options.duration;
    }
  },
  destroy() {
    let instance = getMessageInstance();

    messageInstance = null;
    instance.destroy();
  },
};
