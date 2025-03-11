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
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 房间ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间名称
     */
    @TableField("name")
    private String name;

    /**
     * 房间密码
     */
    @TableField("password")
    private String password;

    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 最小玩家数
     */
    @TableField("min_players")
    private Integer minPlayers;

    /**
     * 最大玩家数
     */
    @TableField("max_players")
    private Integer maxPlayers;

    /**
     * 小盲注
     */
    @TableField("small_blind")
    private BigDecimal smallBlind;

    /**
     * 大盲注
     */
    @TableField("big_blind")
    private BigDecimal bigBlind;

    /**
     * 房间状态
     */
    @TableField("status")
    private String status;

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
} 