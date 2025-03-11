package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.ChipTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 筹码交易服务接口
 */
public interface ChipTransactionService extends IService<ChipTransaction> {

    /**
     * 记录筹码交易
     *
     * @param userId     用户ID
     * @param amount     金额
     * @param type       类型
     * @param reason     原因
     * @param operatorId 操作员ID
     * @return 创建的交易记录
     */
    ChipTransaction recordTransaction(Long userId, BigDecimal amount, String type, String reason, Long operatorId);

    /**
     * 记录游戏相关的筹码交易
     *
     * @param userId     用户ID
     * @param amount     金额
     * @param type       类型
     * @param reason     原因
     * @param operatorId 操作员ID
     * @param gameId     游戏ID
     * @return 创建的交易记录
     */
    ChipTransaction recordGameTransaction(Long userId, BigDecimal amount, String type, String reason, Long operatorId, Long gameId);

    /**
     * 获取用户筹码交易记录
     *
     * @param userId 用户ID
     * @return 交易记录列表
     */
    List<ChipTransaction> getUserTransactions(Long userId);

    /**
     * 获取游戏相关的筹码交易记录
     *
     * @param gameId 游戏ID
     * @return 交易记录列表
     */
    List<ChipTransaction> getGameTransactions(Long gameId);
} 