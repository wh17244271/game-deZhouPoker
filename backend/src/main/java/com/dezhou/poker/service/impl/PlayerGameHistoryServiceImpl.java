package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.entity.PlayerGameHistoryId;
import com.dezhou.poker.mapper.PlayerGameHistoryMapper;
import com.dezhou.poker.service.PlayerGameHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 玩家游戏历史服务实现类
 */
@Service
public class PlayerGameHistoryServiceImpl extends ServiceImpl<PlayerGameHistoryMapper, PlayerGameHistory> implements PlayerGameHistoryService {

    @Override
    @Transactional
    public PlayerGameHistory createPlayerGameHistory(Long gameId, Long userId, Integer seatNumber, BigDecimal initialChips, String holeCards) {
        PlayerGameHistoryId id = new PlayerGameHistoryId(gameId, userId);
        
        PlayerGameHistory playerGameHistory = new PlayerGameHistory();
        playerGameHistory.setId(id);
        playerGameHistory.setGameId(gameId);
        playerGameHistory.setUserId(userId);
        playerGameHistory.setInitialChips(initialChips);
        playerGameHistory.setFinalChips(initialChips);  // 初始时最终筹码等于初始筹码
        playerGameHistory.setIsWinner(false);
        
        save(playerGameHistory);
        return playerGameHistory;
    }

    @Override
    @Transactional
    public boolean updateFinalChips(Long gameId, Long userId, BigDecimal finalChips) {
        LambdaQueryWrapper<PlayerGameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlayerGameHistory::getGameId, gameId)
                .eq(PlayerGameHistory::getUserId, userId);
        
        PlayerGameHistory playerGameHistory = getOne(queryWrapper);
        if (playerGameHistory == null) {
            return false;
        }
        
        playerGameHistory.setFinalChips(finalChips);
        return updateById(playerGameHistory);
    }

    @Override
    @Transactional
    public boolean updateWinner(Long gameId, Long userId, boolean isWinner, String handType) {
        LambdaQueryWrapper<PlayerGameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlayerGameHistory::getGameId, gameId)
                .eq(PlayerGameHistory::getUserId, userId);
        
        PlayerGameHistory playerGameHistory = getOne(queryWrapper);
        if (playerGameHistory == null) {
            return false;
        }
        
        playerGameHistory.setIsWinner(isWinner);
        playerGameHistory.setFinalHandType(handType);
        return updateById(playerGameHistory);
    }

    @Override
    public List<PlayerGameHistory> getGamePlayers(Long gameId) {
        LambdaQueryWrapper<PlayerGameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlayerGameHistory::getGameId, gameId);
        
        return list(queryWrapper);
    }

    @Override
    public List<PlayerGameHistory> getUserGameHistory(Long userId) {
        LambdaQueryWrapper<PlayerGameHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlayerGameHistory::getUserId, userId)
                .orderByDesc(PlayerGameHistory::getGameId);
        
        return list(queryWrapper);
    }

    @Override
    public double calculateWinRate(Long userId) {
        LambdaQueryWrapper<PlayerGameHistory> totalQueryWrapper = new LambdaQueryWrapper<>();
        totalQueryWrapper.eq(PlayerGameHistory::getUserId, userId);
        long totalGames = count(totalQueryWrapper);
        
        if (totalGames == 0) {
            return 0.0;
        }
        
        LambdaQueryWrapper<PlayerGameHistory> winQueryWrapper = new LambdaQueryWrapper<>();
        winQueryWrapper.eq(PlayerGameHistory::getUserId, userId)
                .eq(PlayerGameHistory::getIsWinner, true);
        long winGames = count(winQueryWrapper);
        
        return (double) winGames / totalGames;
    }

    @Override
    public BigDecimal calculateTotalProfit(Long userId) {
        List<PlayerGameHistory> histories = getUserGameHistory(userId);
        BigDecimal totalProfit = BigDecimal.ZERO;
        
        for (PlayerGameHistory history : histories) {
            BigDecimal profit = history.getFinalChips().subtract(history.getInitialChips());
            totalProfit = totalProfit.add(profit);
        }
        
        return totalProfit.setScale(2, RoundingMode.HALF_UP);
    }
} 