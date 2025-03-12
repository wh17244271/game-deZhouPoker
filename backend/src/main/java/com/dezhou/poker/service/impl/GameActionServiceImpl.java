package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.GameAction;
import com.dezhou.poker.mapper.GameActionMapper;
import com.dezhou.poker.service.GameActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏动作服务实现类
 */
@Service
public class GameActionServiceImpl extends ServiceImpl<GameActionMapper, GameAction> implements GameActionService {

    @Override
    @Transactional
    public GameAction recordAction(Long gameId, Long userId, String actionType, BigDecimal amount, String round) {
        GameAction gameAction = new GameAction()
                .setGameId(gameId)
                .setUserId(userId)
                .setActionType(actionType)
                .setAmount(amount)
                .setRound(round)
                .setActionTime(LocalDateTime.now());
        
        save(gameAction);
        return gameAction;
    }

    @Override
    public List<GameAction> getGameActions(Long gameId) {
        LambdaQueryWrapper<GameAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameAction::getGameId, gameId)
                .orderByAsc(GameAction::getActionTime);
        
        return list(queryWrapper);
    }

    @Override
    public List<GameAction> getUserGameActions(Long gameId, Long userId) {
        LambdaQueryWrapper<GameAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameAction::getGameId, gameId)
                .eq(GameAction::getUserId, userId)
                .orderByAsc(GameAction::getActionTime);
        
        return list(queryWrapper);
    }

    @Override
    public List<GameAction> getRoundActions(Long gameId, String round) {
        LambdaQueryWrapper<GameAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameAction::getGameId, gameId)
                .eq(GameAction::getRound, round)
                .orderByAsc(GameAction::getActionTime);
        
        return list(queryWrapper);
    }

    @Override
    public GameAction getLastAction(Long gameId, Long userId) {
        LambdaQueryWrapper<GameAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameAction::getGameId, gameId)
                .eq(GameAction::getUserId, userId)
                .orderByDesc(GameAction::getActionTime)
                .last("LIMIT 1");
        
        return getOne(queryWrapper);
    }
} 
 