package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.AllinVote;
import com.dezhou.poker.entity.GameAction;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.AllinVoteService;
import com.dezhou.poker.service.GameActionService;
import com.dezhou.poker.service.GameService;
import com.dezhou.poker.service.PlayerGameHistoryService;
import com.dezhou.poker.service.RoomService;
import com.dezhou.poker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/games")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    // 添加执行计划任务的服务
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 存储游戏计时器信息
    private final ConcurrentHashMap<Long, GameTimerInfo> gameTimers = new ConcurrentHashMap<>();

    @Autowired
    private GameService gameService;
    
    @Autowired
    private PlayerGameHistoryService playerGameHistoryService;
    
    @Autowired
    private GameActionService gameActionService;
    
    @Autowired
    private AllinVoteService allinVoteService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 获取房间当前游戏
     *
     * @param roomId 房间ID
     * @return 当前游戏
     */
    @GetMapping("/room/{roomId}/current")
    public ResponseEntity<?> getCurrentGame(@PathVariable Long roomId) {
        try {
            // 获取房间
            Room room = roomService.getById(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }

            // 检查房间状态
            if (!"PLAYING".equals(room.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间当前没有进行中的游戏"));
            }

            // 获取当前游戏
            GameHistory gameHistory = gameService.getCurrentGame(roomId);
            if (gameHistory == null) {
                // 如果没有找到游戏，但房间状态是PLAYING，则创建一个新游戏
                try {
                    gameHistory = gameService.startNewGame(roomId);
                    System.out.println("自动创建游戏: " + gameHistory.getId());
                } catch (Exception e) {
                    System.err.println("创建游戏失败: " + e.getMessage());
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "房间当前没有进行中的游戏，且无法自动创建游戏"));
                }
            }

            // 设置游戏关联的房间信息
            gameHistory.setRoom(room);

            // 获取游戏玩家
            List<PlayerGameHistory> players = gameService.getGamePlayers(gameHistory.getId());
            
            // 为每个玩家设置用户信息
            for (PlayerGameHistory player : players) {
                if (player.getUser() == null && player.getUserId() != null) {
                    User user = userService.getById(player.getUserId());
                    player.setUser(user);
                }
            }

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("game", gameHistory);
            response.put("players", players);

            return ResponseEntity.ok(new ApiResponse(true, "获取当前游戏成功", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 开始发牌
     *
     * @param gameId 游戏ID
     * @return 发牌结果
     */
    @PostMapping("/{gameId}/deal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> dealCards(@PathVariable Long gameId) {
        try {
            logger.info("开始发牌, 游戏ID: {}", gameId);
            
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                logger.error("游戏不存在, ID: {}", gameId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }
            
            // 发牌
            Map<String, Object> result = gameService.dealCards(gameId);
            logger.info("发牌成功, 游戏ID: {}, 结果: {}", gameId, result);
            
            // 返回结果
            return ResponseEntity.ok(new ApiResponse(true, "发牌成功", result));
        } catch (Exception e) {
            logger.error("发牌失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "发牌失败: " + e.getMessage()));
        }
    }

    /**
     * 获取玩家手牌
     *
     * @param currentUser 当前用户
     * @param gameId      游戏ID
     * @return 玩家手牌
     */
    @GetMapping("/{gameId}/my-cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCards(@AuthenticationPrincipal UserPrincipal currentUser,
                                       @PathVariable Long gameId) {
        try {
            // 获取玩家手牌
            String holeCards = gameService.getPlayerCards(gameId, currentUser.getId());
            if (holeCards == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您不是该游戏的玩家或游戏尚未开始"));
            }

            return ResponseEntity.ok(new ApiResponse(true, "获取手牌成功", holeCards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取公共牌
     *
     * @param gameId 游戏ID
     * @return 公共牌
     */
    @GetMapping("/{gameId}/community-cards")
    public ResponseEntity<?> getCommunityCards(@PathVariable Long gameId) {
        try {
            // 获取当前轮次可见的公共牌
            String visibleCommunityCards = gameService.getVisibleCommunityCards(gameId);
            if (visibleCommunityCards == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }

            return ResponseEntity.ok(new ApiResponse(true, "获取公共牌成功", visibleCommunityCards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 管理房间游戏状态
     * 根据房间人数和游戏状态自动管理游戏
     *
     * @param roomId 房间ID
     * @return 游戏状态
     */
    @PostMapping("/room/{roomId}/manage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> manageRoomGameState(@PathVariable Long roomId) {
        try {
            logger.info("管理房间游戏状态, 房间ID: {}", roomId);
            
            // 调用服务方法
            Map<String, Object> result = gameService.manageRoomGameState(roomId);
            logger.info("房间游戏状态管理结果: {}", result);
            
            return ResponseEntity.ok(new ApiResponse(true, "游戏状态管理成功", result));
        } catch (Exception e) {
            logger.error("管理房间游戏状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "管理游戏状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 处理玩家离开座位
     *
     * @param currentUser 当前用户
     * @param roomId 房间ID
     * @return 处理结果
     */
    @PostMapping("/room/{roomId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> handlePlayerLeave(@AuthenticationPrincipal UserPrincipal currentUser,
                                             @PathVariable Long roomId) {
        try {
            logger.info("处理玩家离开座位, 房间ID: {}, 用户ID: {}", roomId, currentUser.getId());
            
            // 调用服务方法
            Map<String, Object> result = gameService.handlePlayerLeave(roomId, currentUser.getId());
            logger.info("玩家离开座位处理结果: {}", result);
            
            return ResponseEntity.ok(new ApiResponse(true, "处理玩家离开成功", result));
        } catch (Exception e) {
            logger.error("处理玩家离开座位失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "处理玩家离开失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查游戏结束条件
     *
     * @param gameId 游戏ID
     * @return 检查结果
     */
    @PostMapping("/{gameId}/check-end")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkGameEndCondition(@PathVariable Long gameId) {
        try {
            logger.info("检查游戏结束条件, 游戏ID: {}", gameId);
            
            // 调用服务方法
            boolean isEnded = gameService.checkGameEndCondition(gameId);
            logger.info("游戏结束条件检查结果: {}", isEnded);
            
            Map<String, Object> result = new HashMap<>();
            result.put("gameId", gameId);
            result.put("isEnded", isEnded);
            
            if (isEnded) {
                // 获取游戏信息
                GameHistory game = gameService.findById(gameId);
                result.put("game", game);
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "检查游戏结束条件成功", result));
        } catch (Exception e) {
            logger.error("检查游戏结束条件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "检查游戏结束条件失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动发牌
     * 
     * @param roomId 房间ID
     * @return 发牌结果
     */
    @PostMapping("/room/{roomId}/auto-deal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> autoDealCards(@PathVariable Long roomId) {
        try {
            logger.info("自动发牌, 房间ID: {}", roomId);
            
            // 获取当前游戏
            GameHistory currentGame = gameService.getCurrentGame(roomId);
            if (currentGame == null) {
                logger.error("没有进行中的游戏, 房间ID: {}", roomId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "没有进行中的游戏"));
            }
            
            // 发牌
            Map<String, Object> result = gameService.dealCards(currentGame.getId());
            logger.info("自动发牌成功, 游戏ID: {}, 结果: {}", currentGame.getId(), result);
            
            return ResponseEntity.ok(new ApiResponse(true, "自动发牌成功", result));
        } catch (Exception e) {
            logger.error("自动发牌失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "自动发牌失败: " + e.getMessage()));
        }
    }

    /**
     * 设置庄家位置
     *
     * @param gameId 游戏ID
     * @return 操作结果
     */
    @PostMapping("/{gameId}/set-dealer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setDealerPosition(@PathVariable Long gameId) {
        try {
            logger.info("设置庄家位置, 游戏ID: {}", gameId);
            
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                logger.error("游戏不存在, ID: {}", gameId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }
            
            // 获取游戏玩家
            List<PlayerGameHistory> players = gameService.getGamePlayers(gameId);
            if (players.isEmpty()) {
                logger.error("游戏没有玩家, ID: {}", gameId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏没有玩家"));
            }
            
            // 随机选择庄家位置
            Random random = new Random();
            int dealerIndex = random.nextInt(players.size());
            PlayerGameHistory dealer = players.get(dealerIndex);
            
            // 获取房间玩家信息，以获取座位号
            List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(game.getRoomId());
            Map<Long, Integer> userSeatMap = new HashMap<>();
            for (RoomPlayer rp : roomPlayers) {
                userSeatMap.put(rp.getUserId(), rp.getSeatNumber());
            }
            
            // 找出庄家的座位号
            Integer dealerSeat = userSeatMap.get(dealer.getUserId());
            if (dealerSeat == null) {
                logger.error("无法确定庄家座位, 用户ID: {}", dealer.getUserId());
                return ResponseEntity.badRequest().body(new ApiResponse(false, "无法确定庄家座位"));
            }
            
            // 计算小盲注和大盲注的位置
            List<Integer> occupiedSeats = roomPlayers.stream()
                    .map(RoomPlayer::getSeatNumber)
                    .sorted()
                    .collect(Collectors.toList());
            
            int dealerSeatIndex = occupiedSeats.indexOf(dealerSeat);
            int smallBlindIndex = (dealerSeatIndex + 1) % occupiedSeats.size();
            int bigBlindIndex = (dealerSeatIndex + 2) % occupiedSeats.size();
            
            int smallBlindSeat = occupiedSeats.get(smallBlindIndex);
            int bigBlindSeat = occupiedSeats.get(bigBlindIndex);
            
            // 更新游戏中的庄家位置信息
            game.setDealerPosition(dealerSeat);
            game.setSmallBlindPosition(smallBlindSeat);
            game.setBigBlindPosition(bigBlindSeat);
            gameService.updateById(game);
            
            // 设置当前回合的玩家（从小盲注开始）
            Long smallBlindUserId = null;
            for (RoomPlayer rp : roomPlayers) {
                if (rp.getSeatNumber() == smallBlindSeat) {
                    smallBlindUserId = rp.getUserId();
                    break;
                }
            }
            
            if (smallBlindUserId != null) {
                // 设置当前回合玩家 - 需要修改GameHistory类以支持此方法
                // game.setCurrentTurn(smallBlindUserId);
                // 替代方案：使用自定义字段存储当前回合玩家ID
                game.setCurrentPlayerId(smallBlindUserId);
                gameService.updateById(game);
                
                // 启动计时器
                startRoundTimer(gameId, smallBlindUserId, game.getRoomId());
            }
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("gameId", gameId);
            result.put("dealerSeat", dealerSeat);
            result.put("smallBlindSeat", smallBlindSeat);
            result.put("bigBlindSeat", bigBlindSeat);
            result.put("currentTurn", smallBlindUserId);
            
            return ResponseEntity.ok(new ApiResponse(true, "设置庄家位置成功", result));
        } catch (Exception e) {
            logger.error("设置庄家位置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "设置庄家位置失败: " + e.getMessage()));
        }
    }

    /**
     * 设置游戏计时器
     *
     * @param gameId 游戏ID
     * @param timePerRound 每回合的时间（秒）
     * @return 操作结果
     */
    @PostMapping("/{gameId}/set-timer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setGameTimer(@PathVariable Long gameId, 
                                          @RequestParam(defaultValue = "30") int timePerRound) {
        try {
            logger.info("设置游戏计时器, 游戏ID: {}, 每回合时间: {}秒", gameId, timePerRound);
            
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                logger.error("游戏不存在, ID: {}", gameId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }
            
            // 更新游戏计时器信息
            GameTimerInfo timerInfo = gameTimers.getOrDefault(gameId, new GameTimerInfo(gameId));
            timerInfo.setTimePerRound(timePerRound);
            gameTimers.put(gameId, timerInfo);
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("gameId", gameId);
            result.put("timePerRound", timePerRound);
            
            return ResponseEntity.ok(new ApiResponse(true, "设置游戏计时器成功", result));
        } catch (Exception e) {
            logger.error("设置游戏计时器失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "设置游戏计时器失败: " + e.getMessage()));
        }
    }

    /**
     * 启动回合计时器
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    private void startRoundTimer(Long gameId, Long userId, Long roomId) {
        GameTimerInfo timerInfo = gameTimers.getOrDefault(gameId, new GameTimerInfo(gameId));
        
        // 取消之前的计时器（如果有）
        if (timerInfo.getTimerTask() != null) {
            timerInfo.getTimerTask().cancel(true);
        }
        
        // 设置当前玩家
        timerInfo.setCurrentPlayerId(userId);
        
        // 重置剩余时间
        timerInfo.setTimeLeft(timerInfo.getTimePerRound());
        
        // 创建新的计时器任务
        Runnable timerTask = () -> {
            try {
                // 减少剩余时间
                int timeLeft = timerInfo.getTimeLeft() - 1;
                timerInfo.setTimeLeft(timeLeft);
                
                // 发送时间更新消息
                Map<String, Object> timeUpdate = new HashMap<>();
                timeUpdate.put("type", "ROUND_TIME_UPDATE");
                timeUpdate.put("gameId", gameId);
                timeUpdate.put("userId", userId);
                
                Map<String, Object> timeData = new HashMap<>();
                timeData.put("timeLeft", timeLeft);
                timeData.put("totalTime", timerInfo.getTimePerRound());
                
                timeUpdate.put("data", timeData);
                
                messagingTemplate.convertAndSend("/topic/room/" + roomId, timeUpdate);
                
                // 时间到，自动处理
                if (timeLeft <= 0) {
                    handleTimeOut(gameId, userId, roomId);
                }
            } catch (Exception e) {
                logger.error("计时器任务执行错误", e);
            }
        };
        
        // 调度计时器任务
        timerInfo.setTimerTask(scheduler.scheduleAtFixedRate(timerTask, 0, 1, TimeUnit.SECONDS));
        
        // 更新计时器信息
        gameTimers.put(gameId, timerInfo);
    }

    /**
     * 处理超时
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    private void handleTimeOut(Long gameId, Long userId, Long roomId) {
        try {
            logger.info("玩家回合超时, 游戏ID: {}, 用户ID: {}", gameId, userId);
            
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null || !game.getStatusEnum().equals(GameHistory.GameStatus.IN_PROGRESS)) {
                return;
            }
            
            // 判断是否可以过牌
            boolean canCheck = canPlayerCheck(gameId, userId);
            
            if (canCheck) {
                // 如果可以过牌，自动过牌
                recordAutomaticAction(gameId, userId, GameAction.ActionType.CHECK, BigDecimal.ZERO);
                
                // 发送消息
                sendGameActionMessage(roomId, gameId, userId, "CHECK", "过牌（自动）");
            } else {
                // 如果不能过牌，自动弃牌
                recordAutomaticAction(gameId, userId, GameAction.ActionType.FOLD, BigDecimal.ZERO);
                
                // 发送消息
                sendGameActionMessage(roomId, gameId, userId, "FOLD", "弃牌（自动）");
            }
            
            // 更新游戏计时器信息
            GameTimerInfo timerInfo = gameTimers.get(gameId);
            if (timerInfo != null && timerInfo.getTimerTask() != null) {
                timerInfo.getTimerTask().cancel(true);
                timerInfo.setTimerTask(null);
            }
            
            // 获取下一个玩家
            Long nextPlayerId = getNextPlayerInTurn(gameId, userId);
            if (nextPlayerId != null) {
                // 更新当前回合玩家
                // game.setCurrentTurn(nextPlayerId);
                // 替代方案：使用自定义字段存储当前回合玩家ID
                game.setCurrentPlayerId(nextPlayerId);
                gameService.updateById(game);
                
                // 启动下一个玩家的计时器
                startRoundTimer(gameId, nextPlayerId, roomId);
            }
        } catch (Exception e) {
            logger.error("处理超时错误", e);
        }
    }

    /**
     * 判断玩家是否可以过牌
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 是否可以过牌
     */
    private boolean canPlayerCheck(Long gameId, Long userId) {
        try {
            // 获取当前轮次最高下注
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                return false;
            }
            
            String currentRound = game.getCurrentRound() != null 
                ? GameAction.GameRound.values()[game.getCurrentRound()].name() 
                : GameAction.GameRound.PRE_FLOP.name();
            
            // 获取当前轮次的下注动作
            List<GameAction> betActions = gameService.getGameRoundActions(gameId, GameAction.GameRound.valueOf(currentRound))
                    .stream()
                    .filter(action -> action.getActionTypeEnum() == GameAction.ActionType.BET || 
                                     action.getActionTypeEnum() == GameAction.ActionType.RAISE || 
                                     action.getActionTypeEnum() == GameAction.ActionType.CALL)
                    .collect(Collectors.toList());
            
            if (betActions.isEmpty()) {
                // 没有下注动作，可以过牌
                return true;
            }
            
            // 获取当前玩家在当前轮次的下注总额
            BigDecimal playerBetAmount = BigDecimal.ZERO;
            for (GameAction action : betActions) {
                if (action.getUserId().equals(userId)) {
                    playerBetAmount = playerBetAmount.add(action.getAmount());
                }
            }
            
            // 获取当前轮次的最高下注
            BigDecimal highestBet = betActions.stream()
                    .map(GameAction::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            // 如果玩家的下注等于最高下注，可以过牌
            return playerBetAmount.compareTo(highestBet) >= 0;
        } catch (Exception e) {
            logger.error("判断是否可以过牌错误", e);
            return false;
        }
    }

    /**
     * 记录自动动作
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @param actionType 动作类型
     * @param amount 金额
     */
    private void recordAutomaticAction(Long gameId, Long userId, GameAction.ActionType actionType, BigDecimal amount) {
        try {
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                return;
            }
            
            // 获取当前轮次
            String roundStr = game.getCurrentRound() != null 
                ? GameAction.GameRound.values()[game.getCurrentRound()].name() 
                : GameAction.GameRound.PRE_FLOP.name();
            GameAction.GameRound round = GameAction.GameRound.valueOf(roundStr);
            
            // 记录动作
            gameService.recordGameAction(gameId, userId, actionType, amount, round);
        } catch (Exception e) {
            logger.error("记录自动动作错误", e);
        }
    }

    /**
     * 获取下一个玩家
     *
     * @param gameId 游戏ID
     * @param currentUserId 当前用户ID
     * @return 下一个玩家的用户ID
     */
    private Long getNextPlayerInTurn(Long gameId, Long currentUserId) {
        try {
            // 获取游戏信息
            GameHistory game = gameService.findById(gameId);
            if (game == null) {
                return null;
            }
            
            // 获取游戏玩家
            List<PlayerGameHistory> players = gameService.getGamePlayers(gameId);
            if (players.isEmpty()) {
                return null;
            }
            
            // 过滤掉已弃牌的玩家
            List<PlayerGameHistory> activePlayers = players.stream()
                    .filter(p -> !hasPlayerFolded(gameId, p.getUserId()))
                    .collect(Collectors.toList());
            
            if (activePlayers.size() <= 1) {
                // 只剩一名或没有活跃玩家，游戏结束
                return null;
            }
            
            // 获取房间玩家信息，以获取座位号
            List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(game.getRoomId());
            Map<Long, Integer> userSeatMap = new HashMap<>();
            for (RoomPlayer rp : roomPlayers) {
                userSeatMap.put(rp.getUserId(), rp.getSeatNumber());
            }
            
            // 获取当前玩家的座位号
            Integer currentSeat = userSeatMap.get(currentUserId);
            if (currentSeat == null) {
                // 当前玩家不在座位上，取第一个活跃玩家
                return activePlayers.get(0).getUserId();
            }
            
            // 按座位号排序
            List<PlayerGameHistory> sortedPlayers = activePlayers.stream()
                    .filter(p -> userSeatMap.containsKey(p.getUserId()))
                    .sorted((p1, p2) -> {
                        Integer seat1 = userSeatMap.get(p1.getUserId());
                        Integer seat2 = userSeatMap.get(p2.getUserId());
                        return seat1.compareTo(seat2);
                    })
                    .collect(Collectors.toList());
            
            // 找到当前玩家在排序后列表中的索引
            int currentIndex = -1;
            for (int i = 0; i < sortedPlayers.size(); i++) {
                if (sortedPlayers.get(i).getUserId().equals(currentUserId)) {
                    currentIndex = i;
                    break;
                }
            }
            
            if (currentIndex == -1) {
                // 当前玩家不在活跃列表中，取第一个活跃玩家
                return sortedPlayers.get(0).getUserId();
            }
            
            // 返回下一个玩家
            int nextIndex = (currentIndex + 1) % sortedPlayers.size();
            return sortedPlayers.get(nextIndex).getUserId();
        } catch (Exception e) {
            logger.error("获取下一个玩家错误", e);
            return null;
        }
    }

    /**
     * 判断玩家是否已弃牌
     *
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @return 是否已弃牌
     */
    private boolean hasPlayerFolded(Long gameId, Long userId) {
        try {
            // 获取玩家的所有动作
            List<GameAction> playerActions = gameService.getGameActions(gameId)
                    .stream()
                    .filter(action -> action.getUserId().equals(userId))
                    .collect(Collectors.toList());
            
            // 检查是否有弃牌动作
            return playerActions.stream()
                    .anyMatch(action -> action.getActionTypeEnum() == GameAction.ActionType.FOLD);
        } catch (Exception e) {
            logger.error("判断玩家是否已弃牌错误", e);
            return false;
        }
    }

    /**
     * 发送游戏动作消息
     *
     * @param roomId 房间ID
     * @param gameId 游戏ID
     * @param userId 用户ID
     * @param actionType 动作类型
     * @param actionMessage 动作消息
     */
    private void sendGameActionMessage(Long roomId, Long gameId, Long userId, String actionType, String actionMessage) {
        try {
            // 获取用户信息
            User user = userService.getById(userId);
            String username = user != null ? user.getUsername() : "玩家";
            
            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ACTION");
            message.put("gameId", gameId);
            message.put("userId", userId);
            message.put("username", username);
            message.put("actionType", actionType);
            message.put("content", username + " " + actionMessage);
            
            // 发送消息
            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        } catch (Exception e) {
            logger.error("发送游戏动作消息错误", e);
        }
    }

    /**
     * 游戏计时器信息类
     */
    private static class GameTimerInfo {
        private final Long gameId;
        private int timePerRound = 30; // 默认每回合30秒
        private int timeLeft = 30;
        private Long currentPlayerId;
        private java.util.concurrent.ScheduledFuture<?> timerTask;
        
        public GameTimerInfo(Long gameId) {
            this.gameId = gameId;
        }
        
        public Long getGameId() {
            return gameId;
        }
        
        public int getTimePerRound() {
            return timePerRound;
        }
        
        public void setTimePerRound(int timePerRound) {
            this.timePerRound = timePerRound;
        }
        
        public int getTimeLeft() {
            return timeLeft;
        }
        
        public void setTimeLeft(int timeLeft) {
            this.timeLeft = timeLeft;
        }
        
        public Long getCurrentPlayerId() {
            return currentPlayerId;
        }
        
        public void setCurrentPlayerId(Long currentPlayerId) {
            this.currentPlayerId = currentPlayerId;
        }
        
        public java.util.concurrent.ScheduledFuture<?> getTimerTask() {
            return timerTask;
        }
        
        public void setTimerTask(java.util.concurrent.ScheduledFuture<?> timerTask) {
            this.timerTask = timerTask;
        }
    }
}
