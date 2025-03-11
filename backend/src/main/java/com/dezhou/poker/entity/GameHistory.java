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
 * 游戏历史实体类
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
     * 房间
     */
    @TableField(exist = false)
    private Room room;

    /**
     * 游戏状态
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
     * 奖池大小
     */
    @TableField("pot_size")
    private BigDecimal potSize;

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
     * 获取游戏状态枚举
     */
    public GameStatus getStatusEnum() {
        return GameStatus.valueOf(status);
    }

    /**
     * 设置游戏状态枚举
     */
    public void setStatusEnum(GameStatus status) {
        this.status = status.name();
    }

    /**
     * 设置房间
     */
    public void setRoom(Room room) {
        this.room = room;
        this.roomId = room.getId();
    }
} 