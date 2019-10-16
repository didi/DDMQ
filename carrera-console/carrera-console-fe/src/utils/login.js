import { Cookies } from './index';
const COOKIE_KEY = 'carrera-console-ddmq-login-token'
const cookieExpires = 1; // default 1 day

export const setToken = (token) => {
  Cookies.set(COOKIE_KEY, token, { expires: cookieExpires || 1 })
}

export const getToken = () => {
  const token = Cookies.get(COOKIE_KEY)
  if (token) return token
  else return false
}

export const removeToken = () => {
  Cookies.remove(COOKIE_KEY)
}
