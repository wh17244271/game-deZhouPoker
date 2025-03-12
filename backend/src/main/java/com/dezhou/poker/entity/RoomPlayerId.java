package com.dezhou.poker.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 房间玩家关系复合主键
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RoomPlayerId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "room_id")
    @TableField("room_id")
    private Long roomId;

    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;
} 