import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import AuthService from './AuthService';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscription = null;
    this.isConnected = false;
    this.callbacks = {};
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
  connect(roomId, callbacks = {}) {
    if (this.isConnected) {
      console.log('WebSocket已连接，无需重新连接');
      return;
    }

    this.roomId = roomId;
    this.callbacks = callbacks;

    // 获取当前用户的认证令牌
    const user = AuthService.getCurrentUser();
    if (!user || !user.token) {
      console.error('未找到认证令牌，无法连接WebSocket');
      if (this.callbacks.onError) {
        this.callbacks.onError('未找到认证令牌，无法连接WebSocket');
      }
      return;
    }

    // 构建WebSocket URL
    const API_URL = process.env.REACT_APP_API_URL || '';
    const wsUrl = `${API_URL}/ws`;

    try {
      // 使用SockJS创建WebSocket连接
      const socket = new SockJS(wsUrl);
      this.stompClient = Stomp.over(socket);
      
      // 禁用调试日志
      this.stompClient.debug = null;

      // 设置连接头
      const headers = {
        'Authorization': 'Bearer ' + user.token
      };

      // 连接WebSocket
      this.stompClient.connect(
        headers,
        this.handleConnect.bind(this),
        this.handleError.bind(this)
      );
    } catch (error) {
      console.error('WebSocket连接错误:', error);
      if (this.callbacks.onError) {
        this.callbacks.onError(error.message);
      }
    }
  }

  /**
   * 断开WebSocket连接
   */
  disconnect() {
    if (this.stompClient) {
      if (this.subscription) {
        this.subscription.unsubscribe();
        this.subscription = null;
      }
      
      this.stompClient.disconnect();
      this.stompClient = null;
      this.isConnected = false;
      this.roomId = null;
      
      // 清除重连定时器
      if (this.reconnectTimeout) {
        clearTimeout(this.reconnectTimeout);
        this.reconnectTimeout = null;
      }
      
      console.log('WebSocket连接已断开');
      
      if (this.callbacks.onDisconnect) {
        this.callbacks.onDisconnect();
      }
    }
  }

  /**
   * 处理连接成功
   */
  handleConnect() {
    console.log('WebSocket连接已建立');
    this.isConnected = true;
    this.reconnectAttempts = 0;

    // 订阅房间主题
    this.subscription = this.stompClient.subscribe(
      `/topic/room.${this.roomId}`,
      this.handleMessage.bind(this)
    );

    // 发送加入房间消息
    this.sendJoinMessage();

    if (this.callbacks.onConnect) {
      this.callbacks.onConnect();
    }
  }

  /**
   * 处理WebSocket消息
   * @param {Object} message - STOMP消息对象
   */
  handleMessage(message) {
    try {
      const parsedMessage = JSON.parse(message.body);
      
      if (this.callbacks.onMessage) {
        this.callbacks.onMessage(parsedMessage);
      }
    } catch (error) {
      console.error('解析消息失败:', error, message.body);
    }
  }

  /**
   * 处理WebSocket错误
   * @param {Error} error - 错误对象
   */
  handleError(error) {
    console.error('WebSocket错误:', error);
    this.isConnected = false;
    
    if (this.callbacks.onError) {
      this.callbacks.onError(error);
    }

    // 尝试重新连接
    this.attemptReconnect();
  }

  /**
   * 尝试重新连接
   */
  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts || !this.roomId) {
      console.log('达到最大重连次数或房间ID不存在，停止重连');
      return;
    }

    this.reconnectAttempts++;
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
    
    console.log(`尝试在${delay}毫秒后重新连接 (尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    this.reconnectTimeout = setTimeout(() => {
      console.log(`正在重新连接... (尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      this.connect(this.roomId, this.callbacks);
    }, delay);
  }

  /**
   * 发送加入房间消息
   */
  sendJoinMessage() {
    if (!this.isConnected || !this.stompClient) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      this.stompClient.send(
        `/app/room/${this.roomId}/join`,
        {},
        JSON.stringify({ type: 'JOIN', content: '加入房间' })
      );
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      return false;
    }
  }

  /**
   * 发送聊天消息
   * @param {string} content - 消息内容
   */
  sendChatMessage(content) {
    if (!this.isConnected || !this.stompClient) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      this.stompClient.send(
        `/app/room/${this.roomId}/chat`,
        {},
        JSON.stringify({ type: 'CHAT', content })
      );
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
    if (!this.isConnected || !this.stompClient) {
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

      this.stompClient.send(
        `/app/room/${this.roomId}/action`,
        {},
        JSON.stringify({ type: 'ACTION', data })
      );
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
    if (!this.isConnected || !this.stompClient) {
      console.error('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      const voteOption = this.getVoteOptionValue(option);
      
      this.stompClient.send(
        `/app/room/${this.roomId}/allin-vote`,
        {},
        JSON.stringify({ 
          type: 'ALLIN_VOTE', 
          data: {
            gameId,
            voteOption
          }
        })
      );
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