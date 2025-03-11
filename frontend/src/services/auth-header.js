/**
 * 生成认证请求头
 * 从本地存储中获取用户令牌，并创建包含Authorization头的对象
 * @returns {Object} 包含Authorization头的对象，如果没有令牌则返回空对象
 */
export default function authHeader() {
  const user = JSON.parse(localStorage.getItem('user'));

  if (user && user.token) {
    return { Authorization: 'Bearer ' + user.token };
  } else {
    return {};
  }
} 