import Notification from '../notification/index.js';

const classPrefix = 'bcui-notice';
const iconclassPrefix = 'bcui-icon';
const prefixKey = 'bcui_notice_key_';

let top = 24;
let defaultDuration = 4500;
let noticeInstance;
let name = 1;

const iconTypes = {
  info: 'info',
  success: 'confirm',
  warning: 'alert',
  error: 'error',
};

function getNoticeInstance() {
  noticeInstance =
    noticeInstance ||
    Notification.newInstance({
      classPrefix,
      styles: {
        top: `${top}px`,
        right: 0,
      },
    });

  return noticeInstance;
}

function notice(type, options = {}) {
  const title = options.title || '';
  const desc = options.desc || '';
  const noticeKey = options.name || `${prefixKey}${name}`;
  const onClose = options.onClose || function() {};
  const duration = options.duration === 0 ? 0 : options.duration || defaultDuration;
  const render = options.render || function () {};

  name++;

  let instance = getNoticeInstance();

  let content;

  const with_desc = desc === '' ? '' : ` ${classPrefix}--with-desc`;

  if (type == 'normal') {
    content = `
            <div class="${classPrefix}__custom-content ${classPrefix}--with-normal ${with_desc}">
                <div class="${classPrefix}__title">${title}</div>
                <div class="${classPrefix}__desc">${desc}</div>
            </div>
        `;
  } else {
    const iconType = iconTypes[type];
    content = `
            <div class="${classPrefix}__custom-content ${classPrefix}--with-icon ${classPrefix}--with-${type} ${with_desc}">
                <span class="${classPrefix}__icon ${classPrefix}__icon-${type}">
                    <i class="${iconclassPrefix} ${iconclassPrefix}-${iconType}"></i>
                </span>
                <div class="${classPrefix}__title">${title}</div>
                <div class="${classPrefix}__desc">${desc}</div>
            </div>
        `;
  }

  instance.notice({
    name: noticeKey.toString(),
    duration,
    styles: {},
    transitionName: 'move-notice',
    content,
    onClose,
    render,
    closable: true,
  });
}

export default {
  open(options) {
    return notice('normal', options);
  },
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
  config(options) {
    if (options.top) {
      top = options.top;
    }
    if (options.duration || options.duration === 0) {
      defaultDuration = options.duration;
    }
  },
  close(name) {
    if (name) {
      name = name.toString();
      if (noticeInstance) {
        noticeInstance.remove(name);
      }
    } else {
      return false;
    }
  },
  destroy() {
    let instance = getNoticeInstance();

    noticeInstance = null;
    instance.destroy();
  },
};
