import Vue from 'vue';
const SPECIAL_CHARS_REGEXP = /([\:\-\_]+(.))/g;
const MOZ_HACK_REGEXP = /^moz([A-Z])/;
const isServer = Vue.prototype.$isServer;

function camelCase(name) {
  return name.replace(SPECIAL_CHARS_REGEXP, function(_, separator, letter, offset) {
    return offset ? letter.toUpperCase() : letter;
  }).replace(MOZ_HACK_REGEXP, 'Moz$1');
}

export function oneOf(target, list) {
  return list.indexOf(target) !== -1;
};

export function camelcaseToHyphen(str) {
  return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
}

/**
 * 获取CSS样式
 */
export function getStyle(element, styleName) {
  if (!element || !styleName) {
    return null;
  }

  styleName = camelCase(styleName);

  if (styleName === 'float') {
    styleName = 'cssFloat';
  }

  if (document.defaultView && document.defaultView.getComputedStyle) {
    const computed = document.defaultView.getComputedStyle(element, '');
    return element.style[styleName] || computed ? computed[styleName] : null;
  } else {
    return element.style[styleName];
  }
}

// FIXME: 参数
/**
 * 向上查找组件
 * @param {Object} context 起点组件
 * @param {String} componentName 组件名
 * @param {Array} componentNames 组件名数组
 * @returns {Object} 组件
 */
function findComponentUpward(context, componentName, componentNames) {
  if (typeof componentName === 'string') {
    componentNames = [componentName];
  } else {
    componentNames = componentName;
  }

  let parent = context.$parent;
  let name = parent.$options.name;
  while (parent && (!name || componentNames.indexOf(name) < 0)) {
    parent = parent.$parent;
    if (parent) name = parent.$options.name;
  }
  return parent;
}

/**
 * 向下查找组件 只找一个
 * @param {Object} context 起点组件
 * @param {String} componentName 组件名
 * @returns {Object} 组件
 */
function findComponentDownward(context, componentName) {
  const childrens = context.$children;
  let children = null;

  if (childrens.length) {
    childrens.forEach(child => {
      const name = child.$options.name;
      if (name === componentName) {
        children = child;
      }
    });

    for (let i = 0; i < childrens.length; i++) {
      const child = childrens[i];
      const name = child.$options.name;
      if (name === componentName) {
        children = child;
        break;
      } else {
        children = findComponentDownward(child, componentName);
        if (children) break;
      }
    }
  }
  return children;
}

/**
 * 向下查找组件 找多个
 * @param {Object} context 起点组件
 * @param {String} componentName 组件名
 * @returns {Array} 组件数组
 */
function findComponentsDownward(context, componentName, components = []) {
  const childrens = context.$children;
  if (childrens.length) {
    childrens.forEach(child => {
      const name = child.$options.name;
      const childs = child.$children;

      if (name === componentName) {
        components.push(child);
      }
      if (childs.length) {
        const findChilds = findComponentsDownward(child, componentName, components);
        if (findChilds) {
          components.concat(findChilds);
        }
      }
    });
  }
  return components;
}

export {
  findComponentUpward,
  findComponentDownward,
  findComponentsDownward,
};

export const getValueByPath = function(object, prop) {
  prop = prop || '';
  const paths = prop.split('.');
  let current = object;
  let result = null;
  for (let i = 0, j = paths.length; i < j; i++) {
    const path = paths[i];
    if (!current) break;

    if (i === j - 1) {
      result = current[path];
      break;
    }
    current = current[path];
  }
  return result;
};

// For Modal scrollBar hidden
let cached;
export function getScrollBarSize (fresh) {
  if (isServer) {
    return 0;
  }
  if (fresh || cached === undefined) {
    const inner = document.createElement('div');
    inner.style.width = '100%';
    inner.style.height = '200px';

    const outer = document.createElement('div');
    const outerStyle = outer.style;

    outerStyle.position = 'absolute';
    outerStyle.top = 0;
    outerStyle.left = 0;
    outerStyle.pointerEvents = 'none';
    outerStyle.visibility = 'hidden';
    outerStyle.width = '200px';
    outerStyle.height = '150px';
    outerStyle.overflow = 'hidden';

    outer.appendChild(inner);

    document.body.appendChild(outer);

    const widthContained = inner.offsetWidth;
    outer.style.overflow = 'scroll';
    let widthScroll = inner.offsetWidth;

    if (widthContained === widthScroll) {
      widthScroll = outer.clientWidth;
    }

    document.body.removeChild(outer);

    cached = widthContained - widthScroll;
  }
  return cached;
}
