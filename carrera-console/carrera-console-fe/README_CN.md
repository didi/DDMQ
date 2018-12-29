[English](./README.md) | **中文**
## 开发使用
* 1. 进入 carrera-console-fe 文件夹
* 2. npm i 安装依赖包
* 3. 修改build文件夹下webpack.base.config.js文件，
       ``` js
      devServer: {
        ...
        proxy: {
          '/carrera/api': {
            // 修改 target
            target: 'http://xxxxxx',
            changeOrigin: true,
            secure: false,
          },
        },
      }
     ```
* 4. npm run start 启动服务
* 5. 浏览器输入 http://0.0.0.0:8080/ 即可访问


## 部署使用
* 进入 carrera-console-fe 文件夹
*  执行./build.sh 等待打包编译，编译生成的静态资源放置在dist文件夹。