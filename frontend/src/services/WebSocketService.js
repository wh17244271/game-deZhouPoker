import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import AuthService from './AuthService';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscription = null;
    this.isConnected = false;
    this.callbacks = {
      onConnect: () => {},
      onMessage: () => {},
      onError: () => {},
      onDisconnect: () => {}
    };
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectTimeout = null;
    this.roomId = null;
  }

  /**
   * 连接到WebSocket服务器
   * @param {string} roomId - 房间ID
   * @param {Object} callbacks - 回调函数对象
   */
  connect(roomId, callbacks) {
    console.log('WebSocketService: 尝试连接WebSocket, 房间ID:', roomId);
    
    // 如果已经连接，先断开
    if (this.stompClient && this.stompClient.connected) {
      this.disconnect();
    }
    
    // 保存回调函数和房间ID
    this.callbacks = callbacks || this.callbacks;
    this.roomId = roomId;
    
    // 创建STOMP客户端
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      debug: function(str) {
        console.debug(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });
    
    // 设置连接成功回调
    this.stompClient.onConnect = (frame) => {
      console.log('WebSocketService: WebSocket连接成功');
      this.isConnected = true;
      
      // 订阅房间频道
      if (this.roomId) {
        this.subscription = this.stompClient.subscribe(
          `/topic/room/${this.roomId}`,
          (message) => {
            this.onMessageReceived(message);
          }
        );
        
        console.log('WebSocketService: 已订阅房间频道', this.roomId);
        
        // 发送加入消息
        this.stompClient.publish({
          destination: `/app/room/${this.roomId}/join`,
          body: JSON.stringify({ type: 'JOIN' })
        });
      }
      
      // 调用连接成功回调
      if (this.callbacks.onConnect) {
        this.callbacks.onConnect();
      }
    };
    
    // 设置错误回调
    this.stompClient.onStompError = (error) => {
      console.error('WebSocketService: WebSocket连接错误', error);
      
      // 调用错误回调
      if (this.callbacks.onError) {
        this.callbacks.onError(error);
      }
    };
    
    // 设置断开连接回调
    this.stompClient.onWebSocketClose = () => {
      console.log('WebSocketService: WebSocket连接已断开');
      this.isConnected = false;
      
      // 调用断开连接回调
      if (this.callbacks.onDisconnect) {
        this.callbacks.onDisconnect();
      }
      
      // 尝试重新连接
      this.reconnectAttempts++;
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        console.log(`WebSocketService: 尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        this.reconnectTimeout = setTimeout(() => {
          this.connect(this.roomId, this.callbacks);
        }, 5000);
      }
    };
    
    // 激活连接
    this.stompClient.activate();
  }
  
  /**
   * 接收消息回调
   * @param {Object} message - 消息对象
   */
  onMessageReceived(message) {
    try {
      const parsedMessage = JSON.parse(message.body);
      console.log('WebSocketService: 收到消息', parsedMessage);
      
      // 调用消息回调
      if (this.callbacks.onMessage) {
        this.callbacks.onMessage(parsedMessage);
      }
    } catch (error) {
      console.error('WebSocketService: 解析消息错误', error);
      
      // 调用错误回调
      if (this.callbacks.onError) {
        this.callbacks.onError(error);
      }
    }
  }
  
  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  sendMessage(message) {
    if (this.stompClient && this.stompClient.connected && this.roomId) {
      this.stompClient.publish({
        destination: `/app/room/${this.roomId}/message`,
        body: JSON.stringify(message)
      });
    } else {
      console.error('WebSocketService: 无法发送消息，WebSocket未连接');
    }
  }
  
  /**
   * 断开连接
   */
  disconnect() {
    console.log('WebSocketService: 断开WebSocket连接');
    
    // 发送离开消息
    if (this.stompClient && this.stompClient.connected && this.roomId) {
      this.stompClient.publish({
        destination: `/app/room/${this.roomId}/leave`,
        body: JSON.stringify({ type: 'LEAVE' })
      });
    }
    
    // 取消订阅
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
    
    // 断开连接
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
    
    // 重置状态
    this.isConnected = false;
    this.roomId = null;
    this.reconnectAttempts = 0;
    
    // 清除重连定时器
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
  }

  /**
   * 发送聊天消息
   * @param {string} content - 消息内容
   */
  sendChatMessage(content) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      this.stompClient.publish({
        destination: `/app/room/${this.roomId}/chat`,
        body: JSON.stringify({ type: 'CHAT', content })
      });
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      return false;
    }
  }

  /**
   * 发送游戏动作
   * @param {string} gameId - 游戏ID
   * @param {string} actionType - 动作类型 (FOLD, CHECK, CALL, BET, RAISE, ALL_IN)
   * @param {number} amount - 下注金额 (仅对BET, RAISE, ALL_IN有效)
   */
  sendGameAction(gameId, actionType, amount = null) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      const data = {
        gameId,
        actionType
      };

      if (amount !== null && ['BET', 'RAISE', 'ALL_IN'].includes(actionType)) {
        data.amount = amount;
      }

      // 获取当前轮次
      data.round = this.getCurrentRound();

      this.stompClient.publish({
        destination: `/app/room/${this.roomId}/action`,
        body: JSON.stringify({ type: 'ACTION', data })
      });
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      return false;
    }
  }

  /**
   * 发送All-in投票
   * @param {string} gameId - 游戏ID
   * @param {string} option - 投票选项 (ONCE, TWICE, THREE_TIMES)
   */
  sendAllinVote(gameId, option) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      const voteOption = this.getVoteOptionValue(option);
      
      this.stompClient.publish({
        destination: `/app/room/${this.roomId}/allin-vote`,
        body: JSON.stringify({ 
          type: 'ALLIN_VOTE', 
          data: {
            gameId,
            option: voteOption
          }
        })
      });
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      return false;
    }
  }

  /**
   * 获取当前轮次
   * @returns {string} 当前轮次
   */
  getCurrentRound() {
    // 这里应该从游戏状态中获取当前轮次
    // 简化处理，默认返回PRE_FLOP
    return 'PRE_FLOP';
  }

  /**
   * 获取投票选项值
   * @param {string} option - 投票选项
   * @returns {number} 投票选项值
   */
  getVoteOptionValue(option) {
    switch (option) {
      case 'ONCE':
        return 1;
      case 'TWICE':
        return 2;
      case 'THREE_TIMES':
        return 3;
      default:
        return 1;
    }
  }
}

// 导出单例实例
export default new WebSocketService(); 