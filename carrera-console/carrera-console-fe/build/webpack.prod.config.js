'use strict';

const utils = require('./utils');
const webpack = require('webpack');
const merge = require('webpack-merge');
const baseConfig = require('./webpack.base.config');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = merge(baseConfig, {
  mode: 'production',
  entry: {
    index: utils.resolve('src/main.js')
  },
  output: {
    path: utils.resolve('dist/'),
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        test: /\.css?$/,
        use: [
          MiniCssExtractPlugin.loader,
          { loader: 'css-loader', options: { importLoaders: 1 } }
        ]
      }, {
        test: /\.less$/,
        use: [
          MiniCssExtractPlugin.loader,
          { loader: 'css-loader', options: { importLoaders: 1 } },
          {
            loader: 'less-loader',
            options: {
              compress: true
            }
          }
        ]
      }
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      filename: utils.resolve('dist/index.html'),
      template: utils.resolve('index.html'),
      inject: true
    }),
    new MiniCssExtractPlugin({
      filename: 'index.css'
    }),
    new webpack.DefinePlugin({
      '__DEV__': false,
      'process.env.NODE_ENV': '"production"'
    }),
    new OptimizeCSSAssetsPlugin({}),
    new CopyWebpackPlugin([{

      from: utils.resolve('static'),
      to: utils.resolve('dist') // 目标地址，相对于output的path目录
    }]),
    new CleanWebpackPlugin(utils.resolve('dist'))
  ],
  optimization: {
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        vendors: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all'
        },
        commons: {
          chunks: 'async',
          name: 'commons-async',
          minSize: 0,
          minChunks: 2
        }
      }
    }
  }
});
