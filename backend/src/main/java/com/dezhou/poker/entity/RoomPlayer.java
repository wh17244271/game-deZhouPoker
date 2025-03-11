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
 * 房间玩家关系实体类
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
     * 房间ID
     */
    @TableId(value = "room_id", type = IdType.INPUT)
    private Long roomId;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.INPUT)
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
} 