package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.GameHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 游戏历史Mapper接口
 */
@Mapper
public interface GameHistoryMapper extends BaseMapper<GameHistory> {

    /**
     * 根据房间ID查询当前游戏
     *
     * @param roomId 房间ID
     * @return 游戏历史
     */
    @Select("SELECT * FROM game_history WHERE room_id = #{roomId} AND status = 'IN_PROGRESS' AND deleted = 0")
    GameHistory selectCurrentGameByRoomId(@Param("roomId") Long roomId);
} 