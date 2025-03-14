import axios from 'axios';
import authHeader from './auth-header';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

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
  getRoomDetails(roomId) {
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
   * @returns {Promise} - 返回加入房间结果的Promise
   */
  joinRoom(roomId) {
    console.log('RoomService: 加入房间', roomId);
    return axios.post(`${API_URL}/rooms/${roomId}/join`, {}, { headers: authHeader() });
  }

  /**
   * 离开房间
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回离开房间结果的Promise
   */
  leaveRoom(roomId) {
    console.log('RoomService: 离开房间', roomId);
    return axios.post(`${API_URL}/rooms/${roomId}/leave`, {}, { headers: authHeader() });
  }

  /**
   * 玩家入座
   * @param {string} roomId - 房间ID
   * @param {number} seatNumber - 座位号，如果为null则自动分配座位
   * @returns {Promise} - 返回入座结果的Promise
   */
  seatPlayer(roomId, seatNumber) {
    console.log('RoomService: 入座请求', roomId, seatNumber);
    const requestData = seatNumber !== null ? { seatNumber } : {};
    
    return axios.post(
      `${API_URL}/rooms/${roomId}/seat`, 
      requestData, 
      { headers: authHeader() }
    ).then(response => {
      console.log('RoomService: 入座响应', response);
      return response;
    }).catch(error => {
      console.error('RoomService: 入座错误', error);
      throw error;
    });
  }

  /**
   * 离开座位但留在房间
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回操作结果的Promise
   */
  leaveTable(roomId) {
    console.log('RoomService: 离开牌桌', roomId);
    return axios.post(`${API_URL}/rooms/${roomId}/unseat`, {}, { headers: authHeader() });
  }

  /**
   * 获取房间玩家列表
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回玩家列表的Promise
   */
  getRoomPlayers(roomId) {
    return axios.get(`${API_URL}/rooms/${roomId}/players`, { headers: authHeader() });
  }

  /**
   * 获取当前用户在房间的状态信息
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回用户在房间的状态
   */
  getUserRoomStatus(roomId) {
    console.log('RoomService: 获取用户房间状态', roomId);
    return axios.get(`${API_URL}/rooms/${roomId}/my-status`, { headers: authHeader() });
  }

  /**
   * 更新房间设置
   * @param {string} roomId - 房间ID
   * @param {Object} settings - 房间设置
   * @returns {Promise} - 返回更新房间设置结果的Promise
   */
  updateRoomSettings(roomId, settings) {
    return axios.put(`${API_URL}/rooms/${roomId}/settings`, settings, { headers: authHeader() });
  }

  /**
   * 删除房间
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回删除房间结果的Promise
   */
  deleteRoom(roomId) {
    return axios.delete(
      `${API_URL}/rooms/${roomId}`,
      { headers: authHeader() }
    );
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

  /**
   * 准备游戏
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回准备结果的Promise
   */
  readyGame(roomId) {
    return axios.post(
      `${API_URL}/rooms/${roomId}/ready`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 取消准备
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回取消准备结果的Promise
   */
  cancelReady(roomId) {
    return axios.post(
      `${API_URL}/rooms/${roomId}/cancel-ready`,
      {},
      { headers: authHeader() }
    );
  }

  /**
   * 开始游戏
   * @param {string} roomId - 房间ID
   * @returns {Promise} - 返回开始游戏结果的Promise
   */
  startGame(roomId) {
    return axios.post(
      `${API_URL}/rooms/${roomId}/start`,
      {},
      { headers: authHeader() }
    );
  }
}

export default new RoomService(); 