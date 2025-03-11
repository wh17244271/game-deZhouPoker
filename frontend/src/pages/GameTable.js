import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card as BootstrapCard, Button, Alert, Modal } from 'react-bootstrap';
import PlayerSeat from '../components/PlayerSeat';
import PokerCard from '../components/Card';
import ChatBox from '../components/ChatBox';
import GameActions from '../components/GameActions';
import AllinVote from '../components/AllinVote';
import WebSocketService from '../services/WebSocketService';
import GameService from '../services/GameService';
import RoomService from '../services/RoomService';
import PokerUtils from '../utils/PokerUtils';
import '../styles/GameTable.css';

const GameTable = ({ currentUser }) => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  
  // 游戏状态
  const [game, setGame] = useState(null);
  const [players, setPlayers] = useState([]);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // 玩家状态
  const [currentPlayer, setCurrentPlayer] = useState(null);
  const [currentPlayerSeat, setCurrentPlayerSeat] = useState(null);
  const [dealerPosition, setDealerPosition] = useState(null);
  const [smallBlindPosition, setSmallBlindPosition] = useState(null);
  const [bigBlindPosition, setbigBlindPosition] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(null);
  
  // 游戏信息
  const [communityCards, setCommunityCards] = useState([]);
  const [potSize, setPotSize] = useState(0);
  const [currentBet, setCurrentBet] = useState(0);
  const [currentRound, setCurrentRound] = useState('PRE_FLOP');
  
  // 聊天消息
  const [messages, setMessages] = useState([]);
  
  // All-in投票
  const [showAllinVote, setShowAllinVote] = useState(false);
  const [voteResults, setVoteResults] = useState({});
  const [mostVotedOption, setMostVotedOption] = useState(null);
  const [hasVoted, setHasVoted] = useState(false);
  
  // 游戏结果
  const [showResults, setShowResults] = useState(false);
  const [winners, setWinners] = useState([]);
  
  // 初始化WebSocket连接
  useEffect(() => {
    // 设置WebSocket回调
    const callbacks = {
      onConnect: () => {
        console.log('WebSocket连接成功');
        addSystemMessage('连接到游戏服务器成功');
      },
      onDisconnect: () => {
        console.log('WebSocket连接断开');
        addSystemMessage('与游戏服务器的连接已断开');
      },
      onMessage: handleWebSocketMessage,
      onError: (error) => {
        console.error('WebSocket错误:', error);
        setError('WebSocket连接错误: ' + error);
      }
    };
    
    // 连接WebSocket
    WebSocketService.connect(roomId, callbacks);
    
    // 加载游戏数据
    loadGameData();
    
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
          const gameData = response.data.data[0];
          const playersData = response.data.data[1];
          const actionsData = response.data.data[2];
          
          setGame(gameData);
          setPlayers(playersData);
          setActions(actionsData);
          
          // 处理游戏数据
          processGameData(gameData, playersData, actionsData);
        } else {
          setError(response.data.message || '获取游戏数据失败');
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
  
  // 处理游戏数据
  const processGameData = (gameData, playersData, actionsData) => {
    if (!gameData || !playersData || !actionsData) return;
    
    // 设置公共牌
    if (gameData.communityCards) {
      setCommunityCards(PokerUtils.parseCards(gameData.communityCards));
    }
    
    // 设置奖池大小
    if (gameData.potSize) {
      setPotSize(gameData.potSize);
    } else {
      // 计算奖池大小
      const potSize = actionsData.reduce((total, action) => {
        if (['BET', 'CALL', 'RAISE', 'ALL_IN'].includes(action.actionType)) {
          return total + action.amount;
        }
        return total;
      }, 0);
      setPotSize(potSize);
    }
    
    // 查找当前玩家
    const currentPlayerData = playersData.find(p => p.user.id === currentUser.userId);
    setCurrentPlayer(currentPlayerData);
    
    if (currentPlayerData) {
      setCurrentPlayerSeat(currentPlayerData.seatNumber);
    }
    
    // 确定当前轮次
    const rounds = ['PRE_FLOP', 'FLOP', 'TURN', 'RIVER', 'SHOWDOWN'];
    let currentRound = 'PRE_FLOP';
    
    for (let i = rounds.length - 1; i >= 0; i--) {
      const roundActions = actionsData.filter(a => a.round === rounds[i]);
      if (roundActions.length > 0) {
        currentRound = rounds[i];
        break;
      }
    }
    
    setCurrentRound(currentRound);
    
    // 确定当前下注金额
    const roundActions = actionsData.filter(a => a.round === currentRound);
    if (roundActions.length > 0) {
      const bets = roundActions
        .filter(a => ['BET', 'RAISE'].includes(a.actionType))
        .map(a => a.amount);
      
      if (bets.length > 0) {
        setCurrentBet(Math.max(...bets));
      }
    }
    
    // 确定庄家位置和盲注位置
    // 简化处理：假设第一个玩家是庄家，第二个是小盲，第三个是大盲
    const sortedPlayers = [...playersData].sort((a, b) => a.seatNumber - b.seatNumber);
    
    if (sortedPlayers.length > 0) {
      setDealerPosition(sortedPlayers[0].seatNumber);
    }
    
    if (sortedPlayers.length > 1) {
      setSmallBlindPosition(sortedPlayers[1].seatNumber);
    }
    
    if (sortedPlayers.length > 2) {
      setbigBlindPosition(sortedPlayers[2].seatNumber);
    }
    
    // 确定当前回合玩家
    // 简化处理：按座位号顺序轮流
    const activePlayers = playersData.filter(p => p.status === 'ACTIVE');
    
    if (activePlayers.length > 0) {
      // 找出最后一个行动的玩家
      const lastActionPlayer = roundActions.length > 0 
        ? roundActions[roundActions.length - 1].user.id 
        : null;
      
      if (lastActionPlayer) {
        const lastActionPlayerIndex = activePlayers.findIndex(p => p.user.id === lastActionPlayer);
        const nextPlayerIndex = (lastActionPlayerIndex + 1) % activePlayers.length;
        setCurrentTurn(activePlayers[nextPlayerIndex].seatNumber);
      } else {
        // 如果没有行动记录，从大盲注后开始
        if (sortedPlayers.length > 2) {
          const bigBlindIndex = activePlayers.findIndex(p => p.seatNumber === sortedPlayers[2].seatNumber);
          const nextPlayerIndex = (bigBlindIndex + 1) % activePlayers.length;
          setCurrentTurn(activePlayers[nextPlayerIndex].seatNumber);
        } else {
          setCurrentTurn(activePlayers[0].seatNumber);
        }
      }
    }
  };
  
  // 处理WebSocket消息
  const handleWebSocketMessage = (message) => {
    console.log('收到WebSocket消息:', message);
    
    // 添加聊天消息
    if (message.type === 'CHAT' || message.type === 'JOIN' || message.type === 'LEAVE') {
      addMessage(message);
    }
    // 处理游戏动作
    else if (message.type === 'ACTION') {
      addMessage({
        type: 'SYSTEM',
        content: message.content
      });
      
      // 重新加载游戏数据
      loadGameData();
    }
    // 处理All-in投票
    else if (message.type === 'ALLIN_VOTE') {
      addMessage({
        type: 'SYSTEM',
        content: message.content
      });
      
      // 显示投票结果
      setShowAllinVote(true);
      
      if (message.data && Array.isArray(message.data) && message.data.length >= 2) {
        setVoteResults(message.data[0]);
        setMostVotedOption(message.data[1]);
        
        // 检查当前用户是否已投票
        if (message.senderId === currentUser.userId) {
          setHasVoted(true);
        }
      }
    }
    // 处理游戏状态更新
    else if (message.type === 'GAME') {
      addMessage({
        type: 'SYSTEM',
        content: message.content
      });
      
      // 处理游戏开始
      if (message.data && Array.isArray(message.data) && message.data.length >= 2) {
        const gameData = message.data[0];
        const playersData = message.data[1];
        
        setGame(gameData);
        setPlayers(playersData);
        
        // 重置游戏状态
        setCommunityCards([]);
        setPotSize(0);
        setCurrentBet(0);
        setCurrentRound('PRE_FLOP');
      }
      // 处理游戏结束
      else if (message.data && message.data.status === 'COMPLETED') {
        setGame(message.data);
        
        // 显示游戏结果
        setShowResults(true);
        
        // 获取获胜者
        const winners = players.filter(p => p.isWinner);
        setWinners(winners);
      }
    }
    // 处理错误消息
    else if (message.type === 'ERROR') {
      addMessage({
        type: 'SYSTEM',
        content: `错误: ${message.content}`
      });
      setError(message.content);
    }
  };
  
  // 添加消息到聊天框
  const addMessage = (message) => {
    setMessages(prevMessages => [...prevMessages, message]);
  };
  
  // 添加系统消息
  const addSystemMessage = (content) => {
    addMessage({
      type: 'SYSTEM',
      content
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
  
  // 检查是否是当前玩家的回合
  const isCurrentPlayerTurn = () => {
    return currentPlayerSeat === currentTurn;
  };
  
  // 获取玩家最后的动作
  const getPlayerLastAction = (playerId) => {
    if (!actions || actions.length === 0) return null;
    
    const playerActions = actions
      .filter(a => a.user.id === playerId && a.round === currentRound)
      .sort((a, b) => new Date(b.actionTime) - new Date(a.actionTime));
    
    return playerActions.length > 0 ? playerActions[0] : null;
  };
  
  // 获取玩家最后的下注金额
  const getPlayerLastBet = (playerId) => {
    const lastAction = getPlayerLastAction(playerId);
    
    if (!lastAction) return 0;
    
    if (['BET', 'CALL', 'RAISE', 'ALL_IN'].includes(lastAction.actionType)) {
      return lastAction.amount;
    }
    
    return 0;
  };
  
  if (loading) {
    return (
      <Container>
        <div className="text-center mt-5">
          <p>加载游戏中...</p>
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
          游戏尚未开始或已结束
        </Alert>
        <Button variant="primary" as={Link} to={`/rooms/${roomId}`} className="mt-3">
          返回房间
        </Button>
      </Container>
    );
  }
  
  return (
    <Container fluid>
      <Row>
        <Col md={9}>
          <div className="game-container">
            <div className="poker-table" style={{ height: '600px' }}>
              {/* 公共牌 */}
              <div className="community-cards">
                {communityCards.length > 0 ? (
                  communityCards.map((card, index) => (
                    <PokerCard key={index} card={card} />
                  ))
                ) : (
                  <div className="text-center text-white">
                    等待发牌...
                  </div>
                )}
              </div>
              
              {/* 奖池 */}
              <div className="pot">
                奖池: {potSize}
              </div>
              
              {/* 玩家座位 */}
              {Array.from({ length: 9 }, (_, i) => i + 1).map(position => {
                const player = players.find(p => p.seatNumber === position);
                const isDealer = position === dealerPosition;
                const isSmallBlind = position === smallBlindPosition;
                const isBigBlind = position === bigBlindPosition;
                const isActive = player && player.status === 'ACTIVE';
                const isCurrent = position === currentTurn;
                
                let lastAction = null;
                let betAmount = 0;
                
                if (player) {
                  const action = getPlayerLastAction(player.user.id);
                  if (action) {
                    lastAction = action.actionType;
                    betAmount = getPlayerLastBet(player.user.id);
                  }
                }
                
                return (
                  <PlayerSeat
                    key={position}
                    player={player}
                    position={position}
                    isActive={isActive}
                    isCurrent={isCurrent}
                    isDealer={isDealer}
                    isSmallBlind={isSmallBlind}
                    isBigBlind={isBigBlind}
                    lastAction={lastAction}
                    betAmount={betAmount}
                  />
                );
              })}
            </div>
            
            {/* 游戏动作 */}
            <GameActions
              gameId={game.id}
              currentRound={currentRound}
              isPlayerTurn={isCurrentPlayerTurn()}
              currentBet={currentBet}
              playerChips={currentPlayer ? currentPlayer.currentChips : 0}
              minRaise={game.room.smallBlind}
            />
            
            {/* 游戏控制按钮 */}
            <div className="game-controls mt-3 text-center">
              <Button variant="secondary" onClick={handleLeaveRoom}>
                离开游戏
              </Button>
            </div>
          </div>
        </Col>
        
        <Col md={3}>
          <BootstrapCard>
            <BootstrapCard.Header>游戏信息</BootstrapCard.Header>
            <BootstrapCard.Body>
              <p><strong>房间:</strong> {game.room.name}</p>
              <p><strong>小盲/大盲:</strong> {game.room.smallBlind}/{game.room.bigBlind}</p>
              <p><strong>当前轮次:</strong> {
                currentRound === 'PRE_FLOP' ? '前翻牌' :
                currentRound === 'FLOP' ? '翻牌' :
                currentRound === 'TURN' ? '转牌' :
                currentRound === 'RIVER' ? '河牌' :
                currentRound === 'SHOWDOWN' ? '摊牌' :
                currentRound
              }</p>
              <p><strong>当前下注:</strong> {currentBet}</p>
              <p><strong>奖池:</strong> {potSize}</p>
            </BootstrapCard.Body>
          </BootstrapCard>
          
          <BootstrapCard className="mt-3">
            <BootstrapCard.Header>聊天</BootstrapCard.Header>
            <BootstrapCard.Body>
              <ChatBox messages={messages} currentUser={currentUser} />
            </BootstrapCard.Body>
          </BootstrapCard>
        </Col>
      </Row>
      
      {/* All-in投票模态框 */}
      <AllinVote
        show={showAllinVote}
        onHide={() => setShowAllinVote(false)}
        gameId={game.id}
        voteResults={voteResults}
        mostVotedOption={mostVotedOption}
        hasVoted={hasVoted}
      />
      
      {/* 游戏结果模态框 */}
      <Modal show={showResults} onHide={() => setShowResults(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>游戏结束</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <h5>获胜者:</h5>
          {winners.length === 0 ? (
            <p>无人获胜</p>
          ) : (
            winners.map((winner, index) => (
              <div key={index} className="winner-info mb-3">
                <p><strong>玩家:</strong> {winner.user.username}</p>
                <p><strong>获胜牌型:</strong> {winner.finalHandType}</p>
                <p><strong>赢得筹码:</strong> {winner.finalChips}</p>
              </div>
            ))
          )}
          
          <h5>最终公共牌:</h5>
          <div className="d-flex justify-content-center mb-3">
            {communityCards.map((card, index) => (
              <PokerCard key={index} card={card} />
            ))}
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" as={Link} to={`/rooms/${roomId}`}>
            返回房间
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default GameTable; 