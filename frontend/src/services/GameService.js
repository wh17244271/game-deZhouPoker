import axios from 'axios';
import authHeader from './auth-header';

const API_URL = process.env.REACT_APP_API_URL || '';

class GameService {
  /**
   * 获取当前游戏信息
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回游戏信息的Promise
   */
  getCurrentGame(roomId) {
    return axios.get(`${API_URL}/games/room/${roomId}/current`, { headers: authHeader() });
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
}

export default new GameService(); 