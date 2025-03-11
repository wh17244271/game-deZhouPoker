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
 * 游戏动作实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("game_action")
public class GameAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 动作类型 (FOLD, CHECK, CALL, BET, RAISE, ALL_IN)
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 轮次 (PRE_FLOP, FLOP, TURN, RIVER)
     */
    @TableField("round")
    private String round;

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