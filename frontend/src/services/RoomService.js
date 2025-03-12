import axios from 'axios';
import authHeader from './auth-header';

const API_URL = process.env.REACT_APP_API_URL || '';

class RoomService {
  /**
   * 获取所有房间
   * @returns {Promise} - 返回所有房间的Promise
   */
  getAllRooms() {
    return axios.get(`${API_URL}/rooms`, { headers: authHeader() });
  }

  /**
   * 获取房间详情
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回房间详情的Promise
   */
  getRoomById(roomId) {
    return axios.get(`${API_URL}/rooms/${roomId}`, { headers: authHeader() });
  }

  /**
   * 创建新房间
   * @param {string} name - 房间名称
   * @param {string} password - 房间密码（可选）
   * @param {number} minPlayers - 最小玩家数
   * @param {number} maxPlayers - 最大玩家数
   * @param {number} smallBlind - 小盲注
   * @param {number} bigBlind - 大盲注
   * @returns {Promise} - 返回创建房间结果的Promise
   */
  createRoom(name, password, minPlayers, maxPlayers, smallBlind, bigBlind) {
    const params = new URLSearchParams();
    params.append('name', name);
    if (password) {
      params.append('password', password);
    }
    params.append('minPlayers', minPlayers);
    params.append('maxPlayers', maxPlayers);
    params.append('smallBlind', smallBlind);
    params.append('bigBlind', bigBlind);
    
    return axios.post(
      `${API_URL}/rooms?${params.toString()}`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 加入房间
   * @param {string} roomId - 房间ID
   * @param {number} seatNumber - 座位号
   * @param {number} buyIn - 买入金额
   * @returns {Promise} - 返回加入房间结果的Promise
   */
  joinRoom(roomId, seatNumber, buyIn) {
    const params = new URLSearchParams();
    params.append('seatNumber', seatNumber);
    params.append('buyIn', buyIn);
    
    return axios.post(
      `${API_URL}/rooms/${roomId}/join?${params.toString()}`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 离开房间
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回离开房间结果的Promise
   */
  leaveRoom(roomId) {
    return axios.post(
      `${API_URL}/rooms/${roomId}/leave`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 获取房间内的玩家
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回房间内玩家的Promise
   */
  getRoomPlayers(roomId) {
    return axios.get(`${API_URL}/rooms/${roomId}/players`, { headers: authHeader() });
  }

  /**
   * 更新房间设置
   * @param {string} roomId - 房间ID
   * @param {Object} settings - 房间设置
   * @returns {Promise} - 返回更新房间设置结果的Promise
   */
  updateRoomSettings(roomId, settings) {
    return axios.put(
      `${API_URL}/rooms/${roomId}`,
      settings,
      { headers: authHeader() }
    );
  }

  /**
   * 删除房间
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回删除房间结果的Promise
   */
  deleteRoom(roomId) {
    return axios.delete(`${API_URL}/rooms/${roomId}`, { headers: authHeader() });
  }

  /**
   * 检查房间密码
   * @param {string} roomId - 房间ID
   * @param {string} password - 房间密码
   * @returns {Promise} - 返回检查结果的Promise
   */
  checkRoomPassword(roomId, password) {
    return axios.post(
      `${API_URL}/rooms/${roomId}/check-password`,
      { password },
      { headers: authHeader() }
    );
  }

  /**
   * 获取房间游戏历史
   * @param {string} roomId - 房间ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise} - 返回房间游戏历史的Promise
   */
  getRoomGameHistory(roomId, page = 0, size = 10) {
    return axios.get(
      `${API_URL}/games/history/room/${roomId}`,
      {
        headers: authHeader(),
        params: { page, size }
      }
    );
  }
}

export default new RoomService(); 