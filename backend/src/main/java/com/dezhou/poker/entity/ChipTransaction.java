package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 筹码交易实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("chip_transaction")
public class ChipTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private Long gameId;

    /**
     * 交易类型
     */
    @TableField("transaction_type")
    private String transactionType;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 交易时间
     */
    @TableField("transaction_time")
    private LocalDateTime transactionTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户
     */
    @TableField(exist = false)
    private User user;

    /**
     * 游戏
     */
    @TableField(exist = false)
    private GameHistory game;

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        WIN,        // 赢得筹码
        LOSE,       // 输掉筹码
        RECHARGE,   // 充值
        WITHDRAW    // 提现
    }

    /**
     * 获取交易类型枚举
     */
    public TransactionType getTransactionTypeEnum() {
        return TransactionType.valueOf(transactionType);
    }

    /**
     * 设置交易类型枚举
     */
    public void setTransactionTypeEnum(TransactionType transactionType) {
        this.transactionType = transactionType.name();
    }
} 