<style lang="less">
@backgroundColor: #F2F9FF;
.login {
  width: 100%;
  height: 100%;
  background: @backgroundColor;
  position: relative;
  .header h1{
    span ~ span {
      padding-left: 15px;
    }
  }
  .container {
    min-width: 800px;
    padding: 60px 150px 72px;
    background: #fff;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate3d(-50%, -50%, 0);
    box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.2);
    .bcui-form {
      margin-top: 40px;
      .bcui-form-item__label.bcui-form-item__label--right{
        line-height: 34px;
        padding-right: 5px;
      }
    }
    .check-mark {
      font-weight: 700;
      color: rgba(0, 0, 0, 0.4);
    }
    .bcui-inputfield {
      padding-right: 5px;
      width: ~'calc(100% - 20px)';
    }
    .login-submit {
      margin-top: 30px;
    }
  }
  .copyright {
    color: #7f8fa4;
    position: absolute;
    left: 50%;
    bottom: 45px;
    transform: translateX(-50%);
  }
}
</style>

<template>
  <div class="login">
    <div class="container">
      <div class="header">
        <h1><span>DDMQ</span><span>Login</span></h1>
      </div>
      <bc-form ref="loginForm" :rules="rules" :model="form">
        <bc-form-item
          :label-width="0"
          prop="username"
        >
          <bc-input v-model="form.username" placeholder="Username"></bc-input>
          <check-mark :valid="valid.username"></check-mark>
        </bc-form-item>
        <bc-form-item
          :label-width="0"
          prop="password"
        >
          <bc-input v-model="form.password" type="password" placeholder="Password" @keyup.enter.native="handleSubmit"></bc-input>
          <check-mark :valid="valid.password"></check-mark>
        </bc-form-item>
        <bc-form-item
          :label-width="0"
          class="login-submit"
        >
          <bc-button
            @click="handleSubmit"
            type="primary"
            long
            size="large"
            v-loading="loading"
            :disabled="disableSubmit"
          >Login</bc-button>
        </bc-form-item>
      </bc-form>
    </div>
    <div class="copyright">
      Powered by DiDi
    </div>
  </div>
</template>

<script>
  import loginMixins from '../../mixins/apis/login';
  import { login } from '../../utils';

  const { setToken } = login;
  const prefix = 'login';

  const checkMark = {
    name: 'checkMark',
    functional: true,
    props: {
      valid: Boolean
    },
    render (h, ctx) {
      return h('bc-icon', {
        class: ['check-mark'],
        props: {
          type: ctx.props.valid ? 'check' : 'times'
        }
      }, ['']);
    }
  };

  export default {
    name: prefix,
    mixins: [loginMixins],
    components: {
      checkMark
    },
    props: {},
    data () {
      return {
        prefix,
        form: {
          username: '',
          password: ''
        },
        valid: {
          username: true,
          password: true
        },
        loading: false
      };
    },
    computed: {
      allValid () {
        return Object.values(this.valid).every(Boolean);
      },
      rules () {
        const vm = this;
        return {
          username: [
            {
              validator (rule, value, cb) {
                if (value === '') {
                  vm.valid.username = false;
                  return cb(new Error('Empty username'));
                }
                vm.valid.username = true;
                return cb();
              },
              trigger: 'blur'
            }
          ],
          password: [
            {
              validator (rule, value, cb) {
                if (value === '') {
                  vm.valid.password = false;
                  return cb(new Error('Empty password'));
                }
                vm.valid.password = true;
                return cb();
              },
              trigger: 'blur'
            }
          ]
        };
      },
      disableSubmit () {
        return this.form.username === '' || this.form.password === '';
      }
    },
    methods: {
      handleSubmit () {
        this.$refs.loginForm.validate((valid) => {
          if (valid) {
            const { username, password } = this.form;
            this.loading = true;
            this.requestPostLogin({
              params: {
                username,
                password
              }
            }).then(({ data }) => {
              if (data === 'success') {
                setToken(username);
                this.$router.push({
                  name: 'intro'
                });
              } else {
                this.$message.error('Username or Password incorrect');
                this.$refs.loginForm.resetFields();
              }
            }).finally(() => {
              this.loading = false;
            })
          }
        });
      }
    }
  };
</script>
