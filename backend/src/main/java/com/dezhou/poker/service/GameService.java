package com.dezhou.poker.service;

import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.model.*;
import com.dezhou.poker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏服务类
 */
@Service
public class GameService {

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    @Autowired
    private PlayerGameHistoryRepository playerGameHistoryRepository;

    @Autowired
    private GameActionRepository gameActionRepository;

    @Autowired
    private AllinVoteRepository allinVoteRepository;

    @Autowired
    private ChipTransactionRepository chipTransactionRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    /**
     * 开始新游戏
     *
     * @param roomId 房间ID
     * @return 创建的游戏
     */
    @Transactional
    public GameHistory startNewGame(Long roomId) {
        Room room = roomService.findById(roomId);
        
        // 检查房间状态
        if (room.getStatus() == Room.RoomStatus.PLAYING) {
            throw new IllegalStateException("房间已经在游戏中");
        }
        
        // 检查房间玩家数量
        if (room.getCurrentPlayers() < room.getMinPlayers()) {
            throw new IllegalStateException("房间玩家数量不足");
        }
        
        // 创建游戏历史记录
        GameHistory gameHistory = new GameHistory();
        gameHistory.setRoom(room);
        gameHistory.setStartTime(LocalDateTime.now());
        gameHistory.setStatus(GameHistory.GameStatus.IN_PROGRESS);
        gameHistory = gameHistoryRepository.save(gameHistory);
        
        // 获取房间玩家
        List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(roomId);
        
        // 为每个玩家创建游戏历史记录
        for (RoomPlayer roomPlayer : roomPlayers) {
            if (roomPlayer.getStatus() == RoomPlayer.PlayerStatus.WAITING) {
                // 更新玩家状态为活跃
                roomPlayer.setStatus(RoomPlayer.PlayerStatus.ACTIVE);
                
                // 创建玩家游戏历史记录
                PlayerGameHistoryId id = new PlayerGameHistoryId(gameHistory.getId(), roomPlayer.getUser().getId());
                PlayerGameHistory playerGameHistory = new PlayerGameHistory();
                playerGameHistory.setId(id);
                playerGameHistory.setGame(gameHistory);
                playerGameHistory.setUser(roomPlayer.getUser());
                playerGameHistory.setInitialChips(roomPlayer.getCurrentChips());
                playerGameHistoryRepository.save(playerGameHistory);
            }
        }
        
        // 更新房间状态
        roomService.updateRoomStatus(roomId, Room.RoomStatus.PLAYING);
        
        return gameHistory;
    }

    /**
     * 记录游戏动作
     *
     * @param gameId     游戏ID
     * @param userId     用户ID
     * @param actionType 动作类型
     * @param amount     金额
     * @param round      轮次
     * @return 创建的游戏动作
     */
    @Transactional
    public GameAction recordGameAction(Long gameId, Long userId, GameAction.ActionType actionType, BigDecimal amount, GameAction.GameRound round) {
        GameHistory gameHistory = findById(gameId);
        User user = userService.findById(userId);
        
        // 创建游戏动作
        GameAction gameAction = new GameAction();
        gameAction.setGame(gameHistory);
        gameAction.setUser(user);
        gameAction.setActionType(actionType);
        gameAction.setAmount(amount);
        gameAction.setRound(round);
        gameAction.setActionTime(LocalDateTime.now());
        
        return gameActionRepository.save(gameAction);
    }

    /**
     * 记录All-in投票
     *
     * @param gameId     游戏ID
     * @param userId     用户ID
     * @param voteOption 投票选项
     * @return 创建的All-in投票
     */
    @Transactional
    public AllinVote recordAllinVote(Long gameId, Long userId, Integer voteOption) {
        GameHistory gameHistory = findById(gameId);
        User user = userService.findById(userId);
        
        // 检查是否已经投票
        AllinVote existingVote = allinVoteRepository.findByGameIdAndUserId(gameId, userId);
        if (existingVote != null) {
            // 更新投票
            existingVote.setVoteOption(voteOption);
            existingVote.setVoteTime(LocalDateTime.now());
            return allinVoteRepository.save(existingVote);
        }
        
        // 创建新投票
        AllinVote allinVote = new AllinVote();
        allinVote.setGame(gameHistory);
        allinVote.setUser(user);
        allinVote.setVoteOption(voteOption);
        allinVote.setVoteTime(LocalDateTime.now());
        
        return allinVoteRepository.save(allinVote);
    }

