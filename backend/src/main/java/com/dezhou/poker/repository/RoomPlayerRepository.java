package com.dezhou.poker.repository;

import com.dezhou.poker.entity.PlayerStatus;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.entity.RoomPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房间玩家仓库接口
 */
@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, RoomPlayerId> {

    /**
     * 根据房间ID查找房间玩家
     *
     * @param roomId 房间ID
     * @return 房间玩家列表
     */
    List<RoomPlayer> findByRoomId(Long roomId);

    /**
     * 根据用户ID查找房间玩家
     *
     * @param userId 用户ID
     * @return 房间玩家列表
     */
    List<RoomPlayer> findByUserId(Long userId);

    /**
     * 根据房间ID和座位号查找房间玩家
     *
     * @param roomId     房间ID
     * @param seatNumber 座位号
     * @return 房间玩家
     */
    RoomPlayer findByRoomIdAndSeatNumber(Long roomId, Integer seatNumber);

    /**
     * 根据房间ID和状态查找房间玩家
     *
     * @param roomId 房间ID
     * @param status 状态
     * @return 房间玩家列表
     */
    List<RoomPlayer> findByRoomIdAndStatus(Long roomId, PlayerStatus status);

    /**
     * 根据房间ID删除所有房间玩家
     *
     * @param roomId 房间ID
     */
    void deleteByRoomId(Long roomId);
}
