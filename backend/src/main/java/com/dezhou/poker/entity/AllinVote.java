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
     * 复合主键
     */
    @TableId(type = IdType.NONE)
    private AllinVoteId id;

    /**
     * 游戏
     */
    @TableField(exist = false)
    private GameHistory game;

    /**
     * 用户
     */
    @TableField(exist = false)
    private User user;

    /**
     * 投票选项
     */
    @TableField("vote_option")
    private Integer voteOption;

    /**
     * 投票时间
     */
    @TableField("vote_time")
    private LocalDateTime voteTime;

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