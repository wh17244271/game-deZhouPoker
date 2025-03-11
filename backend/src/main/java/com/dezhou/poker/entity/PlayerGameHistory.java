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
 * 玩家游戏历史实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("player_game_history")
public class PlayerGameHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 复合主键
     */
    @TableId(type = IdType.NONE)
    private PlayerGameHistoryId id;

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
     * 初始筹码
     */
    @TableField("initial_chips")
    private BigDecimal initialChips;

    /**
     * 最终筹码
     */
    @TableField("final_chips")
    private BigDecimal finalChips;

    /**
     * 最终牌型
     */
    @TableField("final_hand_type")
    private String finalHandType;

    /**
     * 是否获胜
     */
    @TableField("is_winner")
    private Boolean isWinner;

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
     * 设置游戏
     */
    public void setGame(GameHistory game) {
        this.game = game;
        this.gameId = game.getId();
    }

    /**
     * 设置用户
     */
    public void setUser(User user) {
        this.user = user;
        this.userId = user.getId();
    }
} 