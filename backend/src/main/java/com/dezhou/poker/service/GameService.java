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
import java.util.Arrays;

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
            
            // 使用LambdaUpdateWrapper来更新玩家手牌
            playerGameHistoryMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PlayerGameHistory>()
                    .eq(PlayerGameHistory::getGameId, player.getGameId())
                    .eq(PlayerGameHistory::getUserId, player.getUserId())
                    .set(PlayerGameHistory::getHoleCards, holeCards)
            );
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

    /**
     * 处理房间游戏状态
     * 根据房间人数和游戏状态自动管理游戏
     *
     * @param roomId 房间ID
     * @return 更新后的游戏状态
     */
    @Transactional
    public Map<String, Object> manageRoomGameState(Long roomId) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            throw new IllegalStateException("房间不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", roomId);
        result.put("action", "NO_ACTION");

        // 获取当前游戏
        GameHistory currentGame = getCurrentGame(roomId);
        
        // 获取房间玩家
        List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(roomId);
        int activePlayers = (int) roomPlayers.stream()
                .filter(rp -> rp.getStatusEnum() == RoomPlayer.PlayerStatus.PLAYING)
                .count();
        
        result.put("activePlayers", activePlayers);
        result.put("minPlayers", room.getMinPlayers());
        
        // 检查游戏状态
        if (currentGame == null) {
            // 没有进行中的游戏，检查是否可以开始新游戏
            if (activePlayers >= room.getMinPlayers()) {
                // 人数足够，开始新游戏
                GameHistory newGame = startNewGame(roomId);
                result.put("action", "GAME_STARTED");
                result.put("gameId", newGame.getId());
                
                // 自动发牌
                try {
                    Map<String, Object> dealResult = dealCards(newGame.getId());
                    result.put("dealResult", dealResult);
                    result.put("action", "GAME_STARTED_AND_DEALT");
                } catch (Exception e) {
                    result.put("dealError", e.getMessage());
                }
            } else {
                // 人数不足，等待更多玩家
                result.put("action", "WAITING_FOR_PLAYERS");
                result.put("playersNeeded", room.getMinPlayers() - activePlayers);
            }
        } else {
            // 已有进行中的游戏
            result.put("gameId", currentGame.getId());
            result.put("gameStatus", currentGame.getStatusEnum());
            
            // 检查玩家手牌
            List<PlayerGameHistory> gamePlayers = getGamePlayers(currentGame.getId());
            boolean allPlayersHaveCards = gamePlayers.stream()
                    .allMatch(player -> player.getHoleCards() != null && !player.getHoleCards().isEmpty());
            
            if (!allPlayersHaveCards && activePlayers >= room.getMinPlayers()) {
                // 需要发牌
                try {
                    Map<String, Object> dealResult = dealCards(currentGame.getId());
                    result.put("dealResult", dealResult);
                    result.put("action", "CARDS_DEALT");
                } catch (Exception e) {
                    result.put("dealError", e.getMessage());
                }
            } else if (currentGame.getStatusEnum() == GameHistory.GameStatus.FINISHED) {
                // 游戏已结束，检查是否可以开始新游戏
                if (activePlayers >= room.getMinPlayers()) {
                    // 人数足够，开始新游戏
                    GameHistory newGame = startNewGame(roomId);
                    result.put("action", "NEW_GAME_STARTED");
                    result.put("gameId", newGame.getId());
                    
                    // 自动发牌
                    try {
                        Map<String, Object> dealResult = dealCards(newGame.getId());
                        result.put("dealResult", dealResult);
                        result.put("action", "NEW_GAME_STARTED_AND_DEALT");
                    } catch (Exception e) {
                        result.put("dealError", e.getMessage());
                    }
                } else {
                    // 人数不足，等待更多玩家
                    result.put("action", "GAME_FINISHED_WAITING_FOR_PLAYERS");
                    result.put("playersNeeded", room.getMinPlayers() - activePlayers);
                }
            } else {
                // 游戏进行中
                result.put("action", "GAME_IN_PROGRESS");
            }
        }
        
        return result;
    }
    
    /**
     * 处理玩家离开座位
     * 将玩家在当前游戏中的状态设置为自动弃牌
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 处理结果
     */
    @Transactional
    public Map<String, Object> handlePlayerLeave(Long roomId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("roomId", roomId);
        result.put("userId", userId);
        result.put("action", "NO_ACTION");
        
        // 获取当前游戏
        GameHistory currentGame = getCurrentGame(roomId);
        if (currentGame == null) {
            // 没有进行中的游戏，无需处理
            result.put("action", "NO_GAME_IN_PROGRESS");
            return result;
        }
        
        // 检查玩家是否在当前游戏中
        PlayerGameHistory playerGame = playerGameHistoryMapper.selectById(new PlayerGameHistoryId(currentGame.getId(), userId));
        if (playerGame == null) {
            // 玩家不在当前游戏中，无需处理
            result.put("action", "PLAYER_NOT_IN_GAME");
            return result;
        }
        
        // 记录玩家弃牌动作
        try {
            GameAction foldAction = new GameAction();
            foldAction.setGameId(currentGame.getId());
            foldAction.setUserId(userId);
            foldAction.setActionTypeEnum(GameAction.ActionType.FOLD);
            foldAction.setAmount(BigDecimal.ZERO);
            
            // 获取当前轮次
            String roundStr = currentGame.getCurrentRound() != null 
                ? currentGame.getCurrentRound().toString() 
                : "PRE_FLOP";
            GameAction.GameRound round = GameAction.GameRound.valueOf(roundStr);
            foldAction.setRoundEnum(round);
            
            foldAction.setActionTime(LocalDateTime.now());
            gameActionMapper.insert(foldAction);
            
            result.put("action", "PLAYER_AUTO_FOLDED");
        } catch (Exception e) {
            result.put("action", "ERROR_FOLDING");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查游戏是否需要结束
     * 当只剩一名玩家或所有玩家都完成当前轮次动作时结束游戏
     *
     * @param gameId 游戏ID
     * @return 游戏是否结束
     */
    @Transactional
    public boolean checkGameEndCondition(Long gameId) {
        GameHistory game = getById(gameId);
        if (game == null || game.getStatusEnum() == GameHistory.GameStatus.FINISHED) {
            return false;
        }
        
        // 获取游戏玩家
        List<PlayerGameHistory> players = getGamePlayers(gameId);
        
        // 获取未弃牌的玩家
        List<PlayerGameHistory> activePlayers = players.stream()
                .filter(p -> {
                    List<GameAction> playerActions = gameActionMapper.selectList(
                        new QueryWrapper<GameAction>()
                            .eq("game_id", gameId)
                            .eq("user_id", p.getUserId())
                            .eq("action_type", GameAction.ActionType.FOLD.name())
                    );
                    return playerActions.isEmpty(); // 没有弃牌动作的玩家
                })
                .collect(Collectors.toList());
        
        // 检查是否只剩一名玩家
        if (activePlayers.size() <= 1) {
            // 只剩一名玩家，结束游戏
            if (activePlayers.size() == 1) {
                // 设置获胜者
                PlayerGameHistory winner = activePlayers.get(0);
                
                // 计算奖池
                BigDecimal potSize = gameActionMapper.calculatePotSize(gameId);
                
                // 更新获胜者信息
                updateWinner(gameId, winner.getUserId(), potSize, "Last Player Standing");
            }
            
            // 更新游戏状态
            game.setStatusEnum(GameHistory.GameStatus.FINISHED);
            game.setEndTime(LocalDateTime.now());
            updateById(game);
            
            return true;
        }
        
        // 检查当前轮次是否所有玩家都已行动
        String currentRoundStr = game.getCurrentRound() != null 
            ? game.getCurrentRound().toString() 
            : "PRE_FLOP";
        boolean allPlayersActed = true;
        BigDecimal currentBet = BigDecimal.ZERO;
        
        // 获取当前轮次的下注动作
        List<GameAction> betActions = gameActionMapper.selectList(
            new QueryWrapper<GameAction>()
                .eq("game_id", gameId)
                .eq("round", currentRoundStr)
                .in("action_type", Arrays.asList(
                    GameAction.ActionType.BET.name(), 
                    GameAction.ActionType.RAISE.name())
                )
                .orderByDesc("action_time")
        );
        
        if (!betActions.isEmpty()) {
            currentBet = betActions.get(0).getAmount();
        }
        
        // 检查每个活跃玩家的最后一个动作
        for (PlayerGameHistory player : activePlayers) {
            List<GameAction> playerRoundActions = gameActionMapper.selectList(
                new QueryWrapper<GameAction>()
                    .eq("game_id", gameId)
                    .eq("user_id", player.getUserId())
                    .eq("round", currentRoundStr)
                    .orderByDesc("action_time")
            );
            
            if (playerRoundActions.isEmpty()) {
                // 玩家在当前轮次没有行动
                allPlayersActed = false;
                break;
            }
            
            GameAction lastAction = playerRoundActions.get(0);
            if (lastAction.getActionTypeEnum() == GameAction.ActionType.BET 
                || lastAction.getActionTypeEnum() == GameAction.ActionType.RAISE) {
                // 最后一个动作是下注或加注，其他玩家可能需要跟注
                continue;
            }
            
            if (lastAction.getActionTypeEnum() == GameAction.ActionType.CALL) {
                // 最后一个动作是跟注，检查金额是否匹配当前最高下注
                if (lastAction.getAmount().compareTo(currentBet) != 0) {
                    allPlayersActed = false;
                    break;
                }
            }
            
            if (lastAction.getActionTypeEnum() == GameAction.ActionType.CHECK 
                && currentBet.compareTo(BigDecimal.ZERO) > 0) {
                // 如果有人下注，但玩家选择了过牌（应该是不合法的），视为未完成行动
                allPlayersActed = false;
                break;
            }
        }
        
        if (allPlayersActed) {
            // 所有玩家都已行动，进入下一轮或结束游戏
            if ("PRE_FLOP".equals(currentRoundStr)) {
                // 翻牌前阶段完成，进入翻牌阶段
                game.setCurrentRound(GameAction.GameRound.FLOP.ordinal());
                updateById(game);
            } else if ("FLOP".equals(currentRoundStr)) {
                // 翻牌阶段完成，进入转牌阶段
                game.setCurrentRound(GameAction.GameRound.TURN.ordinal());
                updateById(game);
            } else if ("TURN".equals(currentRoundStr)) {
                // 转牌阶段完成，进入河牌阶段
                game.setCurrentRound(GameAction.GameRound.RIVER.ordinal());
                updateById(game);
            } else if ("RIVER".equals(currentRoundStr)) {
                // 河牌阶段完成，进入摊牌阶段并结束游戏
                game.setCurrentRound(GameAction.GameRound.SHOWDOWN.ordinal());
                game.setStatusEnum(GameHistory.GameStatus.FINISHED);
                game.setEndTime(LocalDateTime.now());
                updateById(game);
                
                // 这里应该有摊牌和确定获胜者的逻辑
                // 简化处理：计算奖池并按比例分配给未弃牌的玩家
                BigDecimal potSize = gameActionMapper.calculatePotSize(gameId);
                BigDecimal share = potSize.divide(new BigDecimal(activePlayers.size()), 2, BigDecimal.ROUND_DOWN);
                
                for (PlayerGameHistory player : activePlayers) {
                    updateWinner(gameId, player.getUserId(), share, "Showdown");
                }
                
                return true;
            }
        }
        
        return false;
    }
}
