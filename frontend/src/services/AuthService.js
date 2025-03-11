import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || '';

class AuthService {
  /**
   * 用户登录
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {Promise} - 返回登录结果的Promise
   */
  login(username, password) {
    return axios
      .post(`${API_URL}/auth/login`, {
        username,
        password
      })
      .then(response => {
        if (response.data.token) {
          localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
      });
  }

  /**
   * 用户登出
   */
  logout() {
    localStorage.removeItem('user');
  }

  /**
   * 用户注册
   * @param {string} username - 用户名
   * @param {string} email - 邮箱
   * @param {string} password - 密码
   * @returns {Promise} - 返回注册结果的Promise
   */
  register(username, email, password) {
    return axios.post(`${API_URL}/auth/signup`, {
      username,
      email,
      password
    });
  }

  /**
   * 获取当前用户信息
   * @returns {Object|null} - 返回当前用户信息，如果未登录则返回null
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch (e) {
      return null;
    }
  }

  /**
   * 检查用户是否已登录
   * @returns {boolean} - 返回用户是否已登录
   */
  isLoggedIn() {
    const user = this.getCurrentUser();
    return !!user;
  }

  /**
   * 检查用户是否有指定角色
   * @param {string} requiredRole - 需要检查的角色
   * @returns {boolean} - 返回用户是否有指定角色
   */
  hasRole(requiredRole) {
    const user = this.getCurrentUser();
    if (!user || !user.roles) return false;
    
    return user.roles.includes(requiredRole);
  }

  /**
   * 检查用户令牌是否过期
   * @returns {boolean} - 返回用户令牌是否过期
   */
  isTokenExpired() {
    const user = this.getCurrentUser();
    if (!user || !user.expiresAt) return true;
    
    const expiresAt = new Date(user.expiresAt);
    return expiresAt < new Date();
  }

  /**
   * 刷新用户令牌
   * @returns {Promise} - 返回刷新结果的Promise
   */
  refreshToken() {
    const user = this.getCurrentUser();
    if (!user || !user.refreshToken) {
      return Promise.reject('No refresh token available');
    }
    
    return axios
      .post(`${API_URL}/auth/refresh-token`, {
        refreshToken: user.refreshToken
      })
      .then(response => {
        if (response.data.token) {
          // 更新本地存储中的用户信息
          localStorage.setItem('user', JSON.stringify({
            ...user,
            token: response.data.token,
            refreshToken: response.data.refreshToken,
            expiresAt: response.data.expiresAt
          }));
        }
        return response.data;
      });
  }

  /**
   * 更新用户信息
   * @param {Object} userData - 用户数据
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateUserInfo(userData) {
    const user = this.getCurrentUser();
    if (!user) {
      return Promise.reject('User not logged in');
    }
    
    return axios
      .put(`${API_URL}/users/${user.userId}`, userData, {
        headers: {
          Authorization: 'Bearer ' + user.token
        }
      })
      .then(response => {
        // 更新本地存储中的用户信息
        localStorage.setItem('user', JSON.stringify({
          ...user,
          ...response.data
        }));
        return response.data;
      });
  }

  /**
   * 修改密码
   * @param {string} oldPassword - 旧密码
   * @param {string} newPassword - 新密码
   * @returns {Promise} - 返回修改结果的Promise
   */
  changePassword(oldPassword, newPassword) {
    const user = this.getCurrentUser();
    if (!user) {
      return Promise.reject('User not logged in');
    }
    
    return axios.post(
      `${API_URL}/auth/change-password`,
      {
        oldPassword,
        newPassword
      },
      {
        headers: {
          Authorization: 'Bearer ' + user.token
        }
      }
    );
  }

  /**
   * 请求重置密码
   * @param {string} email - 邮箱
   * @returns {Promise} - 返回请求结果的Promise
   */
  requestPasswordReset(email) {
    return axios.post(`${API_URL}/auth/forgot-password`, { email });
  }

  /**
   * 重置密码
   * @param {string} token - 重置令牌
   * @param {string} newPassword - 新密码
   * @returns {Promise} - 返回重置结果的Promise
   */
  resetPassword(token, newPassword) {
    return axios.post(`${API_URL}/auth/reset-password`, {
      token,
      newPassword
    });
  }
}

export default new AuthService(); 