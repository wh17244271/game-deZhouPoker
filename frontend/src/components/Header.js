import React from 'react';
import { Link } from 'react-router-dom';
import { Navbar, Nav, Container, NavDropdown } from 'react-bootstrap';

const Header = ({ currentUser, logOut }) => {
  return (
    <Navbar bg="light" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to={currentUser ? "/rooms" : "/login"}>德州扑克游戏</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/home">首页</Nav.Link>
            {currentUser && (
              <>
                <Nav.Link as={Link} to="/rooms">房间列表</Nav.Link>
              </>
            )}
          </Nav>
          <Nav>
            {currentUser ? (
              <NavDropdown title={`欢迎, ${currentUser.username || '用户'}`} id="basic-nav-dropdown">
                <NavDropdown.Item as={Link} to="/profile">个人资料</NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={logOut}>退出登录</NavDropdown.Item>
              </NavDropdown>
            ) : (
              <>
                <Nav.Link as={Link} to="/login">登录</Nav.Link>
                <Nav.Link as={Link} to="/register">注册</Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header; 