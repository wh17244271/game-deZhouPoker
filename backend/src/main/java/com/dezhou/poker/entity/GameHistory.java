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
 * 游戏历史记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("game_history")
public class GameHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 游戏状态 (WAITING, RUNNING, FINISHED)
     */
    @TableField("status")
    private String status;

    /**
     * 小盲注
     */
    @TableField("small_blind")
    private BigDecimal smallBlind;

    /**
     * 大盲注
     */
    @TableField("big_blind")
    private BigDecimal bigBlind;

    /**
     * 底池
     */
    @TableField("pot")
    private BigDecimal pot;

    /**
     * 当前轮次 (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
     */
    @TableField("current_round")
    private String currentRound;

    /**
     * 公共牌
     */
    @TableField("community_cards")
    private String communityCards;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

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