package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 房间玩家关系实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_players")
public class RoomPlayer {

    @EmbeddedId
    private RoomPlayerId id;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "current_chips", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentChips;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.WAITING;

    @Column(name = "last_action")
    private String lastAction;

    @Column(name = "last_bet", precision = 15, scale = 2)
    private BigDecimal lastBet = BigDecimal.ZERO;

    /**
     * 玩家状态枚举
     */
    public enum PlayerStatus {
        ACTIVE,     // 活跃
        FOLDED,     // 弃牌
        ALL_IN,     // 全下
        WAITING     // 等待
    }
}