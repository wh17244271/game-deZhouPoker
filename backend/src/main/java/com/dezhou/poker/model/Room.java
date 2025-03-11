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
 * 房间实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String name;

    @Column(name = "room_password")
    private String password;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.WAITING;

    @Column(name = "min_players", nullable = false)
    private Integer minPlayers = 2;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 9;

    @Column(name = "current_players", nullable = false)
    private Integer currentPlayers = 0;

    @Column(name = "small_blind", precision = 15, scale = 2, nullable = false)
    private BigDecimal smallBlind;

    @Column(name = "big_blind", precision = 15, scale = 2, nullable = false)
    private BigDecimal bigBlind;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomPlayer> players = new HashSet<>();

    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        WAITING,    // 等待中
        PLAYING,    // 游戏中
        FINISHED    // 已结束
    }
}