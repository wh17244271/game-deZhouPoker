package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.PlayerGameHistory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 玩家游戏历史服务接口
 */
public interface PlayerGameHistoryService extends IService<PlayerGameHistory> {

    /**
     * 创建玩家游戏历史记录
     *
     * @param gameId       游戏ID
     * @param userId       用户ID
     * @param seatNumber   座位号
     * @param initialChips 初始筹码
     * @param holeCards    手牌
     * @return 创建的玩家游戏历史记录
     */
    PlayerGameHistory createPlayerGameHistory(Long gameId, Long userId, Integer seatNumber, BigDecimal initialChips, String holeCards);

    /**
     * 更新玩家最终筹码
     *
     * @param gameId     游戏ID
     * @param userId     用户ID
     * @param finalChips 最终筹码
     * @return 是否成功
     */
    boolean updateFinalChips(Long gameId, Long userId, BigDecimal finalChips);

    /**
     * 更新玩家获胜状态
     *
     * @param gameId    游戏ID
     * @param userId    用户ID
     * @param isWinner  是否获胜
     * @param handType  牌型
     * @return 是否成功
     */
    boolean updateWinner(Long gameId, Long userId, boolean isWinner, String handType);

    /**
     * 获取游戏玩家列表
     *
     * @param gameId 游戏ID
     * @return 玩家游戏历史记录列表
     */
    List<PlayerGameHistory> getGamePlayers(Long gameId);

    /**
     * 获取用户游戏历史
     *
     * @param userId 用户ID
     * @return 玩家游戏历史记录列表
     */
    List<PlayerGameHistory> getUserGameHistory(Long userId);

    /**
     * 计算用户胜率
     *
     * @param userId 用户ID
     * @return 胜率
     */
    double calculateWinRate(Long userId);

    /**
     * 计算用户总盈利
     *
     * @param userId 用户ID
     * @return 总盈利
     */
    BigDecimal calculateTotalProfit(Long userId);
} 