package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * All-in投票实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("allin_votes")
@Entity
@Table(name = "allin_votes")
public class AllinVote implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "vote_id", type = IdType.AUTO)
    @Column(name = "vote_id")
    private Long id;

    /**
     * 游戏ID
     */
    @Column(name = "game_id")
    @TableField("game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    /**
     * 投票选项
     */
    @Column(name = "vote_option")
    @TableField("vote_option")
    private Integer voteOption;

    /**
     * 投票时间
     */
    @Column(name = "vote_time")
    @TableField("vote_time")
    private LocalDateTime voteTime;

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
     * 游戏
     */
    @TableField(exist = false)
    @Transient
    private GameHistory game;

    /**
     * 用户
     */
    @TableField(exist = false)
    @Transient
    private User user;
} 