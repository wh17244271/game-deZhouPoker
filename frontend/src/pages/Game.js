import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Alert, Badge, Table } from 'react-bootstrap';
import GameService from '../services/gameService';
import AuthService from '../services/authService';
import WebSocketService from '../services/WebSocketService';

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
  const [wsConnected, setWsConnected] = useState(false);
  
  // 获取当前用户
  const currentUser = AuthService.getCurrentUser();
  
  // 初始化WebSocket连接
  useEffect(() => {
    // WebSocket回调函数
    const wsCallbacks = {
      onConnect: () => {
        console.log('WebSocket连接成功');
        setWsConnected(true);
      },
      onDisconnect: () => {
        console.log('WebSocket连接断开');
        setWsConnected(false);
      },
      onError: (error) => {
        console.error('WebSocket错误:', error);
        setError('WebSocket连接错误: ' + (typeof error === 'string' ? error : '连接失败'));
      },
      onMessage: (message) => {
        console.log('收到WebSocket消息:', message);
        // 根据消息类型处理不同的消息
        if (message.type === 'GAME_UPDATE') {
          // 游戏状态更新，刷新游戏数据
          loadGameData();
        } else if (message.type === 'DEAL_CARDS') {
          // 发牌消息，刷新手牌和公共牌
          if (game && game.id) {
            loadMyCards(game.id);
            loadCommunityCards(game.id);
          }
        }
      }
    };
    
    // 连接WebSocket
    WebSocketService.connect(roomId, wsCallbacks);
    
    // 组件卸载时断开WebSocket连接
    return () => {
      WebSocketService.disconnect();
    };
  }, [roomId]);
  
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
  
  // 组件挂载时加载游戏数据
  useEffect(() => {
    loadGameData();
    
    // 每30秒刷新一次游戏数据（作为WebSocket的备用机制）
    const interval = setInterval(() => {
      loadGameData();
    }, 30000);
    
    return () => clearInterval(interval);
  }, [roomId]);
  
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
  
  // 检查当前用户是否是房主
  const isUserRoomOwner = () => {
    if (!game) return false;
    
    // 获取房间信息，可能从game.room或通过其他方式获取
    const room = game.room;
    if (!room || !room.owner) {
      // 如果没有房间信息，尝试通过roomId获取
      return false;
    }
    
    if (!currentUser) return false;
    
    const ownerId = room.owner.id || room.owner.userId || room.owner.user_id;
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
  
  // 获取玩家名称
  const getPlayerName = (player) => {
    if (!player) return '未知玩家';
    if (player.user && player.user.username) return player.user.username;
    return `玩家${player.userId}`;
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
      
      {/* WebSocket连接状态 */}
      <div className="mb-3">
        <Badge bg={wsConnected ? "success" : "danger"}>
          {wsConnected ? "实时连接已建立" : "实时连接未建立"}
        </Badge>
        {!wsConnected && (
          <Button 
            variant="outline-primary" 
            size="sm" 
            className="ms-2"
            onClick={() => {
              WebSocketService.disconnect();
              WebSocketService.connect(roomId, {
                onConnect: () => setWsConnected(true),
                onDisconnect: () => setWsConnected(false),
                onError: (error) => setError('WebSocket连接错误: ' + (typeof error === 'string' ? error : '连接失败')),
                onMessage: (message) => {
                  console.log('收到WebSocket消息:', message);
                  if (message.type === 'GAME_UPDATE') loadGameData();
                  else if (message.type === 'DEAL_CARDS' && game && game.id) {
                    loadMyCards(game.id);
                    loadCommunityCards(game.id);
                  }
                }
              });
            }}
          >
            重新连接
          </Button>
        )}
      </div>
      
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
                      <th>状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    {players.map(player => (
                      <tr key={player.userId || Math.random()}>
                        <td>
                          {getPlayerName(player)}
                          {currentUser && player.userId === (currentUser.userId || currentUser.id) && (
                            <Badge bg="info" className="ms-1">你</Badge>
                          )}
                        </td>
                        <td>{player.initialChips || 0}</td>
                        <td>
                          <Badge bg={player.holeCards ? "success" : "secondary"}>
                            {player.holeCards ? "已发牌" : "等待发牌"}
                          </Badge>
                        </td>
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