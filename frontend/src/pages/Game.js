import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Alert, Badge, Table } from 'react-bootstrap';
import GameService from '../services/gameService';
import AuthService from '../services/authService';

const Game = () => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [game, setGame] = useState(null);
  const [players, setPlayers] = useState([]);
  const [myCards, setMyCards] = useState(null);
  const [communityCards, setCommunityCards] = useState(null);
  const [dealLoading, setDealLoading] = useState(false);
  
  // 获取当前用户
  const currentUser = AuthService.getCurrentUser();
  
  // 加载游戏数据
  const loadGameData = () => {
    setLoading(true);
    setError('');
    
    GameService.getCurrentGame(roomId)
      .then(response => {
        if (response.data && response.data.success) {
          const gameData = response.data.data.game;
          const playersData = response.data.data.players;
          
          console.log('Game data:', gameData);
          console.log('Players data:', playersData);
          
          setGame(gameData);
          setPlayers(playersData || []);
          
          // 如果游戏已经开始，获取手牌和公共牌
          if (gameData && gameData.id) {
            loadMyCards(gameData.id);
            loadCommunityCards(gameData.id);
          }
        } else {
          setError('获取游戏数据失败');
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
  
  // 加载玩家手牌
  const loadMyCards = (gameId) => {
    if (!currentUser) return;
    
    GameService.getMyCards(gameId)
      .then(response => {
        if (response.data && response.data.success) {
          setMyCards(response.data.data);
        }
      })
      .catch(error => {
        console.error('获取手牌失败:', error);
      });
  };
  
  // 加载公共牌
  const loadCommunityCards = (gameId) => {
    GameService.getCommunityCards(gameId)
      .then(response => {
        if (response.data && response.data.success) {
          setCommunityCards(response.data.data);
        }
      })
      .catch(error => {
        console.error('获取公共牌失败:', error);
      });
  };
  
  // 开始发牌
  const handleDealCards = () => {
    if (!game || !game.id) return;
    
    setDealLoading(true);
    
    GameService.dealCards(game.id)
      .then(response => {
        if (response.data && response.data.success) {
          console.log('发牌成功:', response.data.data);
          loadMyCards(game.id);
          loadCommunityCards(game.id);
        } else {
          setError('发牌失败');
        }
        setDealLoading(false);
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setDealLoading(false);
      });
  };
  
  // 组件挂载时加载游戏数据
  useEffect(() => {
    loadGameData();
    
    // 每10秒刷新一次游戏数据
    const interval = setInterval(() => {
      loadGameData();
    }, 10000);
    
    return () => clearInterval(interval);
  }, [roomId]);
  
  // 检查当前用户是否是房主
  const isUserRoomOwner = () => {
    if (!game || !game.room || !game.room.owner || !currentUser) return false;
    
    const ownerId = game.room.owner.id || game.room.owner.userId || game.room.owner.user_id;
    const userId = currentUser.userId || currentUser.id || currentUser.user_id;
    
    return ownerId === userId;
  };
  
  // 渲染玩家手牌
  const renderMyCards = () => {
    if (!myCards) return '等待发牌...';
    
    const cards = myCards.split(',');
    return cards.map((card, index) => (
      <span key={index} className="card-display">{card}</span>
    ));
  };
  
  // 渲染公共牌
  const renderCommunityCards = () => {
    if (!communityCards) return '等待发牌...';
    
    const cards = communityCards.split(',');
    return cards.map((card, index) => (
      <span key={index} className="card-display">{card}</span>
    ));
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
        <Button variant="primary" as={Link} to={`/rooms/${roomId}`} className="mt-3">
          返回房间
        </Button>
      </Container>
    );
  }
  
  if (!game) {
    return (
      <Container>
        <Alert variant="warning" className="mt-3">
          游戏不存在或已结束
        </Alert>
        <Button variant="primary" as={Link} to={`/rooms/${roomId}`} className="mt-3">
          返回房间
        </Button>
      </Container>
    );
  }
  
  return (
    <Container>
      <h2 className="mt-4 mb-4">德州扑克游戏</h2>
      
      <Row>
        <Col md={8}>
          <Card className="mb-4">
            <Card.Header>游戏信息</Card.Header>
            <Card.Body>
              <p><strong>游戏ID:</strong> {game.id}</p>
              <p><strong>开始时间:</strong> {new Date(game.startTime).toLocaleString()}</p>
              <p><strong>状态:</strong> {game.status}</p>
              
              {isUserRoomOwner() && (
                <Button 
                  variant="primary" 
                  onClick={handleDealCards}
                  disabled={dealLoading}
                  className="mt-3"
                >
                  {dealLoading ? '发牌中...' : '开始发牌'}
                </Button>
              )}
            </Card.Body>
          </Card>
          
          <Card className="mb-4">
            <Card.Header>公共牌</Card.Header>
            <Card.Body>
              <div className="community-cards">
                {renderCommunityCards()}
              </div>
            </Card.Body>
          </Card>
        </Col>
        
        <Col md={4}>
          <Card className="mb-4">
            <Card.Header>我的手牌</Card.Header>
            <Card.Body>
              <div className="my-cards">
                {renderMyCards()}
              </div>
            </Card.Body>
          </Card>
          
          <Card>
            <Card.Header>玩家列表</Card.Header>
            <Card.Body>
              {players.length === 0 ? (
                <p className="text-center">暂无玩家</p>
              ) : (
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th>用户名</th>
                      <th>初始筹码</th>
                    </tr>
                  </thead>
                  <tbody>
                    {players.map(player => (
                      <tr key={player.userId}>
                        <td>
                          {player.user ? player.user.username : '未知用户'}
                          {currentUser && player.userId === (currentUser.userId || currentUser.id) && (
                            <Badge bg="info" className="ms-1">你</Badge>
                          )}
                        </td>
                        <td>{player.initialChips}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
      
      <div className="mt-4 mb-4">
        <Button variant="secondary" as={Link} to={`/rooms/${roomId}`}>
          返回房间
        </Button>
      </div>
    </Container>
  );
};

export default Game; 