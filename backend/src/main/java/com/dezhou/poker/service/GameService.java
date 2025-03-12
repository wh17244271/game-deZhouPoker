package com.dezhou.poker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.entity.*;

import com.dezhou.poker.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * 游戏服务类
 */
@Service
@Transactional
public class GameService extends ServiceImpl<GameHistoryMapper, GameHistory> {

    @Autowired
    private PlayerGameHistoryMapper playerGameHistoryMapper;

    @Autowired
    private GameActionMapper gameActionMapper;

    @Autowired
    private AllinVoteMapper allinVoteMapper;

    @Autowired
    private ChipTransactionMapper chipTransactionMapper;

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
        Room room = roomService.getById(roomId);
        
        // 检查房间状态
        if (room.getStatusEnum() == Room.RoomStatus.PLAYING) {
            throw new IllegalStateException("房间已经在游戏中");
        }
        
        // 检查房间玩家数量
        if (room.getCurrentPlayers() < room.getMinPlayers()) {
            throw new IllegalStateException("房间玩家数量不足");
        }
        
        // 创建游戏历史记录
        GameHistory gameHistory = new GameHistory();
        gameHistory.setRoomId(room.getId());
        gameHistory.setStartTime(LocalDateTime.now());
        gameHistory.setStatusEnum(GameHistory.GameStatus.IN_PROGRESS);
        save(gameHistory);
        
        // 获取房间玩家
        List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(roomId);
        
        // 为每个玩家创建游戏历史记录
        for (RoomPlayer roomPlayer : roomPlayers) {
            if (roomPlayer.getStatusEnum() == RoomPlayer.PlayerStatus.WAITING) {
                // 更新玩家状态为活跃
                roomPlayer.setStatusEnum(RoomPlayer.PlayerStatus.PLAYING);
                
                // 创建玩家游戏历史记录
                PlayerGameHistory playerGameHistory = new PlayerGameHistory();
                playerGameHistory.setId(new PlayerGameHistoryId(gameHistory.getId(), roomPlayer.getUserId()));
                playerGameHistory.setGameId(gameHistory.getId());
                playerGameHistory.setUserId(roomPlayer.getUserId());
                playerGameHistory.setInitialChips(roomPlayer.getCurrentChips());
                playerGameHistoryMapper.insert(playerGameHistory);
            }
        }
        
        // 更新房间状态
        // room.setStatusEnum(Room.RoomStatus.PLAYING);
    
        roomService.updateStatus(roomId, "PLAYING");

        // roomService.updateById(room);
        
