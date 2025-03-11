package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.mapper.UserMapper;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    @Override
    @Transactional
    public User createUser(String username, String password, BigDecimal initialChips) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCurrentChips(initialChips);
        user.setTotalGames(0);
        user.setWins(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setDeleted(0);
        
        save(user);
        return user;
    }

    @Override
    @Transactional
    public boolean updateChips(Long userId, BigDecimal amount) {
        return baseMapper.updateChips(userId, amount) > 0;
    }

    @Override
    @Transactional
    public boolean updateGameStats(Long userId, boolean isWin) {
        return baseMapper.updateGameStats(userId, isWin ? 1 : 0) > 0;
    }
} 