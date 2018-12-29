'use strict';

const fs = require('fs');
const utils = require('./utils');
const webpack = require('webpack');
const merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const baseConfig = require('./webpack.base.config');

const HOST = 'localhost';
const PORT = 8080;

module.exports = merge(baseConfig, {
  mode: 'development',
  entry: utils.resolve('src/main.js'),
  devServer: {
    clientLogLevel: 'warning',
    contentBase: utils.resolve('static/'),
    hot: true,
    compress: true,
    host: HOST,
    port: PORT,
    open: true,
    overlay: { warnings: false, errors: true },
    publicPath: '/',
    quiet: true,
    watchOptions: {
      poll: true
    },
    proxy: {
      '/carrera/api': {
        target: 'http://xxxxx ',
        changeOrigin: true,
        secure: false
      }
    }
  },

  resolve: {
    extensions: ['.js', '.vue', '.json']
  },

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader'
        ]
      }, {
        test: /\.less$/,
        use: [
          'vue-style-loader',
          'css-loader',
          'less-loader'
        ]
      }
    ]
  },

  plugins: [
    new HtmlWebpackPlugin({
      template: utils.resolve('index.html'),
      inject: true
    }),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.DefinePlugin({
      '__DEV__': true,
      'process.env.NODE_ENV': '"development"'
    })
  ]
});
