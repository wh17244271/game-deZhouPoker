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
 * 筹码交易记录实体类
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
     * 交易金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 交易类型 (DEPOSIT, WITHDRAW, GAME_WIN, GAME_LOSS)
     */
    @TableField("type")
    private String type;

    /**
     * 交易原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 操作员ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 关联游戏ID
     */
    @TableField("game_id")
    private Long gameId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 