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
     * 是否是庄家
     */
    @TableField("is_dealer")
    private Boolean isDealer;

    /**
     * 是否是小盲注
     */
    @TableField("is_small_blind")
    private Boolean isSmallBlind;

    /**
     * 是否是大盲注
     */
    @TableField("is_big_blind")
    private Boolean isBigBlind;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

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