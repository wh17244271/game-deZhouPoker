import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Table, Badge, Modal, Form, Alert } from 'react-bootstrap';
import RoomService from '../services/RoomService';
import GameService from '../services/GameService';
import AuthService from '../services/AuthService';

const RoomDetail = ({ currentUser }) => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  
  // 添加更多调试信息
  console.log('Props currentUser:', currentUser);
  const storedUser = AuthService.getCurrentUser();
  console.log('Stored user:', storedUser);
  
  // 确保用户对象有效
  const [user, setUser] = useState({ username: '游客', userId: null, currentChips: 0 });
  
  // 在组件挂载时设置用户
  useEffect(() => {
    const validUser = currentUser || storedUser || { username: '游客', userId: null, currentChips: 0 };
    console.log('Setting user to:', validUser);
    setUser(validUser);
  }, [currentUser, storedUser]);
  
  const [room, setRoom] = useState(null);
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [joinError, setJoinError] = useState('');
  const [seatNumber, setSeatNumber] = useState(1);
  const [buyIn, setBuyIn] = useState(100);
  const [password, setPassword] = useState('');
  
  const [startGameLoading, setStartGameLoading] = useState(false);

  // 加载房间详情
  const loadRoomDetails = () => {
    setLoading(true);
    setError('');
    
    RoomService.getRoomById(roomId)
      .then(response => {
        if (response.data && response.data.success) {
          const roomData = response.data.data[0];
          const playersData = response.data.data[1];
          
          console.log('Room data:', roomData);
          console.log('Players data:', playersData);
          
          setRoom(roomData);
          setPlayers(playersData || []);
        } else {
          setError('获取房间详情失败');
        }
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

  // 组件挂载时加载房间详情
  useEffect(() => {
    // 添加调试信息
    console.log('Current user object:', user);
    
    loadRoomDetails();
    
    // 每10秒刷新一次房间详情
    const interval = setInterval(() => {
      loadRoomDetails();
    }, 10000);
    
    return () => clearInterval(interval);
  }, [roomId]);

  // 加入房间
  const handleJoinRoom = (e) => {
    e.preventDefault();
    setJoinError('');
    
    // 检查用户是否登录
    if (!user || !user.userId) {
      setJoinError('请先登录');
      return;
    }
    
    // 表单验证
    if (room && room.password && !password) {
      setJoinError('请输入房间密码');
      return;
    }
    
    if (room && room.password && password !== room.password) {
      setJoinError('房间密码错误');
      return;
    }
    
    if (buyIn <= 0) {
      setJoinError('买入金额必须大于0');
      return;
    }
    
    if (user && user.currentChips < buyIn) {
      setJoinError('您的筹码不足');
      return;
    }
    
    // 检查座位是否已被占用
    const seatTaken = players.some(player => player.seatNumber === seatNumber);
    if (seatTaken) {
      setJoinError('该座位已被占用，请选择其他座位');
      return;
    }
    
    RoomService.joinRoom(roomId, seatNumber, buyIn)
      .then(response => {
        setShowJoinModal(false);
        loadRoomDetails();
        
        // 重置表单
        setSeatNumber(1);
        setBuyIn(100);
        setPassword('');
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setJoinError(resMessage);
      });
  };

  // 离开房间
  const handleLeaveRoom = () => {
    RoomService.leaveRoom(roomId)
      .then(response => {
        navigate('/rooms');
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
      });
  };

  // 检查当前用户是否已在房间中
  const isUserInRoom = () => {
    if (!user || !players || players.length === 0) return false;
    
    // 检查用户ID可能存在的不同属性名
    const userId = user.userId || user.id || user.user_id;
    if (!userId) return false;
    
    return players.some(player => {
      if (!player || !player.user) return false;
      const playerId = player.user.id || player.user.userId || player.user.user_id;
      return playerId === userId;
    });
  };

  // 检查当前用户是否是房主
  const isUserRoomOwner = () => {
    if (!room || !room.owner || !user) return false;
    
    // 检查用户ID可能存在的不同属性名
    const userId = user.userId || user.id || user.user_id;
    if (!userId) return false;
    
    const ownerId = room.owner.id || room.owner.userId || room.owner.user_id;
    return ownerId === userId;
  };

  // 获取可用座位列表
  const getAvailableSeats = () => {
    if (!room) return [];
    
    const takenSeats = players.map(player => player.seatNumber);
    const allSeats = Array.from({ length: room.maxPlayers }, (_, i) => i + 1);
    
    return allSeats.filter(seat => !takenSeats.includes(seat));
  };

  // 开始游戏
  const handleStartGame = () => {
    setStartGameLoading(true);
    
    RoomService.startGame(roomId)
      .then(response => {
        console.log('开始游戏成功:', response);
        navigate(`/game/${roomId}`);
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setStartGameLoading(false);
      });
  };

  if (loading) {
    return (
      <Container>
        <div className="text-center mt-5">
          <p>加载中...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Alert variant="danger" className="mt-3">
          {error}
        </Alert>
        <Button variant="primary" as={Link} to="/rooms" className="mt-3">
          返回房间列表
        </Button>
      </Container>
    );
  }

  if (!room) {
    return (
      <Container>
        <Alert variant="warning" className="mt-3">
          房间不存在或已被删除
        </Alert>
        <Button variant="primary" as={Link} to="/rooms" className="mt-3">
          返回房间列表
        </Button>
      </Container>
    );
  }

  // 确保 room.owner 存在
  const owner = room.owner || { username: '未知', id: null };

  // 判断是否满足开始游戏的最小人数要求
  const canStartGame = isUserRoomOwner() && players.length >= room.minPlayers;

  return (
    <Container>
      <Row className="mb-4">
        <Col md={8}>
          <h2>{room.name}</h2>
          <p>
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
          </p>
        </Col>
        <Col md={4} className="text-end">
          {room.status === 'WAITING' ? (
            isUserInRoom() ? (
              <div className="d-flex justify-content-end">
                {/* 离开房间按钮 */}
                <Button 
                  variant="danger" 
                  onClick={handleLeaveRoom} 
                  className="me-2"
                  disabled={loading}
                >
                  离开房间
                </Button>
                
                {/* 房主开始游戏按钮 - 只有当玩家数量满足最小要求时才启用 */}
                {isUserRoomOwner() && (
                  <Button 
                    variant="primary" 
                    onClick={handleStartGame}
                    disabled={!canStartGame || startGameLoading || loading}
                  >
                    {startGameLoading ? '开始中...' : '开始游戏'}
                  </Button>
                )}
              </div>
            ) : (
              <Button 
                variant="primary" 
                onClick={() => setShowJoinModal(true)}
                disabled={room.currentPlayers >= room.maxPlayers || loading}
              >
                {room.currentPlayers >= room.maxPlayers ? '房间已满' : '加入房间'}
              </Button>
            )
          ) : (
            <Button 
              variant="primary" 
              as={Link} 
              to={`/game/${roomId}`}
              disabled={!isUserInRoom()}
            >
              {isUserInRoom() ? '进入游戏' : '游戏进行中'}
            </Button>
          )}
        </Col>
      </Row>

      <Card className="mb-4">
        <Card.Header>玩家列表</Card.Header>
        <Card.Body>
          {players.length === 0 ? (
            <p className="text-center">暂无玩家</p>
          ) : (
            <Table striped bordered hover responsive>
              <thead>
                <tr>
                  <th>座位号</th>
                  <th>用户名</th>
                  <th>当前筹码</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                {players.map(player => {
                  // 确保 player 和 player.user 存在
                  if (!player) return null;
                  const playerUser = player.user || { username: '未知用户', id: null };
                  
                  return (
                    <tr key={`player-${player.seatNumber}-${playerUser.username}`}>
                      <td>{player.seatNumber}</td>
                      <td>
                        {playerUser.username}
                        {owner && playerUser && owner.id === playerUser.id && (
                          <Badge bg="warning" className="ms-1">房主</Badge>
                        )}
                        {user && playerUser && 
                          ((playerUser.id === (user.userId || user.id)) || 
                           (player.userId === (user.userId || user.id))) && (
                          <Badge bg="info" className="ms-1">你</Badge>
                        )}
                      </td>
                      <td>{player.currentChips}</td>
                      <td>
                        <Badge bg={
                          player.status === 'WAITING' ? 'secondary' : 
                          player.status === 'ACTIVE' ? 'success' : 'info'
                        }>
                          {
                            player.status === 'WAITING' ? '等待中' : 
                            player.status === 'ACTIVE' ? '游戏中' : '已准备'
                          }
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>

      <Row>
        <Col md={12}>
          <Card>
            <Card.Header>房间信息</Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <p><strong>房主:</strong> {owner.username}</p>
                  <p><strong>最小玩家数:</strong> {room.minPlayers}</p>
                  <p><strong>最大玩家数:</strong> {room.maxPlayers}</p>
                </Col>
                <Col md={6}>
                  <p><strong>小盲注:</strong> {room.smallBlind}</p>
                  <p><strong>大盲注:</strong> {room.bigBlind}</p>
                  <p><strong>创建时间:</strong> {new Date(room.createdAt).toLocaleString()}</p>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* 加入房间模态框 */}
      <Modal show={showJoinModal} onHide={() => setShowJoinModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>加入房间</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {joinError && (
            <Alert variant="danger">{joinError}</Alert>
          )}
          <Form onSubmit={handleJoinRoom}>
            {room.password && (
              <Form.Group className="mb-3">
                <Form.Label>房间密码</Form.Label>
                <Form.Control
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="请输入房间密码"
                />
              </Form.Group>
            )}

            <Form.Group className="mb-3">
              <Form.Label>选择座位</Form.Label>
              <Form.Select
                value={seatNumber}
                onChange={(e) => setSeatNumber(Number(e.target.value))}
              >
                {getAvailableSeats().map(seat => (
                  <option key={seat} value={seat}>座位 {seat}</option>
                ))}
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>买入筹码</Form.Label>
              <Form.Control
                type="number"
                value={buyIn}
                onChange={(e) => setBuyIn(Number(e.target.value))}
                min="1"
                max={user && user.currentChips ? user.currentChips : 1000}
              />
              <Form.Text className="text-muted">
                您当前拥有 {user && user.currentChips ? user.currentChips : 0} 筹码
              </Form.Text>
            </Form.Group>

            <div className="d-grid gap-2">
              <Button variant="primary" type="submit">
                加入房间
              </Button>
              <Button variant="secondary" onClick={() => setShowJoinModal(false)}>
                取消
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default RoomDetail; 