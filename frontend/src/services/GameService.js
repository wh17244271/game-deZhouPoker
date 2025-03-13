import axios from 'axios';
import authHeader from './auth-header';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class GameService {
  /**
   * 获取当前游戏信息
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回游戏信息的Promise
   */
  getCurrentGame(roomId) {
    console.log('GameService: 获取当前游戏', roomId);
    return axios.get(`${API_URL}/games/room/${roomId}/current`, { headers: authHeader() })
      .then(response => {
        console.log('GameService: 获取当前游戏响应', response);
        return response;
      })
      .catch(error => {
        console.error('GameService: 获取当前游戏错误', error);
        throw error;
      });
  }

  /**
   * 获取游戏历史
   * @param {string} roomId - 房间ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise} - 返回游戏历史的Promise
   */
  getGameHistory(roomId, page = 0, size = 10) {
    return axios.get(`${API_URL}/games/history/room/${roomId}`, {
      headers: authHeader(),
      params: { page, size }
    });
  }

  /**
   * 获取游戏详情
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回游戏详情的Promise
   */
  getGameDetails(gameId) {
    return axios.get(`${API_URL}/games/${gameId}`, { headers: authHeader() });
  }

  /**
   * 获取用户在游戏中的统计数据
   * @param {string} userId - 用户ID
   * @returns {Promise} - 返回用户游戏统计数据的Promise
   */
  getUserGameStats(userId) {
    return axios.get(`${API_URL}/users/${userId}/stats`, { headers: authHeader() });
  }

  /**
   * 获取用户的游戏历史
   * @param {string} userId - 用户ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise} - 返回用户游戏历史的Promise
   */
  getUserGameHistory(userId, page = 0, size = 10) {
    return axios.get(`${API_URL}/users/${userId}/games`, {
      headers: authHeader(),
      params: { page, size }
    });
  }

  /**
   * 获取用户的筹码交易历史
   * @param {string} userId - 用户ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise} - 返回用户筹码交易历史的Promise
   */
  getUserChipTransactions(userId, page = 0, size = 10) {
    return axios.get(`${API_URL}/users/${userId}/chips/transactions`, {
      headers: authHeader(),
      params: { page, size }
    });
  }

  /**
   * 开始新游戏
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回新游戏信息的Promise
   */
  startNewGame(roomId) {
    return axios.post(`${API_URL}/games/start`, { roomId }, { headers: authHeader() });
  }

  /**
   * 结束当前游戏
   * @param {string} gameId - 游戏ID
   * @param {string} communityCards - 公共牌
   * @returns {Promise} - 返回结束游戏结果的Promise
   */
  endGame(gameId, communityCards) {
    return axios.post(
      `${API_URL}/games/${gameId}/end`,
      { communityCards },
      { headers: authHeader() }
    );
  }

  /**
   * 获取游戏动作历史
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回游戏动作历史的Promise
   */
  getGameActions(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/actions`, { headers: authHeader() });
  }

  /**
   * 获取游戏结果
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回游戏结果的Promise
   */
  getGameResults(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/results`, { headers: authHeader() });
  }

  /**
   * 记录游戏动作
   * @param {string} gameId - 游戏ID
   * @param {string} actionType - 动作类型
   * @param {number} amount - 金额
   * @param {string} round - 轮次
   * @returns {Promise} - 返回记录结果的Promise
   */
  recordGameAction(gameId, actionType, amount, round) {
    return axios.post(
      `${API_URL}/games/${gameId}/actions`,
      { actionType, amount, round },
      { headers: authHeader() }
    );
  }

  /**
   * 记录All-in投票
   * @param {string} gameId - 游戏ID
   * @param {number} voteOption - 投票选项
   * @returns {Promise} - 返回记录结果的Promise
   */
  recordAllinVote(gameId, voteOption) {
    return axios.post(
      `${API_URL}/games/${gameId}/allin-votes`,
      { voteOption },
      { headers: authHeader() }
    );
  }

  /**
   * 获取All-in投票结果
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回投票结果的Promise
   */
  getAllinVoteResults(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/allin-votes`, { headers: authHeader() });
  }

  /**
   * 更新获胜者信息
   * @param {string} gameId - 游戏ID
   * @param {string} userId - 用户ID
   * @param {number} finalChips - 最终筹码
   * @param {string} finalHandType - 最终牌型
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateWinner(gameId, userId, finalChips, finalHandType) {
    return axios.post(
      `${API_URL}/games/${gameId}/winners`,
      { userId, finalChips, finalHandType },
      { headers: authHeader() }
    );
  }

  /**
   * 开始发牌
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回发牌结果的Promise
   */
  dealCards(gameId) {
    return axios.post(
      `${API_URL}/games/${gameId}/deal`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 获取玩家手牌
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回玩家手牌的Promise
   */
  getMyCards(gameId) {
    console.log('GameService: 获取玩家手牌', gameId);
    return axios.get(`${API_URL}/games/${gameId}/my-cards`, { headers: authHeader() });
  }

  /**
   * 获取公共牌
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回公共牌的Promise
   */
  getCommunityCards(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/community-cards`, { headers: authHeader() });
  }

  /**
   * 管理房间游戏状态
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回游戏状态管理结果的Promise
   */
  manageRoomGameState(roomId) {
    console.log('GameService: 管理房间游戏状态', roomId);
    return axios.post(`${API_URL}/games/room/${roomId}/manage`, {}, { headers: authHeader() });
  }

  /**
   * 处理玩家离开座位
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回处理结果的Promise
   */
  handlePlayerLeave(roomId) {
    console.log('GameService: 处理玩家离开', roomId);
    return axios.post(`${API_URL}/games/room/${roomId}/player-leave`, {}, { headers: authHeader() });
  }

  /**
   * 自动发牌
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回发牌结果的Promise
   */
  autoDealCards(roomId) {
    console.log('GameService: 自动发牌', roomId);
    return axios.post(`${API_URL}/games/room/${roomId}/deal`, {}, { headers: authHeader() });
  }

  /**
   * 检查游戏结束条件
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回检查结果的Promise
   */
  checkGameEndCondition(gameId) {
    console.log('GameService: 检查游戏结束条件', gameId);
    return axios.get(`${API_URL}/games/${gameId}/check-end`, { headers: authHeader() });
  }

  /**
   * 设置庄家位置
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回设置结果的Promise
   */
  setDealerPosition(gameId) {
    console.log('GameService: 设置庄家位置', gameId);
    return axios.post(`${API_URL}/games/${gameId}/dealer`, {}, { headers: authHeader() });
  }

  /**
   * 设置游戏计时器
   * @param {string} gameId - 游戏ID
   * @param {number} timePerRound - 每轮时间（秒）
   * @returns {Promise} - 返回设置结果的Promise
   */
  setGameTimer(gameId, timePerRound) {
    console.log('GameService: 设置游戏计时器', gameId, timePerRound);
    return axios.post(
      `${API_URL}/games/${gameId}/timer`, 
      { timePerRound }, 
      { headers: authHeader() }
    );
  }

  /**
   * 执行游戏动作
   * @param {string} gameId - 游戏ID
   * @param {string} actionType - 动作类型：CHECK（过牌）、CALL（跟注）、BET（下注）、RAISE（加注）、FOLD（弃牌）、ALL_IN（全押）
   * @param {number} amount - 下注金额
   * @returns {Promise} - 返回执行结果的Promise
   */
  performAction(gameId, actionType, amount = 0) {
    console.log('GameService: 执行游戏动作', gameId, actionType, amount);
    return axios.post(
      `${API_URL}/games/${gameId}/action`, 
      { actionType, amount }, 
      { headers: authHeader() }
    );
  }

  /**
   * 获取当前玩家回合信息
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回当前回合信息的Promise
   */
  getCurrentTurnInfo(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/current-turn`, { headers: authHeader() });
  }

  /**
   * 向下一轮进发
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回下一轮信息的Promise
   */
  advanceToNextRound(gameId) {
    return axios.post(
      `${API_URL}/games/${gameId}/next-round`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 获取游戏配置信息
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回游戏配置信息的Promise
   */
  getGameSettings(gameId) {
    return axios.get(`${API_URL}/games/${gameId}/settings`, { headers: authHeader() });
  }

  /**
   * 更新游戏配置
   * @param {string} gameId - 游戏ID
   * @param {Object} settings - 游戏配置对象
   * @returns {Promise} - 返回更新结果的Promise
   */
  updateGameSettings(gameId, settings) {
    return axios.put(
      `${API_URL}/games/${gameId}/settings`,
      settings,
      { headers: authHeader() }
    );
  }

  /**
   * 发公共牌
   * @param {string} gameId - 游戏ID
   * @param {number} count - 发牌数量
   * @returns {Promise} - 返回发牌结果的Promise
   */
  dealCommunityCards(gameId, count) {
    console.log('GameService: 发公共牌', gameId, count);
    return axios.post(
      `${API_URL}/games/${gameId}/community-cards`, 
      { count }, 
      { headers: authHeader() }
    );
  }

  /**
   * 游戏结算
   * @param {string} gameId - 游戏ID
   * @returns {Promise} - 返回游戏结算结果的Promise
   */
  showdown(gameId) {
    console.log('GameService: 游戏结算', gameId);
    return axios.post(`${API_URL}/games/${gameId}/showdown`, {}, { headers: authHeader() });
  }
}

export default new GameService(); 