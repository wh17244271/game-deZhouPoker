package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户服务接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User getByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param password 密码
     * @param initialChips 初始筹码
     * @return 创建的用户
     */
    User createUser(String username, String password, BigDecimal initialChips);

    /**
     * 更新用户筹码
     *
     * @param userId 用户ID
     * @param amount 筹码变动量
     * @return 是否成功
     */
    boolean updateChips(Long userId, BigDecimal amount);

    /**
     * 更新用户游戏统计信息
     *
     * @param userId 用户ID
     * @param isWin 是否获胜
     * @return 是否成功
     */
    boolean updateGameStats(Long userId, boolean isWin);
}
