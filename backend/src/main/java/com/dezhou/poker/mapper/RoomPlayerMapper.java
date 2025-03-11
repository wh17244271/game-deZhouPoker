package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.RoomPlayer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房间玩家Mapper接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Mapper
public interface RoomPlayerMapper extends BaseMapper<RoomPlayer> {

    /**
     * 根据房间ID查询玩家列表
     *
     * @param roomId 房间ID
     * @return 玩家列表
     */
    @Select("SELECT rp.*, u.* FROM room_player rp " +
            "LEFT JOIN user u ON rp.user_id = u.id " +
            "WHERE rp.room_id = #{roomId} AND rp.deleted = 0")
    List<RoomPlayer> selectByRoomId(@Param("roomId") Long roomId);

    /**
     * 查询用户所在的所有房间
     *
     * @param userId 用户ID
     * @return 房间玩家关系列表
     */
    @Select("SELECT * FROM room_player WHERE user_id = #{userId} AND deleted = 0")
    List<RoomPlayer> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询房间内特定座位的玩家
     *
     * @param roomId 房间ID
     * @param seatNumber 座位号
     * @return 房间玩家关系
     */
    @Select("SELECT * FROM room_player WHERE room_id = #{roomId} AND seat_number = #{seatNumber} AND deleted = 0")
    RoomPlayer selectBySeat(@Param("roomId") Long roomId, @Param("seatNumber") Integer seatNumber);
} 