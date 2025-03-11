package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 玩家游戏历史记录复合主键
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PlayerGameHistoryId implements Serializable {

    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "user_id")
    private Long userId;
}