import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

// 导入页面组件
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import RoomList from './pages/RoomList';
import RoomDetail from './pages/RoomDetail';
import GameTable from './pages/GameTable';
import Profile from './pages/Profile';

// 导入通用组件
import Header from './components/Header';
import PrivateRoute from './components/PrivateRoute';

// 导入服务
import AuthService from './services/AuthService';

// 导入Bootstrap样式
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      console.log('App: Setting current user from localStorage:', user);
      setCurrentUser(user);
    } else {
      console.log('App: No user found in localStorage');
    }
    setLoading(false);
  }, []);

  const logOut = () => {
    console.log('App: Logging out user');
    AuthService.logout();
    setCurrentUser(null);
    // 重定向到登录页面
    window.location.href = '/login';
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <div className="App">
        <Header currentUser={currentUser} logOut={logOut} />
        <div className="container mt-3">
          <Routes>
            <Route path="/" element={currentUser ? <Navigate to="/rooms" /> : <Navigate to="/login" />} />
            <Route path="/home" element={<Home />} />
            <Route path="/login" element={currentUser ? <Navigate to="/rooms" /> : <Login setCurrentUser={setCurrentUser} />} />
            <Route path="/register" element={currentUser ? <Navigate to="/rooms" /> : <Register />} />
            <Route 
              path="/rooms" 
              element={
                <PrivateRoute currentUser={currentUser}>
                  <RoomList />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/rooms/:roomId" 
              element={
                <PrivateRoute currentUser={currentUser}>
                  <RoomDetail currentUser={currentUser} />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/game/:roomId" 
              element={
                <PrivateRoute currentUser={currentUser}>
                  <GameTable currentUser={currentUser} />
                </PrivateRoute>
              } 
            />
            <Route 
              path="/profile" 
              element={
                <PrivateRoute currentUser={currentUser}>
                  <Profile currentUser={currentUser} />
                </PrivateRoute>
              } 
            />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App; 