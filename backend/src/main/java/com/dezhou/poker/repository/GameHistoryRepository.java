package com.dezhou.poker.repository;

import com.dezhou.poker.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏历史记录仓库接口
 */
@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    /**
     * 根据房间ID查找游戏历史记录
     *
     * @param roomId 房间ID
     * @return 游戏历史记录列表
     */
    List<GameHistory> findByRoomId(Long roomId);

    /**
     * 根据房间ID和状态查找游戏历史记录
     *
     * @param roomId 房间ID
     * @param status 状态
     * @return 游戏历史记录列表
     */
    List<GameHistory> findByRoomIdAndStatus(Long roomId, GameHistory.GameStatus status);

    /**
     * 根据开始时间范围查找游戏历史记录
     *
     * @param startFrom 开始时间起点
     * @param startTo   开始时间终点
     * @return 游戏历史记录列表
     */
    List<GameHistory> findByStartTimeBetween(LocalDateTime startFrom, LocalDateTime startTo);

    /**
     * 查找当前进行中的游戏
     *
     * @param roomId 房间ID
     * @return 游戏历史记录
     */
    GameHistory findFirstByRoomIdAndStatusOrderByStartTimeDesc(Long roomId, GameHistory.GameStatus status);
}
