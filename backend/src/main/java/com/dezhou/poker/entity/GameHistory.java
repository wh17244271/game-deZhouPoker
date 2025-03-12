package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
@Entity
@Table(name = "game_history")
public class GameHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏状态枚举
     */
    public enum GameStatus {
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        CANCELLED,      // 已取消
        FINISHED        // 已结束 (兼容旧代码)
    }

    /**
     * 游戏ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "game_id", type = IdType.AUTO)
    @Column(name = "game_id")
    private Long id;

    /**
     * 房间ID
     */
    @Column(name = "room_id")
    @TableField("room_id")
    private Long roomId;

    /**
     * 房间
     */
    @Transient
    @TableField(exist = false)
    private Room room;

    /**
     * 游戏状态
     */
    @Column(name = "status")
    @TableField("status")
    private String status;

    /**
     * 小盲注
     */
    @Column(name = "small_blind")
    @TableField("small_blind")
    private BigDecimal smallBlind;

    /**
     * 大盲注
     */
    @Column(name = "big_blind")
    @TableField("big_blind")
    private BigDecimal bigBlind;

    /**
     * 底池
     */
    @Column(name = "pot_size")
    @TableField("pot_size")
    private BigDecimal potSize;

    /**
     * 底池 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private BigDecimal pot;

    /**
     * 当前轮次 (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
     */
    @Column(name = "current_round")
    @TableField("current_round")
    private Integer currentRound;

    /**
     * 公共牌
     */
    @Column(name = "community_cards")
    @TableField("community_cards")
    private String communityCards;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    @TableField("end_time")
    private LocalDateTime endTime;

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
     * 当前玩家ID
     */
    @Column(name = "current_player_id")
    @TableField("current_player_id")
    private Long currentPlayerId;

    /**
     * 庄家位置
     */
    @Column(name = "dealer_position")
    @TableField("dealer_position")
    private Integer dealerPosition;

    /**
     * 小盲注位置
     */
    @Column(name = "small_blind_position")
    @TableField("small_blind_position")
    private Integer smallBlindPosition;

    /**
     * 大盲注位置
     */
    @Column(name = "big_blind_position")
    @TableField("big_blind_position")
    private Integer bigBlindPosition;

    /**
     * 当前下注
     */
    @Column(name = "current_bet")
    @TableField("current_bet")
    private BigDecimal currentBet;

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

    /**
     * 设置底池 (兼容旧代码)
     */
    public GameHistory setPot(BigDecimal pot) {
        this.pot = pot;
        this.potSize = pot;
        return this;
    }

    /**
     * 获取底池 (兼容旧代码)
     */
    public BigDecimal getPot() {
        return this.potSize;
    }
} 