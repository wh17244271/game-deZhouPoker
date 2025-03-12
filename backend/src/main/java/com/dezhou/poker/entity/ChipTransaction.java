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
@TableName("chip_transaction")
@Entity
@Table(name = "chip_transaction")
public class ChipTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        WIN,        // 游戏获胜
        LOSE,       // 游戏失败
        DEPOSIT,    // 充值
        WITHDRAW,   // 提现
        TRANSFER    // 转账
    }

    /**
     * 交易ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
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
    @Column(name = "type")
    @TableField("type")
    private String type;

    /**
     * 交易时间
     */
    @Column(name = "created_at")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 备注
     */
    @Column(name = "remark")
    @TableField("remark")
    private String remark;

    /**
     * 操作者ID
     */
    @Column(name = "operator_id")
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 创建时间
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
     * 用户
     */
    @TableField(exist = false)
    @Transient
    private User user;

    /**
     * 游戏
     */
    @TableField(exist = false)
    @Transient
    private GameHistory game;

    /**
     * 获取交易类型枚举
     */
    public TransactionType getTypeEnum() {
        return TransactionType.valueOf(type);
    }

    /**
     * 设置交易类型枚举
     */
    public void setTypeEnum(TransactionType type) {
        this.type = type.name();
    }

    /**
     * 设置备注
     */
    public ChipTransaction setReason(String reason) {
        this.remark = reason;
        return this;
    }

    /**
     * 获取备注
     */
    public String getReason() {
        return this.remark;
    }
} 