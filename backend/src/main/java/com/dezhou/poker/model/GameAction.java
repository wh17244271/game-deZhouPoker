package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游戏动作记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_actions")
public class GameAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private GameHistory game;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "action_time")
    private LocalDateTime actionTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameRound round;

    /**
     * 动作类型枚举
     */
    public enum ActionType {
        FOLD,   // 弃牌
        CHECK,  // 过牌
        CALL,   // 跟注
        BET,    // 下注
        RAISE,  // 加注
        ALL_IN  // 全下
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
}