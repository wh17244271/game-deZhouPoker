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
 * 玩家游戏历史记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("player_game_history")
public class PlayerGameHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    @TableId(value = "game_id", type = IdType.INPUT)
    private Long gameId;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /**
     * 座位号
     */
    @TableField("seat_number")
    private Integer seatNumber;

    /**
     * 初始筹码
     */
    @TableField("initial_chips")
    private BigDecimal initialChips;

    /**
     * 最终筹码
     */
    @TableField("final_chips")
    private BigDecimal finalChips;

    /**
     * 是否获胜
     */
    @TableField("is_winner")
    private Boolean isWinner;

    /**
     * 手牌
     */
    @TableField("hole_cards")
    private String holeCards;

    /**
     * 最终牌型
     */
    @TableField("final_hand_type")
    private String finalHandType;

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