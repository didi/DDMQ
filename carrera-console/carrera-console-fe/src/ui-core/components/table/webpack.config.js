const path = require('path');
const fs = require('fs');
const argv = require('yargs').argv;
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

function resolve(filename) {
  return path.resolve(__dirname, filename);
}

const ENV = argv.env;

let baseConfig = {};

let entry = {};
let output = {};

if (ENV == 'development') {
  let files = fs.readdirSync(resolve('examples'));
  files.forEach((file) => {
    let extname = path.extname(file);
    let name = path.basename(file, extname);
    if (extname === '.js' && name.indexOf('.b') < 0) {
      let htmlFile = path.join(resolve('examples'), `${name}.html`);
      if (fs.existsSync(htmlFile)) {
        entry[name] = [`./examples/${file}`];
      }
    }
  });
  output = {
    path: resolve('./examples'),
    filename: '[name].js',
    publicPath: '/examples',
  };
} else {
  entry = resolve('./src/index.js');
  output = {
    path: resolve('./lib'),
    filename: 'index.js',
    library: 'bc-v-siderbar', // 模块名称
    libraryTarget: 'umd', // 输出格式
    umdNamedDefine: true, // 是否将模块名称作为 AMD 输出的命名空间
  };
}

baseConfig = {
  entry: entry,
  output,
  performance: {
    hints: 'warning', // enum
  },
  resolve: {
    extensions: ['.js', '.json', '.vue', '.css', '.less'],
    modules: [
      resolve('node_modules'),
      resolve('examples'),
    ],
  },
  externals: {
    vue: {
      root: 'Vue',
      commonjs: 'vue',
      commonjs2: 'vue',
      amd: 'vue'
    }
  },
  module: {
    rules: [
      // {
      //   enforce: 'pre',
      //   test: /.vue$|.js$/,
      //   loader: 'eslint-loader',
      //   exclude: /node_modules/,
      // },
      // vue ----------------------------------
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
            js: 'babel-loader?presets[]=es2015&presets[]=stage-0&plugins[]=add-module-exports&plugins[]=transform-decorators-legacy',
            css: ExtractTextPlugin.extract(['css-loader']),
            less: ExtractTextPlugin.extract(['css-loader', 'less-loader']),
          },
          postcss: [
            require('autoprefixer')(),
          ],
        },
      },
      // js ----------------------------------
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
        query: {
          presets: [
            require.resolve('babel-preset-es2015'),
            require.resolve('babel-preset-stage-0')
          ],
          plugins: [
            require.resolve('babel-plugin-add-module-exports'),
            require.resolve('babel-plugin-transform-decorators-legacy'),
          ],
        },
      },

      // 样式 ----------------------------------
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          use: 'css-loader',
        })
      }, {
        test: /\.less$/,
        use: ExtractTextPlugin.extract(['css-loader', 'less-loader']),
      },
      // 资源文件 ----------------------------------
      {
        test: /\.(png|jpe?g|gif)$/,
        loader: 'url-loader',
        query: {
          limit: 10000,
          name: 'images/[name].[hash:7].[ext]',
        },
      }, {
        test: /\.(woff|woff2|eot|ttf|svg)$/,
        loader: 'url-loader',
        options: {
          limit: 1000,
          name: 'fonts/[name].[hash:7].[ext]',
        },
      },
    ],
  },
  plugins: [
    new ExtractTextPlugin('index.css'),
    new webpack.optimize.UglifyJsPlugin({
      sourcemap: true,
      compress: {
        screw_ie8: true,
        warnings: false,
      },
      comments: false,
      beautify: false,
    }),
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"development"',
      }
    }),
  ],
};

module.exports = baseConfig;