    /**
     * 获取All-in投票结果
     *
     * @param gameId 游戏ID
     * @return 投票结果
     */
    public Map<Integer, Long> getAllinVoteResults(Long gameId) {
        List<Object[]> results = allinVoteRepository.countVotesByGameId(gameId);
        
        // 转换为Map
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    /**
     * 获取最多票数的All-in投票选项
     *
     * @param gameId 游戏ID
     * @return 最多票数的选项
     */
    public Integer getMostVotedAllinOption(Long gameId) {
        List<Integer> options = allinVoteRepository.findMostVotedOptionByGameId(gameId);
        return options.isEmpty() ? null : options.get(0);
    }

    /**
     * 结束游戏
     *
     * @param gameId         游戏ID
     * @param communityCards 公共牌
     * @return 更新后的游戏
     */
    @Transactional
    public GameHistory endGame(Long gameId, String communityCards) {
        GameHistory gameHistory = findById(gameId);
        
        // 更新游戏历史记录
        gameHistory.setStatus(GameHistory.GameStatus.COMPLETED);
        gameHistory.setEndTime(LocalDateTime.now());
        gameHistory.setCommunityCards(communityCards);
        
        // 计算奖池大小
        BigDecimal potSize = gameActionRepository.calculatePotSize(gameId);
        gameHistory.setPotSize(potSize);
        
        // 更新房间状态
        roomService.updateRoomStatus(gameHistory.getRoom().getId(), Room.RoomStatus.WAITING);
        
        return gameHistoryRepository.save(gameHistory);
    }

    /**
     * 更新获胜者信息
     *
     * @param gameId        游戏ID
     * @param userId        用户ID
     * @param finalChips    最终筹码
     * @param finalHandType 最终牌型
     * @return 更新后的玩家游戏历史记录
     */
    @Transactional
    public PlayerGameHistory updateWinner(Long gameId, Long userId, BigDecimal finalChips, String finalHandType) {
        PlayerGameHistoryId id = new PlayerGameHistoryId(gameId, userId);
        PlayerGameHistory playerGameHistory = playerGameHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerGameHistory", "id", id));
        
        // 更新玩家游戏历史记录
        playerGameHistory.setFinalChips(finalChips);
        playerGameHistory.setFinalHandType(finalHandType);
        playerGameHistory.setIsWinner(true);
        
        // 更新用户统计信息
        userService.updateGameStats(userId, true);
        
        // 更新用户筹码
        userService.updateChips(userId, finalChips);
        
        // 记录筹码变动
        ChipTransaction chipTransaction = new ChipTransaction();
        chipTransaction.setUser(playerGameHistory.getUser());
        chipTransaction.setAmount(finalChips);
        chipTransaction.setTransactionType(ChipTransaction.TransactionType.WIN);
        chipTransaction.setGame(playerGameHistory.getGame());
        chipTransaction.setTransactionTime(LocalDateTime.now());
        chipTransactionRepository.save(chipTransaction);
        
        return playerGameHistoryRepository.save(playerGameHistory);
    }

    /**
     * 根据ID查找游戏
     *
     * @param gameId 游戏ID
     * @return 游戏
     */
    public GameHistory findById(Long gameId) {
        return gameHistoryRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("GameHistory", "id", gameId));
    }

    /**
     * 获取房间当前游戏
     *
     * @param roomId 房间ID
     * @return 当前游戏
     */
    public GameHistory getCurrentGame(Long roomId) {
        return gameHistoryRepository.findFirstByRoomIdAndStatusOrderByStartTimeDesc(roomId, GameHistory.GameStatus.IN_PROGRESS);
    }

    /**
     * 获取游戏玩家
     *
     * @param gameId 游戏ID
     * @return 游戏玩家列表
     */
    public List<PlayerGameHistory> getGamePlayers(Long gameId) {
        return playerGameHistoryRepository.findByGameId(gameId);
    }

    /**
     * 获取游戏动作
     *
     * @param gameId 游戏ID
     * @return 游戏动作列表
     */
    public List<GameAction> getGameActions(Long gameId) {
        return gameActionRepository.findByGameId(gameId);
    }

    /**
     * 获取游戏轮次动作
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 游戏动作列表
     */
    public List<GameAction> getGameRoundActions(Long gameId, GameAction.GameRound round) {
        return gameActionRepository.findByGameIdAndRound(gameId, round);
    }
}
