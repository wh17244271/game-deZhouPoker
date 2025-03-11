package com.dezhou.poker.repository;

import com.dezhou.poker.model.PlayerGameHistory;
import com.dezhou.poker.model.PlayerGameHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 玩家游戏历史记录仓库接口
 */
@Repository
public interface PlayerGameHistoryRepository extends JpaRepository<PlayerGameHistory, PlayerGameHistoryId> {

    /**
     * 根据游戏ID查找玩家游戏历史记录
     *
     * @param gameId 游戏ID
     * @return 玩家游戏历史记录列表
     */
    List<PlayerGameHistory> findByGameId(Long gameId);

    /**
     * 根据用户ID查找玩家游戏历史记录
     *
     * @param userId 用户ID
     * @return 玩家游戏历史记录列表
     */
    List<PlayerGameHistory> findByUserId(Long userId);

    /**
     * 根据游戏ID和是否获胜查找玩家游戏历史记录
     *
     * @param gameId   游戏ID
     * @param isWinner 是否获胜
     * @return 玩家游戏历史记录列表
     */
    List<PlayerGameHistory> findByGameIdAndIsWinner(Long gameId, Boolean isWinner);
}
