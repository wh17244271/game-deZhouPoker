package com.dezhou.poker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 玩家游戏历史复合主键
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerGameHistoryId implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 用户ID
     */
    private Long userId;
} 