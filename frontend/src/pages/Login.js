import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert } from 'react-bootstrap';
import AuthService from '../services/AuthService';

const Login = ({ setCurrentUser }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);

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
    <div className="login-container">
      <Card>
        <Card.Body>
          <h2 className="text-center mb-4">登录</h2>
          {message && (
            <Alert variant="danger">{message}</Alert>
          )}
          <Form onSubmit={handleLogin}>
            <Form.Group className="mb-3">
              <Form.Label>用户名</Form.Label>
              <Form.Control
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="请输入用户名"
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>密码</Form.Label>
              <Form.Control
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码"
              />
            </Form.Group>

            <Button
              variant="primary"
              type="submit"
              className="w-100 mt-3"
              disabled={loading}
            >
              {loading ? '登录中...' : '登录'}
            </Button>
          </Form>
          <div className="text-center mt-3">
            还没有账号？ <Link to="/register">立即注册</Link>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Login; 