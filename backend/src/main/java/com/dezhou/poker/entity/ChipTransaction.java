package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
@TableName("chip_transactions")
@Entity
@Table(name = "chip_transactions")
public class ChipTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        BUY_IN,     // 买入
        CASH_OUT,   // 提现
        WIN,        // 游戏获胜
        LOSE,       // 游戏失败
        DEPOSIT,    // 充值 (兼容旧代码)
        WITHDRAW    // 提现 (兼容旧代码)
    }

    /**
     * 交易ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "transaction_id", type = IdType.AUTO)
    @Column(name = "transaction_id")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    /**
     * 游戏ID
     */
    @Column(name = "game_id")
    @TableField("game_id")
    private Long gameId;

    /**
     * 交易金额
     */
    @Column(name = "amount")
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 交易类型
     */
    @Column(name = "transaction_type")
    @TableField("transaction_type")
    private String transactionType;

    /**
     * 交易类型 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private String type;

    /**
     * 交易时间
     */
    @Column(name = "transaction_time")
    @TableField("transaction_time")
    private LocalDateTime transactionTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @Column(name = "deleted")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 备注 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private String remark;

    /**
     * 操作者ID (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private Long operatorId;

    /**
     * 用户
     */
    @Transient
    @TableField(exist = false)
    private User user;

    /**
     * 游戏
     */
    @Transient
    @TableField(exist = false)
    private GameHistory game;

    /**
     * 获取交易类型枚举
     */
    public TransactionType getTypeEnum() {
        return TransactionType.valueOf(transactionType);
    }

    /**
     * 设置交易类型枚举
     */
    public void setTypeEnum(TransactionType type) {
        this.transactionType = type.name();
        this.type = type.name();
    }

    /**
     * 设置交易类型 (兼容旧代码)
     */
    public ChipTransaction setType(String type) {
        this.type = type;
        this.transactionType = type;
        return this;
    }

    /**
     * 获取交易类型 (兼容旧代码)
     */
    public String getType() {
        return this.transactionType;
    }

    /**
     * 设置备注 (兼容旧代码)
     */
    public ChipTransaction setReason(String reason) {
        this.remark = reason;
        return this;
    }

    /**
     * 获取备注 (兼容旧代码)
     */
    public String getReason() {
        return this.remark;
    }

    /**
     * 设置操作者ID (兼容旧代码)
     */
    public ChipTransaction setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    /**
     * 获取操作者ID (兼容旧代码)
     */
    public Long getOperatorId() {
        return this.operatorId;
    }
} 