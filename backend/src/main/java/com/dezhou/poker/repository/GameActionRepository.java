package com.dezhou.poker.repository;

import com.dezhou.poker.entity.GameAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏动作仓库接口
 */
@Repository
public interface GameActionRepository extends JpaRepository<GameAction, Long> {

    /**
     * 根据游戏ID查找游戏动作
     *
     * @param gameId 游戏ID
     * @return 游戏动作列表
     */
    List<GameAction> findByGameId(Long gameId);

    /**
     * 根据游戏ID和用户ID查找游戏动作
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 游戏动作列表
     */
    List<GameAction> findByGameIdAndUserId(Long gameId, Long userId);

    /**
     * 根据游戏ID和轮次查找游戏动作
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 游戏动作列表
     */
    List<GameAction> findByGameIdAndRound(Long gameId, GameAction.GameRound round);

    /**
     * 计算游戏奖池大小
     *
     * @param gameId 游戏ID
     * @return 奖池大小
     */
    @Query("SELECT SUM(a.amount) FROM GameAction a WHERE a.game.id = ?1 AND a.actionType IN ('BET', 'CALL', 'RAISE', 'ALL_IN')")
    BigDecimal calculatePotSize(Long gameId);
}
