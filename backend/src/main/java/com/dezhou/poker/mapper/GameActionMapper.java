package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.GameAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏动作Mapper接口
 */
@Mapper
public interface GameActionMapper extends BaseMapper<GameAction> {

    /**
     * 根据游戏ID查询动作列表
     *
     * @param gameId 游戏ID
     * @return 动作列表
     */
    @Select("SELECT ga.*, u.* FROM game_action ga " +
            "LEFT JOIN user u ON ga.user_id = u.id " +
            "WHERE ga.game_id = #{gameId} AND ga.deleted = 0 " +
            "ORDER BY ga.action_time ASC")
    List<GameAction> selectByGameId(@Param("gameId") Long gameId);

    /**
     * 根据游戏ID和轮次查询动作列表
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 动作列表
     */
    @Select("SELECT ga.*, u.* FROM game_action ga " +
            "LEFT JOIN user u ON ga.user_id = u.id " +
            "WHERE ga.game_id = #{gameId} AND ga.round = #{round} AND ga.deleted = 0 " +
            "ORDER BY ga.action_time ASC")
    List<GameAction> selectByGameIdAndRound(@Param("gameId") Long gameId, @Param("round") String round);

    /**
     * 计算奖池大小
     *
     * @param gameId 游戏ID
     * @return 奖池大小
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM game_action " +
            "WHERE game_id = #{gameId} AND deleted = 0")
    BigDecimal calculatePotSize(@Param("gameId") Long gameId);
} 