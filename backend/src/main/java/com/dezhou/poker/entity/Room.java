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
 * 房间实体类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("rooms")
@Entity
@Table(name = "rooms")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        WAITING,    // 等待中
        PLAYING,    // 游戏中
        FINISHED    // 已结束
    }

    /**
     * 房间ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "room_id", type = IdType.AUTO)
    @Column(name = "room_id")
    private Long id;

    /**
     * 房间名称
     */
    @Column(name = "room_name")
    @TableField("room_name")
    private String name;

    /**
     * 房间密码
     */
    @Column(name = "room_password")
    @TableField("room_password")
    private String password;

    /**
     * 创建者ID
     */
    @Column(name = "owner_id")
    @TableField("owner_id")
    private Long creatorId;

    /**
     * 最小玩家数
     */
    @Column(name = "min_players")
    @TableField("min_players")
    private Integer minPlayers;

    /**
     * 最大玩家数
     */
    @Column(name = "max_players")
    @TableField("max_players")
    private Integer maxPlayers;

    /**
     * 当前玩家数
     */
    @Column(name = "current_players")
    @TableField("current_players")
    private Integer currentPlayers;

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
     * 房间状态
     */
    @Column(name = "status")
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间 (兼容旧代码)
     */
    @Transient
    @TableField(exist = false)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @Column(name = "deleted")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 房主信息（非数据库字段）
     */
    @Transient
    @TableField(exist = false)
    private User owner;

    /**
     * 获取房间状态枚举
     */
    public RoomStatus getStatusEnum() {
        return RoomStatus.valueOf(status);
    }

    /**
     * 设置房间状态枚举
     */
    public void setStatusEnum(RoomStatus status) {
        this.status = status.name();
    }
} 