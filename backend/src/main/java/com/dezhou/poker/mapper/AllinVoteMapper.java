package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.AllinVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * All-in投票Mapper接口
 */
@Mapper
public interface AllinVoteMapper extends BaseMapper<AllinVote> {

    /**
     * 根据游戏ID统计投票结果
     *
     * @param gameId 游戏ID
     * @return 投票结果
     */
    @Select("SELECT vote_option, COUNT(*) as count FROM allin_vote " +
            "WHERE game_id = #{gameId} AND deleted = 0 " +
            "GROUP BY vote_option")
    List<Object[]> countVotesByGameId(@Param("gameId") Long gameId);

    /**
     * 查找最多票数的选项
     *
     * @param gameId 游戏ID
     * @return 最多票数的选项
     */
    @Select("SELECT vote_option FROM allin_vote " +
            "WHERE game_id = #{gameId} AND deleted = 0 " +
            "GROUP BY vote_option " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT 1")
    List<Integer> findMostVotedOptionByGameId(@Param("gameId") Long gameId);
} 