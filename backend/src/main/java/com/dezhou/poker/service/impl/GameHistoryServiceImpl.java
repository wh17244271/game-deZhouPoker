package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.mapper.GameHistoryMapper;
import com.dezhou.poker.service.GameHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏历史服务实现类
 */
@Service
public class GameHistoryServiceImpl extends ServiceImpl<GameHistoryMapper, GameHistory> implements GameHistoryService {

    @Override
    @Transactional
    public GameHistory createGame(Long roomId, BigDecimal smallBlind, BigDecimal bigBlind) {
        GameHistory gameHistory = new GameHistory()
                .setRoomId(roomId)
                .setSmallBlind(smallBlind)
                .setBigBlind(bigBlind)
                .setStatus("WAITING")
                .setCurrentRound(0)  // 0 for PRE_FLOP
                .setPot(BigDecimal.ZERO)
                .setStartTime(LocalDateTime.now());
        
        save(gameHistory);
        return gameHistory;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long gameId, String status) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            return false;
        }
        
        gameHistory.setStatus(status);
        return updateById(gameHistory);
    }

    @Override
    @Transactional
    public boolean updateRound(Long gameId, String round) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            return false;
        }
        
        // Convert round string to integer
        int roundValue;
        switch (round) {
            case "PRE_FLOP":
                roundValue = 0;
                break;
            case "FLOP":
                roundValue = 1;
                break;
            case "TURN":
                roundValue = 2;
                break;
            case "RIVER":
                roundValue = 3;
                break;
            case "SHOWDOWN":
                roundValue = 4;
                break;
            default:
                roundValue = 0;
        }
        
        gameHistory.setCurrentRound(roundValue);
        return updateById(gameHistory);
    }

    @Override
    @Transactional
    public boolean updatePot(Long gameId, BigDecimal amount) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            return false;
        }
        
        gameHistory.setPot(amount);
        return updateById(gameHistory);
    }

    @Override
    @Transactional
    public boolean updateCommunityCards(Long gameId, String communityCards) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            return false;
        }
        
        gameHistory.setCommunityCards(communityCards);
        return updateById(gameHistory);
    }

    @Override
    @Transactional
    public boolean endGame(Long gameId) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            return false;
        }
        
        gameHistory.setStatus("COMPLETED");
        gameHistory.setEndTime(LocalDateTime.now());
        return updateById(gameHistory);
    }

    @Override
    public GameHistory getCurrentGame(Long roomId) {
        LambdaQueryWrapper<GameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameHistory::getRoomId, roomId)
                .eq(GameHistory::getStatus, "ACTIVE")
                .or()
                .eq(GameHistory::getStatus, "WAITING")
                .orderByDesc(GameHistory::getStartTime)
                .last("LIMIT 1");
        
        return getOne(queryWrapper);
    }

    @Override
    public List<GameHistory> getRoomGames(Long roomId) {
        LambdaQueryWrapper<GameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameHistory::getRoomId, roomId)
                .orderByDesc(GameHistory::getStartTime);
        
        return list(queryWrapper);
    }
} 