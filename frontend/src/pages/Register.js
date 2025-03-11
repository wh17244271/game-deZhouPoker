import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert } from 'react-bootstrap';
import AuthService from '../services/AuthService';

const Register = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [initialChips, setInitialChips] = useState(1000);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [successful, setSuccessful] = useState(false);
  const navigate = useNavigate();

  const handleRegister = (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);
    setSuccessful(false);

    // 表单验证
    if (!username) {
      setMessage('请输入用户名');
      setLoading(false);
      return;
    }

    if (!password) {
      setMessage('请输入密码');
      setLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setMessage('两次输入的密码不一致');
      setLoading(false);
      return;
    }

    if (initialChips < 100) {
      setMessage('初始筹码不能少于100');
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
    <div className="register-container">
      <Card>
        <Card.Body>
          <h2 className="text-center mb-4">注册</h2>
          {message && (
            <Alert variant={successful ? "success" : "danger"}>
              {message}
            </Alert>
          )}
          <Form onSubmit={handleRegister}>
            <Form.Group className="mb-3">
              <Form.Label>用户名</Form.Label>
              <Form.Control
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="请输入用户名"
                disabled={loading || successful}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>密码</Form.Label>
              <Form.Control
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码"
                disabled={loading || successful}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>确认密码</Form.Label>
              <Form.Control
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="请再次输入密码"
                disabled={loading || successful}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>初始筹码</Form.Label>
              <Form.Control
                type="number"
                value={initialChips}
                onChange={(e) => setInitialChips(Number(e.target.value))}
                placeholder="请输入初始筹码数量"
                min="100"
                disabled={loading || successful}
              />
              <Form.Text className="text-muted">
                初始筹码不能少于100
              </Form.Text>
            </Form.Group>

            <Button
              variant="primary"
              type="submit"
              className="w-100 mt-3"
              disabled={loading || successful}
            >
              {loading ? '注册中...' : '注册'}
            </Button>
          </Form>
          <div className="text-center mt-3">
            已有账号？ <Link to="/login">立即登录</Link>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Register; 