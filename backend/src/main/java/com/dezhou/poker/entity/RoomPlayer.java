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
 * 房间玩家实体类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("room_players")
@Entity
@Table(name = "room_players")
@IdClass(RoomPlayerId.class)
public class RoomPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩家状态枚举
     */
    public enum PlayerStatus {
        ACTIVE,     // 活跃
        FOLDED,     // 弃牌
        ALL_IN,     // 全下
        WAITING,    // 等待
        PLAYING     // 游戏中 (兼容旧代码)
    }

    /**
     * 房间ID - 主键部分
     */
    @Id
    @Column(name = "room_id")
    @TableId(value = "room_id", type = IdType.INPUT)
    private Long roomId;

    /**
     * 用户ID - 主键部分
     */
    @Id
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    /**
     * 座位号
     */
    @Column(name = "seat_number")
    @TableField("seat_number")
    private Integer seatNumber;

    /**
     * 当前筹码
     */
    @Column(name = "current_chips")
    @TableField("current_chips")
    private BigDecimal currentChips;

    /**
     * 状态
     */
    @Column(name = "status")
    @TableField("status")
    private String status;

    /**
     * 最后动作
     */
    @Column(name = "last_action")
    @TableField("last_action")
    private String lastAction;

    /**
     * 最后下注
     */
    @Column(name = "last_bet")
    @TableField("last_bet")
    private BigDecimal lastBet;

    /**
     * 手牌 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private String holeCards;

    /**
     * 加入时间 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private LocalDateTime joinedAt;

    /**
     * 逻辑删除标志
     */
    @Column(name = "deleted")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户
     */
    @Transient
    @TableField(exist = false)
    private User user;

    /**
     * 获取玩家状态枚举
     */
    public PlayerStatus getStatusEnum() {
        return PlayerStatus.valueOf(status);
    }

    /**
     * 设置玩家状态枚举
     */
    public void setStatusEnum(PlayerStatus status) {
        this.status = status.name();
    }
    
    /**
     * 设置加入时间 (兼容旧代码)
     */
    public RoomPlayer setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
        return this;
    }
    
    /**
     * 获取加入时间 (兼容旧代码)
     */
    public LocalDateTime getJoinedAt() {
        return this.joinedAt;
    }
} 