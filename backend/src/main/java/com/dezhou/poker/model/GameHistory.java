package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 游戏历史记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_history")
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time")
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "pot_size", precision = 15, scale = 2)
    private BigDecimal potSize = BigDecimal.ZERO;

    @Column(name = "community_cards")
    private String communityCards;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.IN_PROGRESS;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayerGameHistory> playerHistories = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GameAction> actions = new HashSet<>();

    /**
     * 游戏状态枚举
     */
    public enum GameStatus {
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        CANCELLED       // 已取消
    }
}