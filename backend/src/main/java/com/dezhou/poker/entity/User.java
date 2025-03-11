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
 * 用户实体类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 当前筹码
     */
    @TableField("current_chips")
    private BigDecimal currentChips;

    /**
     * 总游戏局数
     */
    @TableField("total_games")
    private Integer totalGames;

    /**
     * 获胜局数
     */
    @TableField("wins")
    private Integer wins;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    @TableField("last_login")
    private LocalDateTime lastLogin;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 