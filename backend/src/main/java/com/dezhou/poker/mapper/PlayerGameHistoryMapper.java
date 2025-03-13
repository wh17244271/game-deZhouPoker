package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.entity.PlayerGameHistoryId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 玩家游戏历史Mapper接口
 *
 * @author CodeGenerator
 * @since 2023-03-12
 */
@Mapper
public interface PlayerGameHistoryMapper extends BaseMapper<PlayerGameHistory> {

    /**
     * 根据游戏ID查询玩家游戏历史
     *
     * @param gameId 游戏ID
     * @return 玩家游戏历史列表
     */
    @Select("SELECT * FROM player_game_history WHERE game_id = #{gameId} AND deleted = 0")
    List<PlayerGameHistory> selectByGameId(@Param("gameId") Long gameId);

    /**
     * 根据复合主键查询玩家游戏历史
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 玩家游戏历史
     */
    @Select("SELECT * FROM player_game_history WHERE game_id = #{gameId} AND user_id = #{userId} AND deleted = 0")
    PlayerGameHistory selectByGameIdAndUserId(@Param("gameId") Long gameId, @Param("userId") Long userId);
    
    /**
     * 根据复合主键ID查询玩家游戏历史
     *
     * @param id 复合主键ID
     * @return 玩家游戏历史
     */
    @Select("SELECT * FROM player_game_history WHERE game_id = #{id.gameId} AND user_id = #{id.userId} AND deleted = 0")
    PlayerGameHistory selectById(@Param("id") PlayerGameHistoryId id);
} 