package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.GameHistory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏历史服务接口
 */
public interface GameHistoryService extends IService<GameHistory> {

    /**
     * 创建新游戏
     *
     * @param roomId     房间ID
     * @param smallBlind 小盲注
     * @param bigBlind   大盲注
     * @return 创建的游戏历史记录
     */
    GameHistory createGame(Long roomId, BigDecimal smallBlind, BigDecimal bigBlind);

    /**
     * 更新游戏状态
     *
     * @param gameId 游戏ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long gameId, String status);

    /**
     * 更新游戏轮次
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 是否成功
     */
    boolean updateRound(Long gameId, String round);

    /**
     * 更新底池
     *
     * @param gameId 游戏ID
     * @param amount 金额
     * @return 是否成功
     */
    boolean updatePot(Long gameId, BigDecimal amount);

    /**
     * 更新公共牌
     *
     * @param gameId         游戏ID
     * @param communityCards 公共牌
     * @return 是否成功
     */
    boolean updateCommunityCards(Long gameId, String communityCards);

    /**
     * 结束游戏
     *
     * @param gameId 游戏ID
     * @return 是否成功
     */
    boolean endGame(Long gameId);

    /**
     * 获取房间当前游戏
     *
     * @param roomId 房间ID
     * @return 当前游戏
     */
    GameHistory getCurrentGame(Long roomId);

    /**
     * 获取房间历史游戏
     *
     * @param roomId 房间ID
     * @return 历史游戏列表
     */
    List<GameHistory> getRoomGames(Long roomId);
} 