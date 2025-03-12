package com.dezhou.poker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 玩家游戏历史复合主键
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PlayerGameHistoryId implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    @Column(name = "game_id")
    private Long gameId;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
} 