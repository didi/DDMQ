'use strict';

const VueLoaderPlugin = require('vue-loader/lib/plugin');

const utils = require('./utils');

module.exports = {
  output: {
    path: utils.resolve('dist/'),
    libraryTarget: 'umd',
    umdNamedDefine: true,
    filename: '[name].js'
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      vue: 'vue/dist/vue.js'
    }
  },
  module: {
    rules: [
      // {
      //   test: /\.(js|vue)$/,
      //   use: 'eslint-loader',
      //   enforce: 'pre'
      // },
      {
        test: /\.vue$/,
        use: 'vue-loader'
      }, {
        test: /\.js$/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env'],
            plugins: ['@babel/plugin-transform-runtime']
          }
        },
        exclude: file => {
          return /node_modules/.test(file) && !/\.vue\.js/.test(file)
        }
      }, {
        test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
        use: {
          loader: 'url-loader',
          options: {
            limit: 10000,
            name: utils.assetsPath('img/[name].[hash:7].[ext]')
          }
        }
      }, {
        test: /\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/,
        use: {
          loader: 'url-loader',
          options: {
            limit: 10000,
            name: utils.assetsPath('media/[name].[hash:7].[ext]')
          }
        }
      }, {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        use: {
          loader: 'url-loader',
          options: {
            limit: 10000,
            name: utils.assetsPath('fonts/[name].[hash:7].[ext]')
          }
        }
      }
    ]
  },

  plugins: [
    new VueLoaderPlugin()
  ]
};
