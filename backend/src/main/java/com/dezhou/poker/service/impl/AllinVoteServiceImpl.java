package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.AllinVote;
import com.dezhou.poker.mapper.AllinVoteMapper;
import com.dezhou.poker.service.AllinVoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All-in投票服务实现类
 */
@Service
public class AllinVoteServiceImpl extends ServiceImpl<AllinVoteMapper, AllinVote> implements AllinVoteService {

    @Override
    @Transactional
    public AllinVote recordVote(Long gameId, Long userId, Integer voteOption) {
        // 检查是否已投票
        if (hasVoted(gameId, userId)) {
            // 如果已投票，更新投票
            LambdaQueryWrapper<AllinVote> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AllinVote::getGameId, gameId)
                    .eq(AllinVote::getUserId, userId);
            
            AllinVote existingVote = getOne(queryWrapper);
            existingVote.setVoteOption(voteOption);
            existingVote.setVoteTime(LocalDateTime.now());
            updateById(existingVote);
            
            return existingVote;
        } else {
            // 如果未投票，创建新投票
            AllinVote allinVote = new AllinVote()
                    .setGameId(gameId)
                    .setUserId(userId)
                    .setVoteOption(voteOption)
                    .setVoteTime(LocalDateTime.now());
            
            save(allinVote);
            return allinVote;
        }
    }

    @Override
    public List<AllinVote> getGameVotes(Long gameId) {
        LambdaQueryWrapper<AllinVote> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AllinVote::getGameId, gameId)
                .orderByAsc(AllinVote::getVoteTime);
        
        return list(queryWrapper);
    }

    @Override
    public Map<Integer, Long> getVoteResults(Long gameId) {
        List<AllinVote> votes = getGameVotes(gameId);
        
        return votes.stream()
                .collect(Collectors.groupingBy(
                        AllinVote::getVoteOption,
                        Collectors.counting()
                ));
    }

    @Override
    public Integer getMostVotedOption(Long gameId) {
        Map<Integer, Long> voteResults = getVoteResults(gameId);
        
        if (voteResults.isEmpty()) {
            return null;
        }
        
        return voteResults.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public boolean hasVoted(Long gameId, Long userId) {
        LambdaQueryWrapper<AllinVote> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AllinVote::getGameId, gameId)
                .eq(AllinVote::getUserId, userId);
        
        return count(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean clearVotes(Long gameId) {
        LambdaQueryWrapper<AllinVote> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AllinVote::getGameId, gameId);
        
        return remove(queryWrapper);
    }
} 