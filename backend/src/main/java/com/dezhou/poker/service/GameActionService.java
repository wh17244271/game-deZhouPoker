package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.GameAction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏动作服务接口
 */
public interface GameActionService extends IService<GameAction> {

    /**
     * 记录游戏动作
     *
     * @param gameId     游戏ID
     * @param userId     用户ID
     * @param actionType 动作类型
     * @param amount     金额
     * @param round      轮次
     * @return 创建的游戏动作
     */
    GameAction recordAction(Long gameId, Long userId, String actionType, BigDecimal amount, String round);

    /**
     * 获取游戏动作列表
     *
     * @param gameId 游戏ID
     * @return 游戏动作列表
     */
    List<GameAction> getGameActions(Long gameId);

    /**
     * 获取用户在游戏中的动作列表
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 游戏动作列表
     */
    List<GameAction> getUserGameActions(Long gameId, Long userId);

    /**
     * 获取游戏特定轮次的动作列表
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 游戏动作列表
     */
    List<GameAction> getRoundActions(Long gameId, String round);

    /**
     * 获取用户最后一次动作
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 最后一次动作
     */
    GameAction getLastAction(Long gameId, Long userId);
} 