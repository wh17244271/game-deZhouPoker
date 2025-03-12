import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import '../styles/Auth.css';

const Register = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [initialChips, setInitialChips] = useState(1000);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [successful, setSuccessful] = useState(false);
  const navigate = useNavigate();

  // 添加表单验证错误信息
  const [usernameError, setUsernameError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [chipsError, setChipsError] = useState('');

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

  // 验证确认密码
  const validateConfirmPassword = (value) => {
    if (!value) {
      setConfirmPasswordError('请确认密码');
      return false;
    }
    if (value !== password) {
      setConfirmPasswordError('两次输入的密码不一致');
      return false;
    }
    setConfirmPasswordError('');
    return true;
  };

  // 验证筹码
  const validateChips = (value) => {
    if (!value) {
      setChipsError('请输入初始筹码');
      return false;
    }
    if (value < 100) {
      setChipsError('初始筹码不能少于100');
      return false;
    }
    setChipsError('');
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
    if (confirmPassword) {
      validateConfirmPassword(confirmPassword);
    }
  };

  const handleConfirmPasswordChange = (e) => {
    const value = e.target.value;
    setConfirmPassword(value);
    validateConfirmPassword(value);
  };

  const handleChipsChange = (e) => {
    const value = Number(e.target.value);
    setInitialChips(value);
    validateChips(value);
  };

  const handleRegister = (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);
    setSuccessful(false);

    // 表单验证
    const isUsernameValid = validateUsername(username);
    const isPasswordValid = validatePassword(password);
    const isConfirmPasswordValid = validateConfirmPassword(confirmPassword);
    const isChipsValid = validateChips(initialChips);

    if (!isUsernameValid || !isPasswordValid || !isConfirmPasswordValid || !isChipsValid) {
      setLoading(false);
      return;
    }

    AuthService.register(username, password, initialChips)
      .then(response => {
        setMessage(response.data.message || '注册成功，请登录');
        setSuccessful(true);
        setLoading(false);
        
        // 3秒后跳转到登录页面
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();

        setMessage(resMessage);
        setSuccessful(false);
        setLoading(false);
      });
  };

  return (
    <div className="register-page">
      <div className="register-box">
        <h2 className="register-title">注册</h2>
        {message && (
          <div className={`alert ${successful ? 'alert-success' : 'alert-danger'}`}>
            {message}
          </div>
        )}
        <form onSubmit={handleRegister}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              className={`form-control ${usernameError ? 'is-invalid' : ''}`}
              id="username"
              value={username}
              onChange={handleUsernameChange}
              placeholder="请输入用户名 (3-50个字符)"
              disabled={loading || successful}
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
              disabled={loading || successful}
            />
            {passwordError && <div className="invalid-feedback">{passwordError}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="confirmPassword">确认密码</label>
            <input
              type="password"
              className={`form-control ${confirmPasswordError ? 'is-invalid' : ''}`}
              id="confirmPassword"
              value={confirmPassword}
              onChange={handleConfirmPasswordChange}
              placeholder="请再次输入密码"
              disabled={loading || successful}
            />
            {confirmPasswordError && <div className="invalid-feedback">{confirmPasswordError}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="initialChips">初始筹码</label>
            <input
              type="number"
              className={`form-control ${chipsError ? 'is-invalid' : ''}`}
              id="initialChips"
              value={initialChips}
              onChange={handleChipsChange}
              placeholder="请输入初始筹码数量"
              min="100"
              disabled={loading || successful}
            />
            {chipsError && <div className="invalid-feedback">{chipsError}</div>}
            <small className="form-text text-muted">初始筹码不能少于100</small>
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading || successful}
          >
            {loading ? '注册中...' : '注册'}
          </button>
        </form>
        <div className="login-link">
          已有账号？ <Link to="/login">立即登录</Link>
        </div>
      </div>
    </div>
  );
};

export default Register; 