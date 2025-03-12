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
 * 游戏动作实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("game_actions")
@Entity
@Table(name = "game_actions")
public class GameAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "action_id", type = IdType.AUTO)
    @Column(name = "action_id")
    private Long id;

    /**
     * 游戏ID
     */
    @Column(name = "game_id")
    @TableField("game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    /**
     * 动作类型
     */
    @Column(name = "action_type")
    @TableField("action_type")
    private String actionType;

    /**
     * 金额
     */
    @Column(name = "amount")
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 轮次
     */
    @Column(name = "round")
    @TableField("round")
    private String round;

    /**
     * 动作时间
     */
    @Column(name = "action_time")
    @TableField("action_time")
    private LocalDateTime actionTime;

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
     * 游戏
     */
    @Transient
    @TableField(exist = false)
    private GameHistory game;

    /**
     * 用户
     */
    @Transient
    @TableField(exist = false)
    private User user;

    /**
     * 动作类型枚举
     */
    public enum ActionType {
        FOLD,       // 弃牌
        CHECK,      // 过牌
        CALL,       // 跟注
        BET,        // 下注
        RAISE,      // 加注
        ALL_IN      // 全下
    }

    /**
     * 游戏轮次枚举
     */
    public enum GameRound {
        PRE_FLOP,   // 翻牌前
        FLOP,       // 翻牌
        TURN,       // 转牌
        RIVER       // 河牌
    }

    /**
     * 获取动作类型枚举
     */
    public ActionType getActionTypeEnum() {
        return ActionType.valueOf(actionType);
    }

    /**
     * 设置动作类型枚举
     */
    public void setActionTypeEnum(ActionType actionType) {
        this.actionType = actionType.name();
    }

    /**
     * 获取游戏轮次枚举
     */
    public GameRound getRoundEnum() {
        return GameRound.valueOf(round);
    }

    /**
     * 设置游戏轮次枚举
     */
    public void setRoundEnum(GameRound round) {
        this.round = round.name();
    }
} 