package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * All-in投票实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("allin_vote")
public class AllinVote implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 投票选项 (1: 一次, 2: 两次, 3: 三次)
     */
    @TableField("vote_option")
    private Integer voteOption;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 