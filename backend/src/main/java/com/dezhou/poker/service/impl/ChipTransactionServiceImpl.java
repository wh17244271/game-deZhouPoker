package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.ChipTransaction;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.mapper.ChipTransactionMapper;
import com.dezhou.poker.service.ChipTransactionService;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 筹码交易服务实现类
 */
@Service
public class ChipTransactionServiceImpl extends ServiceImpl<ChipTransactionMapper, ChipTransaction> implements ChipTransactionService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public ChipTransaction recordTransaction(Long userId, BigDecimal amount, String type, String reason, Long operatorId) {
        // 创建交易记录
        ChipTransaction transaction = new ChipTransaction()
                .setUserId(userId)
                .setAmount(amount)
                .setTransactionType(type)
                .setReason(reason)
                .setOperatorId(operatorId)
                .setCreatedAt(LocalDateTime.now());
        
        save(transaction);
        
        // 更新用户筹码
        User user = userService.getById(userId);
        if (user != null) {
            BigDecimal currentChips = user.getCurrentChips();
            
            // 根据交易类型更新筹码
            if (type.equals(ChipTransaction.TransactionType.WIN.name()) || 
                type.equals(ChipTransaction.TransactionType.DEPOSIT.name())) {
                currentChips = currentChips.add(amount);
            } else if (type.equals(ChipTransaction.TransactionType.LOSE.name()) || 
                       type.equals(ChipTransaction.TransactionType.WITHDRAW.name())) {
                currentChips = currentChips.subtract(amount);
            }
            
            user.setCurrentChips(currentChips);
            userService.updateById(user);
        }
        
        return transaction;
    }

    @Override
    @Transactional
    public ChipTransaction recordGameTransaction(Long userId, BigDecimal amount, String type, String reason, Long operatorId, Long gameId) {
        ChipTransaction transaction = recordTransaction(userId, amount, type, reason, operatorId);
        transaction.setGameId(gameId);
        updateById(transaction);
        
        return transaction;
    }

    @Override
    public List<ChipTransaction> getUserTransactions(Long userId) {
        LambdaQueryWrapper<ChipTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChipTransaction::getUserId, userId)
                .orderByDesc(ChipTransaction::getCreatedAt);
        
        return list(queryWrapper);
    }

    @Override
    public List<ChipTransaction> getGameTransactions(Long gameId) {
        LambdaQueryWrapper<ChipTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChipTransaction::getGameId, gameId)
                .orderByDesc(ChipTransaction::getCreatedAt);
        
        return list(queryWrapper);
    }
} 