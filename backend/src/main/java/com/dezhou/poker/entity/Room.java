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
@TableName("room")
@Entity
@Table(name = "room")
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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间名称
     */
    @Column(name = "name")
    @TableField("name")
    private String name;

    /**
     * 房间密码
     */
    @Column(name = "password")
    @TableField("password")
    private String password;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id")
    @TableField("creator_id")
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
     * 当前玩家数
     */
    @Column(name = "current_players")
    @TableField("current_players")
    private Integer currentPlayers;

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