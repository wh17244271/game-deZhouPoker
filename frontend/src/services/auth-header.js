/**
 * 生成认证请求头
 * 从本地存储中获取用户令牌，并创建包含Authorization头的对象
 * @returns {Object} 包含Authorization头的对象，如果没有令牌则返回空对象
 */
export default function authHeader() {
  try {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      console.warn('authHeader: No user found in localStorage');
      return {};
    }
    
    const user = JSON.parse(userStr);
    
    if (!user) {
      console.warn('authHeader: User is null or undefined after parsing');
      return {};
    }
    
    // 尝试从不同的可能位置获取 accessToken
    let token = null;
    
    if (user.accessToken) {
      token = user.accessToken;
    } else if (user.token) {
      token = user.token;
    } else if (user.access_token) {
      token = user.access_token;
    } else if (user.jwt) {
      token = user.jwt;
    }
    
    if (token) {
      console.log('authHeader: Token found, generating Authorization header');
      return { Authorization: 'Bearer ' + token };
    } else {
      console.warn('authHeader: No token found in user object:', Object.keys(user));
      return {};
    }
  } catch (error) {
    console.error('Error generating auth header:', error);
    return {};
  }
} 