        return gameHistory;
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
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            throw new IllegalStateException("游戏不存在");
        }
        
        // 更新游戏历史记录
        gameHistory.setStatusEnum(GameHistory.GameStatus.FINISHED);
        gameHistory.setEndTime(LocalDateTime.now());
        gameHistory.setCommunityCards(communityCards);
        
        // 计算奖池大小
        BigDecimal potSize = gameActionMapper.calculatePotSize(gameId);
        gameHistory.setPot(potSize);
        
        // 更新房间状态
        Room room = roomService.getById(gameHistory.getRoomId());
        room.setStatusEnum(Room.RoomStatus.WAITING);
        roomService.updateById(room);
        
        updateById(gameHistory);
        
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
        GameHistory gameHistory = getById(gameId);
        User user = userService.getById(userId);
        
        // 创建游戏动作
        GameAction gameAction = new GameAction();
        gameAction.setGameId(gameId);
        gameAction.setUserId(userId);
        gameAction.setActionTypeEnum(actionType);
        gameAction.setAmount(amount);
        gameAction.setRoundEnum(round);
        gameAction.setActionTime(LocalDateTime.now());
        gameActionMapper.insert(gameAction);
        
        return gameAction;
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
        GameHistory gameHistory = getById(gameId);
        User user = userService.getById(userId);
        
        // 检查是否已经投票
        AllinVote existingVote = allinVoteMapper.selectOne(
            new QueryWrapper<AllinVote>()
                .eq("game_id", gameId)
                .eq("user_id", userId)
        );

        if (existingVote != null) {
            // 更新投票
            existingVote.setVoteOption(voteOption);
            existingVote.setVoteTime(LocalDateTime.now());
            allinVoteMapper.updateById(existingVote);
            return existingVote;
        }
        
        // 创建新投票
        AllinVote allinVote = new AllinVote();
        allinVote.setGameId(gameId);
        allinVote.setUserId(userId);
        allinVote.setVoteOption(voteOption);
        allinVote.setVoteTime(LocalDateTime.now());
        allinVote.setCreatedAt(LocalDateTime.now());
        allinVote.setUpdatedAt(LocalDateTime.now());
        allinVote.setDeleted(0);
        allinVoteMapper.insert(allinVote);
        
        return allinVote;
    }

    /**
     * 获取All-in投票结果
     *
     * @param gameId 游戏ID
     * @return 投票结果
     */
    public Map<Integer, Long> getAllinVoteResults(Long gameId) {
        List<Object[]> results = allinVoteMapper.countVotesByGameId(gameId);
        
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
        List<Integer> options = allinVoteMapper.findMostVotedOptionByGameId(gameId);
        return options.isEmpty() ? null : options.get(0);
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
        PlayerGameHistory playerGameHistory = playerGameHistoryMapper.selectById(id);
        if (playerGameHistory == null) {
            throw new IllegalStateException("玩家游戏历史记录不存在");
        }
        
        // 更新玩家游戏历史记录
        playerGameHistory.setFinalChips(finalChips);
        playerGameHistory.setFinalHandType(finalHandType);
        playerGameHistory.setIsWinner(true);
        playerGameHistoryMapper.updateById(playerGameHistory);
        
        // 更新用户统计信息
        userService.updateGameStats(userId, true);
        
        // 更新用户筹码
        userService.updateChips(userId, finalChips);
        
        // 记录筹码变动
        ChipTransaction chipTransaction = new ChipTransaction();
        chipTransaction.setUserId(userId);
        chipTransaction.setAmount(finalChips);
        chipTransaction.setTransactionType("WIN");
        chipTransaction.setGameId(gameId);
        chipTransaction.setCreatedAt(LocalDateTime.now());
        chipTransactionMapper.insert(chipTransaction);
        
        return playerGameHistory;
    }

    /**
     * 根据ID查找游戏
     *
     * @param gameId 游戏ID
     * @return 游戏
     */
    public GameHistory findById(Long gameId) {
        return getById(gameId);
    }

    /**
     * 获取房间当前游戏
     *
     * @param roomId 房间ID
     * @return 当前游戏
     */
    public GameHistory getCurrentGame(Long roomId) {
        return getOne(new QueryWrapper<GameHistory>()
                .eq("room_id", roomId)
                .eq("status", GameHistory.GameStatus.IN_PROGRESS.name())
                .orderByDesc("start_time")
                .last("LIMIT 1"));
    }

    /**
     * 获取游戏玩家列表
     *
     * @param gameId 游戏ID
     * @return 玩家列表
     */
    public List<PlayerGameHistory> getGamePlayers(Long gameId) {
        return playerGameHistoryMapper.selectByGameId(gameId);
    }

    /**
     * 获取游戏动作列表
     *
     * @param gameId 游戏ID
     * @return 动作列表
     */
    public List<GameAction> getGameActions(Long gameId) {
        return gameActionMapper.selectByGameId(gameId);
    }

    /**
     * 获取游戏轮次动作列表
     *
     * @param gameId 游戏ID
     * @param round  轮次
     * @return 动作列表
     */
    public List<GameAction> getGameRoundActions(Long gameId, GameAction.GameRound round) {
        return gameActionMapper.selectByGameIdAndRound(gameId, round.name());
    }

    /**
     * 发牌
     *
     * @param gameId 游戏ID
     * @return 发牌结果
     */
    @Transactional
    public Map<String, Object> dealCards(Long gameId) {
        GameHistory gameHistory = getById(gameId);
        if (gameHistory == null) {
            throw new ResourceNotFoundException("Game", "id", gameId);
        }

        // 获取游戏玩家
        List<PlayerGameHistory> players = getGamePlayers(gameId);
        if (players.isEmpty()) {
            throw new IllegalStateException("游戏没有玩家");
        }

        // 生成牌组
        String[] deck = generateDeck();
        
        // 洗牌
        shuffleDeck(deck);
        
        // 发手牌
        int cardIndex = 0;
        for (PlayerGameHistory player : players) {
            // 每个玩家发两张牌
            String card1 = deck[cardIndex++];
            String card2 = deck[cardIndex++];
            String holeCards = card1 + "," + card2;
            
            // 更新玩家手牌
            player.setHoleCards(holeCards);
            playerGameHistoryMapper.updateById(player);
        }
        
        // 发公共牌（预留5张）
        String[] communityCards = new String[5];
        for (int i = 0; i < 5; i++) {
            communityCards[i] = deck[cardIndex++];
        }
        
        // 更新游戏公共牌
        gameHistory.setCommunityCards(String.join(",", communityCards));
        updateById(gameHistory);
        
        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("gameId", gameId);
        result.put("playerCount", players.size());
        result.put("communityCards", communityCards);
        
        return result;
    }

    /**
     * 获取玩家手牌
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 玩家手牌
     */
    public String getPlayerCards(Long gameId, Long userId) {
        PlayerGameHistory player = playerGameHistoryMapper.selectById(new PlayerGameHistoryId(gameId, userId));
        return player != null ? player.getHoleCards() : null;
    }

    /**
     * 获取公共牌
     *
     * @param gameId 游戏ID
     * @return 公共牌
     */
    public String getCommunityCards(Long gameId) {
        GameHistory gameHistory = getById(gameId);
        return gameHistory != null ? gameHistory.getCommunityCards() : null;
    }

    /**
     * 生成牌组
     *
     * @return 牌组数组
     */
    private String[] generateDeck() {
        String[] suits = {"H", "D", "C", "S"}; // 红桃、方块、梅花、黑桃
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
        String[] deck = new String[52];
        
        int index = 0;
        for (String suit : suits) {
            for (String rank : ranks) {
                deck[index++] = rank + suit;
            }
        }
        
        return deck;
    }

    /**
     * 洗牌
     *
     * @param deck 牌组数组
     */
    private void shuffleDeck(String[] deck) {
        java.util.Random random = new java.util.Random();
        for (int i = deck.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = deck[i];
            deck[i] = deck[j];
            deck[j] = temp;
        }
    }
}
