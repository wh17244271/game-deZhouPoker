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
 * 玩家游戏历史实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("player_game_history")
@Entity
@Table(name = "player_game_history")
public class PlayerGameHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 复合主键
     */
    @EmbeddedId
    @TableField(exist = false)
    private PlayerGameHistoryId id;

    /**
     * 游戏ID
     */
    @Column(name = "game_id", insertable = false, updatable = false)
    @TableField("game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", insertable = false, updatable = false)
    @TableField("user_id")
    private Long userId;

    /**
     * 游戏
     */
    @Transient
    @TableField(exist = false)
    private GameHistory game;

    /**
     * 用户
     */
    @Transient
    @TableField(exist = false)
    private User user;

    /**
     * 初始筹码
     */
    @Column(name = "initial_chips")
    @TableField("initial_chips")
    private BigDecimal initialChips;

    /**
     * 最终筹码
     */
    @Column(name = "final_chips")
    @TableField("final_chips")
    private BigDecimal finalChips;

    /**
     * 手牌
     */
    @Column(name = "hole_cards")
    @TableField("hole_cards")
    private String holeCards;

    /**
     * 最终牌型
     */
    @Column(name = "final_hand_type")
    @TableField("final_hand_type")
    private String finalHandType;

    /**
     * 位置
     */
    @Column(name = "position")
    @TableField("position")
    private Integer position;

    /**
     * 是否获胜
     */
    @Column(name = "is_winner")
    @TableField("is_winner")
    private Boolean isWinner;

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