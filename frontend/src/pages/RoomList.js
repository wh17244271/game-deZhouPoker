import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Badge, Modal, Form, Alert } from 'react-bootstrap';
import RoomService from '../services/RoomService';
import '../styles/RoomList.css';

const RoomList = () => {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createError, setCreateError] = useState('');
  
  // 创建房间表单数据
  const [roomName, setRoomName] = useState('');
  const [roomPassword, setRoomPassword] = useState('');
  const [minPlayers, setMinPlayers] = useState(2);
  const [maxPlayers, setMaxPlayers] = useState(9);
  const [smallBlind, setSmallBlind] = useState(10);
  const [bigBlind, setBigBlind] = useState(20);

  // 加载房间列表
  const loadRooms = () => {
    setLoading(true);
    setError('');
    
    RoomService.getAllRooms()
      .then(response => {
        setRooms(response.data);
        setLoading(false);
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setLoading(false);
      });
  };

  // 组件挂载时加载房间列表
  useEffect(() => {
    loadRooms();
    
    // 每30秒刷新一次房间列表
    const interval = setInterval(() => {
      loadRooms();
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);

  // 创建房间
  const handleCreateRoom = (e) => {
    e.preventDefault();
    setCreateError('');
    
    // 表单验证
    if (!roomName) {
      setCreateError('请输入房间名称');
      return;
    }
    
    if (minPlayers < 2 || minPlayers > maxPlayers) {
      setCreateError('最小玩家数必须大于等于2且不能大于最大玩家数');
      return;
    }
    
    if (maxPlayers > 9) {
      setCreateError('最大玩家数不能超过9');
      return;
    }
    
    if (smallBlind <= 0 || bigBlind <= 0) {
      setCreateError('盲注金额必须大于0');
      return;
    }
    
    if (bigBlind <= smallBlind) {
      setCreateError('大盲注必须大于小盲注');
      return;
    }
    
    RoomService.createRoom(roomName, roomPassword, minPlayers, maxPlayers, smallBlind, bigBlind)
      .then(response => {
        setShowCreateModal(false);
        loadRooms();
        
        // 重置表单
        setRoomName('');
        setRoomPassword('');
        setMinPlayers(2);
        setMaxPlayers(9);
        setSmallBlind(10);
        setBigBlind(20);
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setCreateError(resMessage);
      });
  };

  return (
    <Container>
      <Row className="mb-4">
        <Col md={8}>
          <h2>房间列表</h2>
        </Col>
        <Col md={4} className="text-end">
          <Button variant="primary" className="create-room-btn" onClick={() => setShowCreateModal(true)}>
            创建新房间
          </Button>
        </Col>
      </Row>

      {loading ? (
        <div className="text-center">
          <p>加载中...</p>
        </div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : rooms.length === 0 ? (
        <Alert variant="info">
          当前没有可用的房间，请创建一个新房间开始游戏。
        </Alert>
      ) : (
        <Row>
          {rooms.map(room => (
            <Col md={4} key={room.id}>
              <Card className="mb-4 room-card">
                <Card.Body>
                  <Card.Title>{room.name}</Card.Title>
                  <Card.Subtitle className="mb-2 text-muted">
                    房主: {room.owner && room.owner.username ? room.owner.username : room.creatorId}
                  </Card.Subtitle>
                  <Card.Text>
                    <Badge bg="primary" className="me-1">
                      玩家: {room.currentPlayers}/{room.maxPlayers}
                    </Badge>
                    <Badge bg="info" className="me-1">
                      小盲/大盲: {room.smallBlind}/{room.bigBlind}
                    </Badge>
                    <Badge bg={room.status === 'WAITING' ? 'success' : 'warning'}>
                      {room.status === 'WAITING' ? '等待中' : '游戏中'}
                    </Badge>
                    {room.password && (
                      <Badge bg="secondary" className="ms-1">
                        密码保护
                      </Badge>
                    )}
                  </Card.Text>
                  <Link to={`/rooms/${room.id}`}>
                    <Button variant="outline-primary" className="w-100">
                      {room.status === 'WAITING' ? '加入房间' : '查看房间'}
                    </Button>
                  </Link>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {/* 创建房间模态框 */}
      <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>创建新房间</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {createError && (
            <Alert variant="danger">{createError}</Alert>
          )}
          <Form onSubmit={handleCreateRoom}>
            <Form.Group className="mb-3">
              <Form.Label>房间名称</Form.Label>
              <Form.Control
                type="text"
                value={roomName}
                onChange={(e) => setRoomName(e.target.value)}
                placeholder="请输入房间名称"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>房间密码 (可选)</Form.Label>
              <Form.Control
                type="password"
                value={roomPassword}
                onChange={(e) => setRoomPassword(e.target.value)}
                placeholder="如需设置密码，请输入"
              />
              <Form.Text className="text-muted">
                如不需要密码保护，请留空
              </Form.Text>
            </Form.Group>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>最小玩家数</Form.Label>
                  <Form.Control
                    type="number"
                    value={minPlayers}
                    onChange={(e) => setMinPlayers(Number(e.target.value))}
                    min="2"
                    max="9"
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>最大玩家数</Form.Label>
                  <Form.Control
                    type="number"
                    value={maxPlayers}
                    onChange={(e) => setMaxPlayers(Number(e.target.value))}
                    min="2"
                    max="9"
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>小盲注</Form.Label>
                  <Form.Control
                    type="number"
                    value={smallBlind}
                    onChange={(e) => setSmallBlind(Number(e.target.value))}
                    min="1"
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>大盲注</Form.Label>
                  <Form.Control
                    type="number"
                    value={bigBlind}
                    onChange={(e) => setBigBlind(Number(e.target.value))}
                    min="2"
                  />
                </Form.Group>
              </Col>
            </Row>

            <div className="d-grid gap-2">
              <Button variant="primary" type="submit">
                创建房间
              </Button>
              <Button variant="secondary" onClick={() => setShowCreateModal(false)}>
                取消
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default RoomList; 