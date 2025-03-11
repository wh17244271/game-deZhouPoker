package com.dezhou.poker.entity;

/**
 * 玩家状态枚举
 */
public enum PlayerStatus {
    /**
     * 等待中
     */
    WAITING,
    
    /**
     * 活跃
     */
    ACTIVE,
    
    /**
     * 已弃牌
     */
    FOLDED,
    
    /**
     * 已离开
     */
    LEFT
} 