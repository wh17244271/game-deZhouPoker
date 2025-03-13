package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.RoomPlayer;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间玩家服务接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
public interface RoomPlayerService extends IService<RoomPlayer> {

    /**
     * 加入房间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param seatNumber 座位号
     * @param buyIn 买入金额
     * @return 房间玩家关系
     */
    RoomPlayer joinRoom(Long roomId, Long userId, Integer seatNumber, BigDecimal buyIn);

    /**
     * 离开房间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveRoom(Long roomId, Long userId);

    /**
     * 获取房间内的所有玩家
     *
     * @param roomId 房间ID
     * @return 玩家列表
     */
    List<RoomPlayer> getRoomPlayers(Long roomId);

    /**
     * 获取用户所在的所有房间
     *
     * @param userId 用户ID
     * @return 房间玩家关系列表
     */
    List<RoomPlayer> getUserRooms(Long userId);

    /**
     * 更新玩家状态
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long roomId, Long userId, String status);

    /**
     * 更新玩家筹码
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param amount 筹码变动量
     * @return 是否成功
     */
    boolean updateChips(Long roomId, Long userId, BigDecimal amount);

    /**
     * 为玩家分配座位
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param seatNumber 座位号（可为空，自动分配）
     * @return 更新后的房间玩家对象
     */
    RoomPlayer assignSeat(Long roomId, Long userId, Integer seatNumber);

    /**
     * 获取特定房间内特定用户的信息
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 房间玩家信息
     */
    RoomPlayer getRoomPlayer(Long roomId, Long userId);
} 