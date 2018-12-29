module.exports = {
  root: true,
  parserOptions: {
    parser: 'babel-eslint',
    ecmaVersion: 2017,
    sourceType: 'module'
  },
  env: {
    browser: true,
    node: true,
    mocha: true,
    amd: true,
    es6: true,
  },
  "globals": {
    "expect": true,
    "__DEV__": true,
    "__DEV_LAYOUT__": true,
  },
  extends: [
    'plugin:vue/essential',
    'standard'
  ],
  plugins: [
    'vue'
  ],
  rules: {
    "semi": "off",
    'generator-star-spacing': 'off',
    'no-debugger': 'error',
    "no-console": 'error',
    "vue/script-indent": ["error", 2, { "baseIndent": 1 }],
    "no-dupe-args": 'error',
    "no-empty": "error",
    "no-extra-semi": "error",
    "no-new-require": "error",
    "no-buffer-constructor": "error",
    "global-require": "error",
    "no-await-in-loop": "error",
    "valid-jsdoc": "error",
  },
  overrides: [
    {
      "files": ["*.vue"],
      "rules": {
        "indent": "off"
      }
    }
  ]
};
