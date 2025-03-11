package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
@TableName("room_player")
public class RoomPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩家状态枚举
     */
    public enum PlayerStatus {
        WAITING,    // 等待中
        READY,      // 准备就绪
        PLAYING,    // 游戏中
        FOLDED,     // 已弃牌
        ALL_IN,     // 全押
        OFFLINE     // 离线
    }

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 座位号
     */
    @TableField("seat_number")
    private Integer seatNumber;

    /**
     * 当前筹码
     */
    @TableField("current_chips")
    private BigDecimal currentChips;

    /**
     * 玩家状态
     */
    @TableField("status")
    private String status;

    /**
     * 手牌
     */
    @TableField("hole_cards")
    private String holeCards;

    /**
     * 加入时间
     */
    @TableField("joined_at")
    private LocalDateTime joinedAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户
     */
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
} 