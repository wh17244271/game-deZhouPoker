package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 玩家游戏历史记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_game_history")
public class PlayerGameHistory {

    @EmbeddedId
    private PlayerGameHistoryId id;

    @ManyToOne
    @MapsId("gameId")
    @JoinColumn(name = "game_id")
    private GameHistory game;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "initial_chips", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialChips;

    @Column(name = "final_chips", precision = 15, scale = 2)
    private BigDecimal finalChips;

    @Column(name = "hole_cards")
    private String holeCards;

    @Column(name = "final_hand_type")
    private String finalHandType;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_winner")
    private Boolean isWinner = false;
}