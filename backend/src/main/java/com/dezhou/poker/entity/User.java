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
 * 用户实体类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("users")
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "user_id", type = IdType.AUTO)
    @Column(name = "user_id")
    private Long id;

    /**
     * 用户名
     */
    @Column(name = "username")
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @Column(name = "password")
    @TableField("password")
    private String password;

    /**
     * 当前筹码
     */
    @Column(name = "current_chips")
    @TableField("current_chips")
    private BigDecimal currentChips;

    /**
     * 总游戏局数
     */
    @Column(name = "total_games")
    @TableField("total_games")
    private Integer totalGames;

    /**
     * 获胜局数
     */
    @Column(name = "wins")
    @TableField("wins")
    private Integer wins;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login")
    @TableField("last_login")
    private LocalDateTime lastLogin;

    /**
     * 逻辑删除标志
     */
    @Column(name = "deleted")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 