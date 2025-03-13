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
  const [isSeated, setIsSeated] = useState(false);
  const [dealerPosition, setDealerPosition] = useState(null);
  const [smallBlindPosition, setSmallBlindPosition] = useState(null);
  const [bigBlindPosition, setbigBlindPosition] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(null);
  
  // 游戏信息
  const [communityCards, setCommunityCards] = useState([]);
  const [playerCards, setPlayerCards] = useState([]);
  const [potSize, setPotSize] = useState(0);
  const [currentBet, setCurrentBet] = useState(0);
  const [currentRound, setCurrentRound] = useState('PRE_FLOP');
  const [roundTimeLeft, setRoundTimeLeft] = useState(0);
  
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
    
    // 检查用户是否已入座，如果未入座则自动申请座位
    checkAndSeatPlayer();
    
    // 加载游戏数据 - 使用自动游戏管理
    manageGameState();
    
    // 设置定时器，定期检查游戏状态
    const gameStateTimer = setInterval(() => {
      if (game && game.id) {
        checkGameEndCondition(game.id);
      }
    }, 5000);
    
    // 组件卸载时断开WebSocket连接
    return () => {
      WebSocketService.disconnect();
      clearInterval(gameStateTimer);
      
      // 处理玩家离开
      handleLeaveGame();
    };
  }, [roomId]);
  
  // 检查用户是否已入座，如果未入座则自动申请座位
  const checkAndSeatPlayer = () => {
    // 先检查用户是否已在房间中有座位
    RoomService.getUserRoomStatus(roomId)
      .then(response => {
        if (response.data && response.data.success) {
          const userData = response.data.data;
          if (userData.isSeated) {
            // 用户已入座
            setIsSeated(true);
            setCurrentPlayerSeat(userData.seatNumber);
            addSystemMessage(`您已入座于座位 ${userData.seatNumber}`);
          } else {
            // 用户未入座，自动分配座位
            seatPlayerAutomatically();
          }
        } else {
          // 请求失败，尝试自动入座
          seatPlayerAutomatically();
        }
      })
      .catch(error => {
        console.error('检查用户状态错误:', error);
        // 发生错误，尝试自动入座
        seatPlayerAutomatically();
      });
  };
  
  // 自动分配座位
  const seatPlayerAutomatically = () => {
    RoomService.seatPlayer(roomId, null)
      .then(response => {
        if (response.data && response.data.success) {
          const seatData = response.data.data;
          setIsSeated(true);
          setCurrentPlayerSeat(seatData.seatNumber);
          addSystemMessage(`您已自动入座于座位 ${seatData.seatNumber}`);
          
          // 入座成功后，重新加载游戏状态
          manageGameState();
          
          // 获取玩家手牌（如果游戏已开始）
          if (game && game.id) {
            loadPlayerCards(game.id);
          }
        } else {
          const errorMessage = response.data?.message || '自动入座失败';
          console.error('自动入座失败:', errorMessage);
          setError(errorMessage);
          addSystemMessage('自动入座失败: ' + errorMessage);
        }
      })
      .catch(error => {
        console.error('自动入座错误:', error);
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        addSystemMessage('自动入座错误: ' + resMessage);
      });
  };
  
  // 加载玩家手牌
  const loadPlayerCards = (gameId) => {
    if (!gameId) return;
    
    GameService.getMyCards(gameId)
      .then(response => {
        if (response.data && response.data.success) {
          const cardsData = response.data.data;
          try {
            // 解析玩家手牌
            const parsedCards = PokerUtils.parseCards(cardsData);
            setPlayerCards(parsedCards);
            addSystemMessage('成功获取您的手牌');
          } catch (e) {
            console.error('解析手牌错误:', e);
            setPlayerCards([]);
          }
        } else {
          console.log('尚未获取手牌或游戏未开始');
          setPlayerCards([]);
        }
      })
      .catch(error => {
        console.error('获取手牌错误:', error);
        setPlayerCards([]);
      });
  };
  
  // 管理游戏状态
  const manageGameState = () => {
    setLoading(true);
    setError('');
    
    console.log('管理游戏状态，房间ID:', roomId);
    
    GameService.manageRoomGameState(roomId)
      .then(response => {
        console.log('管理游戏状态响应:', response);
        
        if (response.data && response.data.success) {
          const result = response.data.data;
          console.log('游戏状态管理结果:', result);
          
          // 根据管理结果执行操作
          const action = result.action;
          
          if (action.includes('GAME_STARTED') || action === 'GAME_IN_PROGRESS') {
            // 游戏已开始，加载游戏数据
            loadGameData();
            
            if (action.includes('GAME_STARTED')) {
              addSystemMessage('游戏已开始');
              
              // 如果是新游戏开始，设置庄家位置和计时器
              const gameId = result.gameId || (game && game.id);
              if (gameId) {
                setupDealerAndTimer(gameId);
              }
            }
            
            if (action.includes('DEALT')) {
              addSystemMessage('已自动发牌');
              
              // 如果游戏已开始并发牌，获取玩家手牌
              if (result.gameId) {
                loadPlayerCards(result.gameId);
              }
            }
          } else if (action === 'WAITING_FOR_PLAYERS') {
            addSystemMessage(`等待更多玩家加入，还需要 ${result.playersNeeded} 名玩家`);
            setLoading(false);
          } else if (action === 'CARDS_DEALT') {
            addSystemMessage('已自动发牌');
            loadGameData();
            
            // 获取玩家手牌
            if (result.gameId || (game && game.id)) {
              loadPlayerCards(result.gameId || game.id);
            }
          } else {
            loadGameData();
          }
        } else {
          const errorMessage = response.data?.message || '管理游戏状态失败';
          console.error('管理游戏状态失败:', errorMessage);
          setError(errorMessage);
          setLoading(false);
        }
      })
      .catch(error => {
        console.error('管理游戏状态错误:', error);
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setLoading(false);
        
        // 尝试直接加载游戏数据
        loadGameData();
      });
  };
  
  // 设置庄家位置和计时器
  const setupDealerAndTimer = (gameId) => {
    console.log('设置庄家位置和计时器, 游戏ID:', gameId);
    
    // 设置庄家位置
    GameService.setDealerPosition(gameId)
      .then(response => {
        if (response.data && response.data.success) {
          const result = response.data.data;
          console.log('设置庄家位置成功:', result);
          
          // 更新UI中的庄家、小盲注和大盲注位置
          setDealerPosition(result.dealerSeat);
          setSmallBlindPosition(result.smallBlindSeat);
          setbigBlindPosition(result.bigBlindSeat);
          setCurrentTurn(result.currentTurn);
          
          addSystemMessage(`庄家位置已设置为座位 ${result.dealerSeat}`);
          
          // 从房间设置获取每轮时间
          const roomSettings = game && game.room ? game.room.settings : null;
          const timePerRound = roomSettings && roomSettings.timePerRound ? 
              roomSettings.timePerRound : 30;
          
          // 设置游戏计时器
          GameService.setGameTimer(gameId, timePerRound)
            .then(timerResponse => {
              if (timerResponse.data && timerResponse.data.success) {
                console.log('设置游戏计时器成功:', timerResponse.data.data);
                addSystemMessage(`每轮时间已设置为 ${timePerRound} 秒`);
              }
            })
            .catch(error => {
              console.error('设置游戏计时器错误:', error);
            });
        } else {
          console.error('设置庄家位置失败:', response.data?.message);
        }
      })
      .catch(error => {
        console.error('设置庄家位置错误:', error);
      });
  };
  
  // 检查游戏结束条件
  const checkGameEndCondition = (gameId) => {
    if (!gameId) return;
    
    GameService.checkGameEndCondition(gameId)
      .then(response => {
        if (response.data && response.data.success) {
          const result = response.data.data;
          
          if (result.isEnded) {
            console.log('游戏已结束:', result);
            addSystemMessage('当前游戏已结束');
            
            // 游戏结束，刷新数据
            manageGameState();
          }
        }
      })
      .catch(error => {
        console.error('检查游戏结束条件错误:', error);
      });
  };
  
  // 离开游戏时处理
  const handleLeaveGame = () => {
    if (!roomId) return;
    
    GameService.handlePlayerLeave(roomId)
      .then(response => {
        if (response.data && response.data.success) {
          console.log('已处理玩家离开:', response.data.data);
        }
      })
      .catch(error => {
        console.error('处理玩家离开错误:', error);
      });
  };
  
  // 处理发牌
  const handleDealCards = () => {
    console.log('开始发牌，房间ID:', roomId);
    setLoading(true);
    
    GameService.autoDealCards(roomId)
      .then(response => {
        console.log('发牌响应:', response);
        if (response.data && response.data.success) {
          addSystemMessage('发牌成功');
          
          // 重新加载游戏数据
          loadGameData();
        } else {
          const errorMessage = response.data?.message || '发牌失败';
          console.error('发牌失败:', errorMessage);
          setError(errorMessage);
          addSystemMessage('发牌失败: ' + errorMessage);
          setLoading(false);
        }
      })
      .catch(error => {
        console.error('发牌错误:', error);
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        addSystemMessage('发牌错误: ' + resMessage);
        setLoading(false);
      });
  };
  
  // 加载游戏数据
  const loadGameData = () => {
    setLoading(true);
    setError('');
    
    console.log('正在加载游戏数据，房间ID:', roomId);
    
    GameService.getCurrentGame(roomId)
      .then(response => {
        console.log('获取游戏数据响应:', response);
        
        if (response.data && response.data.success) {
          // 尝试处理不同格式的响应数据
          let gameData, playersData, actionsData;
          
          if (Array.isArray(response.data.data)) {
            // 原有格式：数组形式的数据
            gameData = response.data.data[0];
            playersData = response.data.data[1];
            actionsData = response.data.data[2];
          } else if (response.data.data && response.data.data.game) {
            // 另一种可能的格式：对象形式的数据
            gameData = response.data.data.game;
            playersData = response.data.data.players || [];
            actionsData = response.data.data.actions || [];
          } else {
            // 可能是单个游戏对象
            gameData = response.data.data;
            playersData = [];
            actionsData = [];
            if (gameData && gameData.players) {
              playersData = gameData.players;
            }
            if (gameData && gameData.actions) {
              actionsData = gameData.actions;
            }
          }
          
          console.log('处理后的游戏数据:', { gameData, playersData, actionsData });
          
          // 确保有游戏数据，即使格式不完整也创建基本游戏对象
          if (!gameData && response.data.data) {
            console.log('创建基本游戏对象');
            gameData = {
              id: response.data.data.id || response.data.data.gameId || Date.now(),
              status: response.data.data.status || 'IN_PROGRESS',
              room: {
                id: roomId,
                name: response.data.data.roomName || '房间 ' + roomId,
                smallBlind: response.data.data.smallBlind || 10,
                bigBlind: response.data.data.bigBlind || 20
              }
            };
          }
          
          if (gameData) {
            // 确保游戏对象有房间信息
            if (!gameData.room) {
              gameData.room = {
                id: roomId,
                name: '房间 ' + roomId,
                smallBlind: 10,
                bigBlind: 20
              };
            }
            
            setGame(gameData);
            setPlayers(Array.isArray(playersData) ? playersData : []);
            setActions(Array.isArray(actionsData) ? actionsData : []);
            
            // 处理游戏数据
            processGameData(gameData, 
                         Array.isArray(playersData) ? playersData : [], 
                         Array.isArray(actionsData) ? actionsData : []);
          } else {
            console.error('未找到有效的游戏数据，创建默认游戏对象');
            
            // 创建一个默认的游戏对象
            const defaultGame = {
              id: Date.now(),
              status: 'IN_PROGRESS',
              room: {
                id: roomId,
                name: '房间 ' + roomId,
                smallBlind: 10,
                bigBlind: 20
              }
            };
            
            setGame(defaultGame);
            setPlayers([]);
            setActions([]);
            
            // 处理默认游戏数据
            processGameData(defaultGame, [], []);
          }
        } else {
          const errorMessage = response.data?.message || '获取游戏数据失败';
          console.error('获取游戏数据失败:', errorMessage);
          setError(errorMessage);
          
          // 如果游戏还没开始，尝试创建一个模拟的游戏对象以便能进入游戏界面
          if (errorMessage.includes('未开始') || errorMessage.includes('不存在')) {
            console.log('游戏尚未开始，创建默认游戏对象');
            const defaultGame = {
              id: Date.now(),
              status: 'IN_PROGRESS',
              room: {
                id: roomId,
                name: '房间 ' + roomId,
                smallBlind: 10,
                bigBlind: 20
              }
            };
            
            setGame(defaultGame);
            setPlayers([]);
            setActions([]);
            
            // 处理默认游戏数据
            processGameData(defaultGame, [], []);
            
            // 清除错误，允许用户看到游戏界面
            setError('');
          }
        }
        setLoading(false);
      })
      .catch(error => {
        console.error('加载游戏数据错误:', error);
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setLoading(false);
        
        // 错误情况下也尝试创建模拟游戏对象，确保用户可以看到界面
        console.log('加载游戏出错，创建默认游戏对象');
        const defaultGame = {
          id: Date.now(),
          status: 'IN_PROGRESS',
          room: {
            id: roomId,
            name: '房间 ' + roomId,
            smallBlind: 10,
            bigBlind: 20
          }
        };
        
        setGame(defaultGame);
        setPlayers([]);
        setActions([]);
        
        // 处理默认游戏数据
        processGameData(defaultGame, [], []);
        
        // 清除错误，允许用户看到游戏界面
        setError('');
      });
  };
  
  // 处理游戏数据
  const processGameData = (gameData, playersData, actionsData) => {
    console.log('处理游戏数据:', { gameData, playersData, actionsData });
    
    // 安全检查 - 确保所有数据都有默认值
    if (!gameData) {
      console.error('游戏数据为空');
      return;
    }
    
    // 确保playersData和actionsData是数组
    const validPlayersData = Array.isArray(playersData) ? playersData : [];
    const validActionsData = Array.isArray(actionsData) ? actionsData : [];
    
    // 设置公共牌
    if (gameData.communityCards) {
      try {
        setCommunityCards(PokerUtils.parseCards(gameData.communityCards));
      } catch (e) {
        console.error('解析公共牌错误:', e);
        setCommunityCards([]);
      }
    } else {
      setCommunityCards([]);
    }
    
    // 设置奖池大小
    if (gameData.pot || gameData.potSize) {
      setPotSize(gameData.pot || gameData.potSize);
    } else {
      // 计算奖池大小
      try {
        const potSize = validActionsData.reduce((total, action) => {
          if (['BET', 'CALL', 'RAISE', 'ALL_IN'].includes(action.actionType)) {
            return total + (parseFloat(action.amount) || 0);
          }
          return total;
        }, 0);
        setPotSize(potSize);
      } catch (e) {
        console.error('计算奖池错误:', e);
        setPotSize(0);
      }
    }
    
    // 查找当前玩家
    if (currentUser) {
      try {
        const currentPlayerData = validPlayersData.find(p => {
          // 增强玩家匹配逻辑，处理不同的ID格式
          const playerId = p.user?.id || p.user?.userId || p.userId || p.id;
          const userId = currentUser.userId || currentUser.id;
          return playerId === userId;
        });
        
        setCurrentPlayer(currentPlayerData || null);
        
        if (currentPlayerData) {
          setCurrentPlayerSeat(currentPlayerData.seatNumber);
        }
      } catch (e) {
        console.error('设置当前玩家错误:', e);
      }
    }
    
    // 确定当前轮次
    const rounds = ['PRE_FLOP', 'FLOP', 'TURN', 'RIVER', 'SHOWDOWN'];
    let currentRound = 'PRE_FLOP';
    
    try {
      if (gameData.currentRound) {
        currentRound = gameData.currentRound;
      } else {
        for (let i = rounds.length - 1; i >= 0; i--) {
          const roundActions = validActionsData.filter(a => a.round === rounds[i]);
          if (roundActions.length > 0) {
            currentRound = rounds[i];
            break;
          }
        }
      }
      
      setCurrentRound(currentRound);
    } catch (e) {
      console.error('设置当前轮次错误:', e);
      setCurrentRound('PRE_FLOP');
    }
    
    // 确定当前下注金额
    try {
      if (gameData.currentBet) {
        setCurrentBet(gameData.currentBet);
      } else {
        const roundActions = validActionsData.filter(a => a.round === currentRound);
        if (roundActions.length > 0) {
          const bets = roundActions
            .filter(a => ['BET', 'RAISE'].includes(a.actionType))
            .map(a => parseFloat(a.amount) || 0);
          
          if (bets.length > 0) {
            setCurrentBet(Math.max(...bets));
          } else {
            setCurrentBet(0);
          }
        } else {
          setCurrentBet(0);
        }
      }
    } catch (e) {
      console.error('设置当前下注错误:', e);
      setCurrentBet(0);
    }
    
    // 确定庄家位置和盲注位置
    try {
      // 使用后端提供的位置信息，如果有的话
      if (gameData.dealerPosition !== undefined) {
        setDealerPosition(gameData.dealerPosition);
      }
      
      if (gameData.smallBlindPosition !== undefined) {
        setSmallBlindPosition(gameData.smallBlindPosition);
      }
      
      if (gameData.bigBlindPosition !== undefined) {
        setbigBlindPosition(gameData.bigBlindPosition);
      }
      
      // 如果后端没有提供位置信息，使用简化处理
      if (gameData.dealerPosition === undefined && validPlayersData.length > 0) {
        const sortedPlayers = [...validPlayersData].sort((a, b) => 
          (a.seatNumber || 0) - (b.seatNumber || 0));
        
        if (sortedPlayers.length > 0) {
          setDealerPosition(sortedPlayers[0].seatNumber || 1);
        }
        
        if (sortedPlayers.length > 1) {
          setSmallBlindPosition(sortedPlayers[1].seatNumber || 2);
        }
        
        if (sortedPlayers.length > 2) {
          setbigBlindPosition(sortedPlayers[2].seatNumber || 3);
        }
      }
    } catch (e) {
      console.error('设置位置错误:', e);
    }
    
    // 确定当前回合玩家
    try {
      if (gameData.currentTurn !== undefined) {
        setCurrentTurn(gameData.currentTurn);
      } else {
        // 简化处理：按座位号顺序轮流
        const activePlayers = validPlayersData.filter(p => p.status === 'ACTIVE');
        
        if (activePlayers.length > 0) {
          // 找出最后一个行动的玩家
          const roundActions = validActionsData.filter(a => a.round === currentRound);
          const lastActionPlayer = roundActions.length > 0 
            ? roundActions[roundActions.length - 1].user?.id 
            : null;
          
          if (lastActionPlayer) {
            const lastActionPlayerIndex = activePlayers.findIndex(p => 
              (p.user?.id || p.userId) === lastActionPlayer);
            const nextPlayerIndex = (lastActionPlayerIndex + 1) % activePlayers.length;
            setCurrentTurn(activePlayers[nextPlayerIndex].seatNumber || 1);
          } else {
            // 如果没有行动记录，从大盲注后开始
            const sortedPlayers = [...validPlayersData].sort((a, b) => 
              (a.seatNumber || 0) - (b.seatNumber || 0));
            
            if (sortedPlayers.length > 2) {
              const bigBlindIndex = activePlayers.findIndex(p => 
                p.seatNumber === sortedPlayers[2].seatNumber);
              const nextPlayerIndex = (bigBlindIndex + 1) % activePlayers.length;
              setCurrentTurn(activePlayers[nextPlayerIndex].seatNumber || 1);
            } else {
              setCurrentTurn(activePlayers[0].seatNumber || 1);
            }
          }
        } else if (validPlayersData.length > 0) {
          // 如果没有活跃玩家，使用第一个玩家的座位
          setCurrentTurn(validPlayersData[0].seatNumber || 1);
        } else {
          // 如果没有玩家数据，设置默认值
          setCurrentTurn(1);
        }
      }
    } catch (e) {
      console.error('设置当前回合玩家错误:', e);
      setCurrentTurn(1);
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
    // 处理发牌消息
    else if (message.type === 'DEAL_CARDS') {
      addMessage({
        type: 'SYSTEM',
        content: '发牌完成，游戏开始'
      });
      
      // 如果收到发牌消息，尝试获取玩家手牌
      if (message.gameId || (game && game.id)) {
        loadPlayerCards(message.gameId || game.id);
      }
      
      // 重新加载游戏数据
      loadGameData();
    }
    // 处理回合时间更新
    else if (message.type === 'ROUND_TIME_UPDATE') {
      if (message.data && message.data.timeLeft !== undefined) {
        setRoundTimeLeft(message.data.timeLeft);
        
        // 如果是当前玩家的回合且剩余时间很少，显示警告
        if (isCurrentPlayerTurn() && message.data.timeLeft <= 10) {
          addMessage({
            type: 'SYSTEM',
            content: `警告：您的回合剩余时间还有 ${message.data.timeLeft} 秒`
          });
        }
      }
    }
    // 处理玩家回合通知
    else if (message.type === 'PLAYER_TURN') {
      if (message.userId) {
        // 更新当前回合的玩家
        setCurrentTurn(message.userId);
        
        // 如果是当前玩家的回合，显示通知
        if (currentUser && message.userId === currentUser.userId) {
          addMessage({
            type: 'SYSTEM',
            content: '现在是您的回合，请选择操作'
          });
        } else {
          // 获取玩家信息以显示姓名
          const player = players.find(p => (p.user?.id || p.userId) === message.userId);
          const playerName = player ? (player.user?.username || '玩家') : '玩家';
          
          addMessage({
            type: 'SYSTEM',
            content: `现在是 ${playerName} 的回合`
          });
        }
      }
    }
    // 处理庄家位置更新
    else if (message.type === 'DEALER_POSITION') {
      if (message.data) {
        setDealerPosition(message.data.dealerSeat);
        setSmallBlindPosition(message.data.smallBlindSeat);
        setbigBlindPosition(message.data.bigBlindSeat);
        
        addMessage({
          type: 'SYSTEM',
          content: `庄家位置已设置为座位 ${message.data.dealerSeat}`
        });
      }
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
  
  // 修改离开游戏功能为离开座位但留在房间
  const handleLeaveTable = () => {
    RoomService.leaveTable(roomId)
      .then(response => {
        if (response.data && response.data.success) {
          setIsSeated(false);
          setCurrentPlayerSeat(null);
          setPlayerCards([]);
          addSystemMessage('您已离开牌桌，但仍在房间中');
          
          // 通知后端玩家离开座位
          handleLeaveGame();
          
          // 重新加载游戏状态
          loadGameData();
        } else {
          const errorMessage = response.data?.message || '离开牌桌失败';
          setError(errorMessage);
        }
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
  
  // 修改返回房间按钮，替代离开游戏功能
  const handleReturnToRoom = () => {
    // 简单地导航回房间页面
    navigate(`/rooms/${roomId}`);
  };
  
  // 执行游戏动作（过牌、跟注、下注、加注、弃牌、全押）
  const performAction = (actionType, amount = 0) => {
    if (!game || !game.id || !isCurrentPlayerTurn()) {
      setError('不是您的回合或游戏未开始');
      return;
    }
    
    console.log('执行游戏动作:', actionType, amount);
    
    GameService.performAction(game.id, actionType, amount)
      .then(response => {
        if (response.data && response.data.success) {
          console.log('动作执行成功:', response.data.data);
          
          // 添加系统消息
          addSystemMessage(`您选择了 ${getActionText(actionType)} ${amount > 0 ? amount : ''}`);
          
          // 重新加载游戏数据
          loadGameData();
        } else {
          const errorMessage = response.data?.message || '执行动作失败';
          console.error('执行动作失败:', errorMessage);
          setError(errorMessage);
        }
      })
      .catch(error => {
        console.error('执行动作错误:', error);
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
      });
  };
  
  // 获取动作文本描述
  const getActionText = (actionType) => {
    switch (actionType) {
      case 'CHECK':
        return '过牌';
      case 'CALL':
        return '跟注';
      case 'BET':
        return '下注';
      case 'RAISE':
        return '加注';
      case 'FOLD':
        return '弃牌';
      case 'ALL_IN':
        return '全押';
      default:
        return actionType;
    }
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
      {error && (
        <Alert variant="danger" className="mt-3 mb-3" dismissible onClose={() => setError('')}>
          {error}
        </Alert>
      )}
      
      <Row>
        <Col md={9}>
          <div className="game-container">
            <div className="poker-table" style={{ height: '600px' }}>
              {/* 游戏状态信息 */}
              <div className="game-status text-center mb-3">
                <h4>
                  {game ? (
                    `游戏ID: ${game.id} - 状态: ${game.status || 'IN_PROGRESS'}`
                  ) : '准备开始游戏'}
                </h4>
                {/* 回合计时器 */}
                {roundTimeLeft > 0 && (
                  <div className="round-timer">
                    <span className={`badge ${roundTimeLeft <= 10 ? 'bg-danger' : 'bg-info'}`}>
                      回合剩余时间: {roundTimeLeft}秒
                    </span>
                  </div>
                )}
              </div>
              
              {/* 公共牌 */}
              <div className="community-cards">
                {communityCards && communityCards.length > 0 ? (
                  communityCards.map((card, index) => (
                    <PokerCard key={index} card={card} />
                  ))
                ) : (
                  <div className="text-center text-white p-3 mb-3">
                    <h5>等待发牌...</h5>
                    {isSeated && game && game.id && (
                      <Button variant="success" onClick={handleDealCards} className="mt-2">
                        开始发牌
                      </Button>
                    )}
                  </div>
                )}
              </div>
              
              {/* 奖池 */}
              <div className="pot bg-dark text-white p-2 rounded text-center">
                奖池: {potSize || 0}
              </div>
              
              {/* 玩家座位 */}
              {Array.from({ length: 9 }, (_, i) => i + 1).map(position => {
                const player = players.find(p => p.seatNumber === position);
                const isDealer = position === dealerPosition;
                const isSmallBlind = position === smallBlindPosition;
                const isBigBlind = position === bigBlindPosition;
                const isActive = player && player.status === 'ACTIVE';
                const isCurrent = position === currentTurn;
                const isCurrentUserSeat = position === currentPlayerSeat;
                
                let lastAction = null;
                let betAmount = 0;
                
                if (player) {
                  const action = getPlayerLastAction(player.user?.id || player.userId);
                  if (action) {
                    lastAction = action.actionType;
                    betAmount = getPlayerLastBet(player.user?.id || player.userId);
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
                    currentUser={currentUser}
                    isCurrentUserSeat={isCurrentUserSeat}
                    playerCards={isCurrentUserSeat ? playerCards : null}
                  />
                );
              })}
            </div>
            
            {/* 玩家手牌 - 只有在入座时才显示 */}
            {isSeated && (
              <div className="player-cards text-center mt-3">
                <h5>您的手牌</h5>
                <div className="d-flex justify-content-center">
                  {playerCards && playerCards.length > 0 ? (
                    playerCards.map((card, index) => (
                      <PokerCard key={index} card={card} />
                    ))
                  ) : (
                    <p>等待发牌...</p>
                  )}
                </div>
              </div>
            )}
            
            {/* 游戏动作 - 只有在入座且当前是自己回合时才显示 */}
            {isSeated && (
              <GameActions
                gameId={game ? game.id : null}
                currentRound={currentRound}
                isPlayerTurn={isCurrentPlayerTurn()}
                currentBet={currentBet}
                playerChips={currentPlayer ? currentPlayer.currentChips : 0}
                minRaise={game && game.room ? game.room.smallBlind : 10}
                timeLeft={roundTimeLeft}
                onAction={performAction}
              />
            )}
            
            {/* 游戏控制按钮 */}
            <div className="game-controls mt-3 text-center">
              {!isSeated ? (
                <Button 
                  variant="primary" 
                  className="me-2" 
                  onClick={seatPlayerAutomatically}
                  disabled={loading}
                >
                  {loading ? '处理中...' : '入座游戏'}
                </Button>
              ) : (
                <>
                  {game && game.id && (
                    <Button 
                      variant="primary" 
                      className="me-2" 
                      onClick={handleDealCards}
                      disabled={loading || (communityCards && communityCards.length > 0)}
                    >
                      {loading ? '处理中...' : '发牌'}
                    </Button>
                  )}
                  <Button 
                    variant="secondary" 
                    className="me-2"
                    onClick={handleLeaveTable}
                  >
                    离开牌桌
                  </Button>
                </>
              )}
              <Button variant="outline-primary" onClick={handleReturnToRoom}>
                返回房间
              </Button>
            </div>
          </div>
        </Col>
        
        <Col md={3}>
          <BootstrapCard>
            <BootstrapCard.Header>游戏信息</BootstrapCard.Header>
            <BootstrapCard.Body>
              <p><strong>房间:</strong> {game && game.room ? game.room.name : '房间 ' + roomId}</p>
              <p><strong>小盲/大盲:</strong> {game && game.room ? `${game.room.smallBlind || 10}/${game.room.bigBlind || 20}` : '10/20'}</p>
              <p><strong>当前轮次:</strong> {
                currentRound === 'PRE_FLOP' ? '前翻牌' :
                currentRound === 'FLOP' ? '翻牌' :
                currentRound === 'TURN' ? '转牌' :
                currentRound === 'RIVER' ? '河牌' :
                currentRound === 'SHOWDOWN' ? '摊牌' :
                currentRound
              }</p>
              <p><strong>当前下注:</strong> {currentBet || 0}</p>
              <p><strong>奖池:</strong> {potSize || 0}</p>
              <p><strong>玩家数:</strong> {players ? players.length : 0}</p>
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