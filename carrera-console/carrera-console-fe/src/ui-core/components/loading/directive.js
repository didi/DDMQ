const loadingDirective = {
  bind(el, binding, vnode) {
    let defaultOption = {
      bg: 'rgba(251, 252, 253, 0.8)',
      textColor: '#fff',
    };
    let text = el.getAttribute('loading-text') || '';
    let options = Object.assign({}, defaultOption, {
      text,
    });
    let position = window.getComputedStyle(el).position;

    if (position === 'static' || position === '') {
      el.style.position = 'relative';
    }

    // Message Box Create
    let msg = document.createElement('div');
    let spinner = document.createElement('div');
    spinner.className = 'bcui-loading-spinner';
    spinner.innerHTML = `
      <svg class="circular" viewBox="25 25 50 50">
        <circle class="path" cx="50" cy="50" r="20" fill="none"/>
      </svg>`;

    if (options.text) {
      let textContent = document.createElement('p');
      textContent.textContent = options.text;
      spinner.appendChild(textContent);
    }

    let box = document.createElement('div');

    box.style.backgroundColor = options.bg;
    box.style.display = 'none';

    box.className = 'bcui-loading-box';

    box.appendChild(spinner);
    el.appendChild(box);
  },
  inserted(el, binding, vnode) {
    let selector = el.getElementsByClassName('bcui-loading-box');
    let box = selector[selector.length - 1];
    if (binding.oldValue != binding.value) {
      if (binding.value) {
        binding.def.showLoadingBox(box);
      } else {
        binding.def.hideLoadingBox(box);
      }
    }
  },
  update(el, binding, vnode) {
    let selector = el.getElementsByClassName('bcui-loading-box');
    let box = selector[selector.length - 1];
    if (binding.oldValue != binding.value) {
      if (binding.value) {
        binding.def.showLoadingBox(box);
      } else {
        binding.def.hideLoadingBox(box);
      }
    }
  },
  showLoadingBox(box) {
    box.style.display = 'initial';
    window.requestAnimationFrame(() => {
      box.style.opacity = 1;
    });
  },
  hideLoadingBox(box) {
    box.style.display = 'none';
    window.requestAnimationFrame(() => {
      box.style.opacity = 0;
    });
  },
};

loadingDirective.version = '__VERSION__';

export default loadingDirective;
