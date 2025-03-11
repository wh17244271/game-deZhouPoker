import React from 'react';
import { Navigate } from 'react-router-dom';

const PrivateRoute = ({ children, currentUser }) => {
  if (!currentUser) {
    // 用户未登录，重定向到登录页面
    return <Navigate to="/login" />;
  }

  // 用户已登录，渲染子组件
  return children;
};

export default PrivateRoute; 