package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.PlayerGameHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 玩家游戏历史Mapper接口
 */
@Mapper
public interface PlayerGameHistoryMapper extends BaseMapper<PlayerGameHistory> {

    /**
     * 根据游戏ID查询玩家游戏历史列表
     *
     * @param gameId 游戏ID
     * @return 玩家游戏历史列表
     */
    @Select("SELECT pgh.*, u.* FROM player_game_history pgh " +
            "LEFT JOIN user u ON pgh.user_id = u.id " +
            "WHERE pgh.game_id = #{gameId} AND pgh.deleted = 0")
    List<PlayerGameHistory> selectByGameId(@Param("gameId") Long gameId);
} 