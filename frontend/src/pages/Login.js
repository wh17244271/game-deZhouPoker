import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert, Container, Row, Col } from 'react-bootstrap';
import AuthService from '../services/AuthService';
import '../styles/Auth.css';

const Login = ({ setCurrentUser }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  // 添加表单验证错误信息
  const [usernameError, setUsernameError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // 验证用户名
  const validateUsername = (value) => {
    if (!value) {
      setUsernameError('请输入用户名');
      return false;
    }
    if (value.length < 3 || value.length > 50) {
      setUsernameError('用户名长度必须在3到50个字符之间');
      return false;
    }
    setUsernameError('');
    return true;
  };

  // 验证密码
  const validatePassword = (value) => {
    if (!value) {
      setPasswordError('请输入密码');
      return false;
    }
    if (value.length < 6 || value.length > 20) {
      setPasswordError('密码长度必须在6到20个字符之间');
      return false;
    }
    setPasswordError('');
    return true;
  };

  // 处理输入变化
  const handleUsernameChange = (e) => {
    const value = e.target.value;
    setUsername(value);
    validateUsername(value);
  };

  const handlePasswordChange = (e) => {
    const value = e.target.value;
    setPassword(value);
    validatePassword(value);
  };

  const handleLogin = (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);

    // 表单验证
    const isUsernameValid = validateUsername(username);
    const isPasswordValid = validatePassword(password);

    if (!isUsernameValid || !isPasswordValid) {
      setLoading(false);
      return;
    }

    AuthService.login(username, password)
      .then(response => {
        setCurrentUser(response);
        navigate('/rooms');
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();

        setLoading(false);
        setMessage(resMessage);
      });
  };

  return (
    <div className="login-page">
      <div className="login-box">
        <h2 className="login-title">登录</h2>
        {message && (
          <div className="alert alert-danger">{message}</div>
        )}
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              className={`form-control ${usernameError ? 'is-invalid' : ''}`}
              id="username"
              value={username}
              onChange={handleUsernameChange}
              placeholder="请输入用户名 (3-50个字符)"
            />
            {usernameError && <div className="invalid-feedback">{usernameError}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="password">密码</label>
            <input
              type="password"
              className={`form-control ${passwordError ? 'is-invalid' : ''}`}
              id="password"
              value={password}
              onChange={handlePasswordChange}
              placeholder="请输入密码 (6-20个字符)"
            />
            {passwordError && <div className="invalid-feedback">{passwordError}</div>}
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading}
          >
            {loading ? '登录中...' : '登录'}
          </button>
        </form>
        <div className="register-link">
          还没有账号？ <Link to="/register">立即注册</Link>
        </div>
      </div>
    </div>
  );
};

export default Login; 