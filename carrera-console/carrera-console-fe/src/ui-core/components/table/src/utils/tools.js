const SPECIAL_CHARS_REGEXP = /([\:\-\_]+(.))/g;
const MOZ_HACK_REGEXP = /^moz([A-Z])/;

function camelCase(name) {
  return name.replace(SPECIAL_CHARS_REGEXP, function(_, separator, letter, offset) {
    return offset ? letter.toUpperCase() : letter;
  }).replace(MOZ_HACK_REGEXP, 'Moz$1');
}

export function oneOf(target, list) {
  return list.indexOf(target) !== -1;
};

/**
 * 获取CSS样式
 */
export function getStyle(element, styleName) {
  if (!element || !styleName) return null;
  styleName = camelCase(styleName);
  if (styleName === 'float') {
    styleName = 'cssFloat';
  }
  try {
    const computed = document.defaultView.getComputedStyle(element, '');
    return element.style[styleName] || computed ? computed[styleName] : null;
  } catch (e) {
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
