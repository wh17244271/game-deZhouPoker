package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.AllinVote;

import java.util.List;
import java.util.Map;

/**
 * All-in投票服务接口
 */
public interface AllinVoteService extends IService<AllinVote> {

    /**
     * 记录投票
     *
     * @param gameId     游戏ID
     * @param userId     用户ID
     * @param voteOption 投票选项
     * @return 创建的投票
     */
    AllinVote recordVote(Long gameId, Long userId, Integer voteOption);

    /**
     * 获取游戏投票列表
     *
     * @param gameId 游戏ID
     * @return 投票列表
     */
    List<AllinVote> getGameVotes(Long gameId);

    /**
     * 获取投票结果
     *
     * @param gameId 游戏ID
     * @return 投票结果 (选项 -> 票数)
     */
    Map<Integer, Long> getVoteResults(Long gameId);

    /**
     * 获取最多票的选项
     *
     * @param gameId 游戏ID
     * @return 最多票的选项
     */
    Integer getMostVotedOption(Long gameId);

    /**
     * 检查用户是否已投票
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 是否已投票
     */
    boolean hasVoted(Long gameId, Long userId);

    /**
     * 清除游戏投票
     *
     * @param gameId 游戏ID
     * @return 是否成功
     */
    boolean clearVotes(Long gameId);
} 