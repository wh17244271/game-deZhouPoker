package com.dezhou.poker.repository;

import com.dezhou.poker.model.AllinVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * All-in投票仓库接口
 */
@Repository
public interface AllinVoteRepository extends JpaRepository<AllinVote, Long> {

    /**
     * 根据游戏ID查找All-in投票
     *
     * @param gameId 游戏ID
     * @return All-in投票列表
     */
    List<AllinVote> findByGameId(Long gameId);

    /**
     * 根据游戏ID和用户ID查找All-in投票
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return All-in投票
     */
    AllinVote findByGameIdAndUserId(Long gameId, Long userId);

    /**
     * 根据游戏ID统计各选项的票数
     *
     * @param gameId 游戏ID
     * @return 各选项的票数
     */
    @Query("SELECT v.voteOption, COUNT(v) FROM AllinVote v WHERE v.game.id = ?1 GROUP BY v.voteOption")
    List<Object[]> countVotesByGameId(Long gameId);

    /**
     * 根据游戏ID查找最多票数的选项
     *
     * @param gameId 游戏ID
     * @return 最多票数的选项
     */
    @Query("SELECT v.voteOption FROM AllinVote v WHERE v.game.id = ?1 GROUP BY v.voteOption ORDER BY COUNT(v) DESC")
    List<Integer> findMostVotedOptionByGameId(Long gameId);
}
