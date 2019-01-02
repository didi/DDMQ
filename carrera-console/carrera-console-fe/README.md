**English** | [中文](./README_CN.md)
## Usage for develop
*  cd carrera-console-fe 
*  npm i
*  cd build, and modify the config in webpack.base.config.js:
     ``` js
      devServer: {
        ...
        proxy: {
          '/carrera/api': {
            // Modify target
            target: 'http://xxxxxx',
            changeOrigin: true,
            secure: false,
          },
        },
      }
     ```
*  npm run start
*  Visit DDMQ Web Console  

    > <http://0.0.0.0:8080>


## Usage for deploy
* cd carrera-console-fe 
* ./build.sh, the generated static resources are placed in the dist folder