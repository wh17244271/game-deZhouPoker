package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.AllinVote;
import com.dezhou.poker.entity.GameAction;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.AllinVoteService;
import com.dezhou.poker.service.GameActionService;
import com.dezhou.poker.service.GameHistoryService;
import com.dezhou.poker.service.PlayerGameHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameHistoryService gameHistoryService;
    
    @Autowired
    private PlayerGameHistoryService playerGameHistoryService;
    
    @Autowired
    private GameActionService gameActionService;
    
    @Autowired
    private AllinVoteService allinVoteService;

    /**
     * 开始新游戏
     *
     * @param roomId 房间ID
     * @return 创建的游戏
     */
    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startNewGame(@RequestParam Long roomId) {
        try {
            // 检查房间是否已有进行中的游戏
            GameHistory existingGame = gameHistoryService.getCurrentGame(roomId);
            if (existingGame != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间已有进行中的游戏"));
            }
            
            // 获取房间信息以获取盲注值
            // 这里假设从房间服务获取盲注值
            BigDecimal smallBlind = new BigDecimal("10"); // 示例值，实际应从房间获取
            BigDecimal bigBlind = new BigDecimal("20"); // 示例值，实际应从房间获取
            
            GameHistory gameHistory = gameHistoryService.createGame(roomId, smallBlind, bigBlind);
            return ResponseEntity.ok(new ApiResponse(true, "游戏开始成功", gameHistory));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取游戏详情
     *
     * @param gameId 游戏ID
     * @return 游戏详情
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGameDetails(@PathVariable Long gameId) {
        GameHistory gameHistory = gameHistoryService.getById(gameId);
        if (gameHistory == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
        }
        
        List<PlayerGameHistory> players = playerGameHistoryService.getGamePlayers(gameId);
        List<GameAction> actions = gameActionService.getGameActions(gameId);
        
        return ResponseEntity.ok(new ApiResponse(true, "获取游戏详情成功", new Object[]{gameHistory, players, actions}));
    }

    /**
     * 获取房间当前游戏
     *
     * @param roomId 房间ID
     * @return 当前游戏
     */
    @GetMapping("/room/{roomId}/current")
    public ResponseEntity<?> getCurrentGame(@PathVariable Long roomId) {
        GameHistory gameHistory = gameHistoryService.getCurrentGame(roomId);
        if (gameHistory == null) {
            return ResponseEntity.ok(new ApiResponse(false, "房间当前没有进行中的游戏"));
        }
        
        List<PlayerGameHistory> players = playerGameHistoryService.getGamePlayers(gameHistory.getId());
        List<GameAction> actions = gameActionService.getGameActions(gameHistory.getId());
        
        return ResponseEntity.ok(new ApiResponse(true, "获取当前游戏成功", new Object[]{gameHistory, players, actions}));
    }

    /**
     * 记录游戏动作
     *
     * @param currentUser 当前用户
     * @param gameId      游戏ID
     * @param actionType  动作类型
     * @param amount      金额
     * @param round       轮次
     * @return 创建的游戏动作
     */
    @PostMapping("/{gameId}/actions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> recordGameAction(@AuthenticationPrincipal UserPrincipal currentUser,
                                              @PathVariable Long gameId,
                                              @RequestParam String actionType,
                                              @RequestParam(required = false) BigDecimal amount,
                                              @RequestParam String round) {
        try {
            // 验证游戏存在且状态为进行中
            GameHistory gameHistory = gameHistoryService.getById(gameId);
            if (gameHistory == null || !"RUNNING".equals(gameHistory.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在或未在进行中"));
            }
            
            // 验证用户是否参与该游戏
            PlayerGameHistory playerHistory = playerGameHistoryService.getOne(
                new LambdaQueryWrapper<PlayerGameHistory>()
                    .eq(PlayerGameHistory::getGameId, gameId)
                    .eq(PlayerGameHistory::getUserId, currentUser.getId())
            );
            
            if (playerHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您不是该游戏的参与者"));
            }
            
            // 创建游戏动作
            GameAction gameAction = new GameAction()
                .setGameId(gameId)
                .setUserId(currentUser.getId())
                .setActionType(actionType)
                .setAmount(amount)
                .setRound(round)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setDeleted(0);
            
            gameActionService.save(gameAction);
            
            // 如果是下注相关的动作，更新底池
            if (amount != null && (
                "BET".equals(actionType) || 
                "CALL".equals(actionType) || 
                "RAISE".equals(actionType) || 
                "ALL_IN".equals(actionType))) {
                gameHistoryService.updatePot(gameId, amount);
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "记录游戏动作成功", gameAction));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 记录All-in投票
     *
     * @param currentUser 当前用户
     * @param gameId      游戏ID
     * @param voteOption  投票选项
     * @return 创建的All-in投票
     */
    @PostMapping("/{gameId}/allin-votes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> recordAllinVote(@AuthenticationPrincipal UserPrincipal currentUser,
                                             @PathVariable Long gameId,
                                             @RequestParam Integer voteOption) {
        try {
            // 验证游戏存在且状态为进行中
            GameHistory gameHistory = gameHistoryService.getById(gameId);
            if (gameHistory == null || !"RUNNING".equals(gameHistory.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在或未在进行中"));
            }
            
            // 验证用户是否参与该游戏
            PlayerGameHistory playerHistory = playerGameHistoryService.getOne(
                new LambdaQueryWrapper<PlayerGameHistory>()
                    .eq(PlayerGameHistory::getGameId, gameId)
                    .eq(PlayerGameHistory::getUserId, currentUser.getId())
            );
            
            if (playerHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您不是该游戏的参与者"));
            }
            
            // 检查用户是否已投票
            AllinVote existingVote = allinVoteService.getOne(
                new LambdaQueryWrapper<AllinVote>()
                    .eq(AllinVote::getGameId, gameId)
                    .eq(AllinVote::getUserId, currentUser.getId())
            );
            
            if (existingVote != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您已经投过票了"));
            }
            
            // 创建投票
            AllinVote allinVote = new AllinVote()
                .setGameId(gameId)
                .setUserId(currentUser.getId())
                .setVoteOption(voteOption)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setDeleted(0);
            
            allinVoteService.save(allinVote);
            
            // 获取投票结果
            Map<Integer, Long> voteResults = allinVoteService.getVoteResults(gameId);
            
            return ResponseEntity.ok(new ApiResponse(true, "记录All-in投票成功", new Object[]{allinVote, voteResults}));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取All-in投票结果
     *
     * @param gameId 游戏ID
     * @return 投票结果
     */
    @GetMapping("/{gameId}/allin-votes")
    public ResponseEntity<?> getAllinVoteResults(@PathVariable Long gameId) {
        Map<Integer, Long> voteResults = allinVoteService.getVoteResults(gameId);
        Integer mostVotedOption = allinVoteService.getMostVotedOption(gameId);
        
        return ResponseEntity.ok(new ApiResponse(true, "获取All-in投票结果成功", new Object[]{voteResults, mostVotedOption}));
    }

    /**
     * 结束游戏
     *
     * @param gameId         游戏ID
     * @param communityCards 公共牌
     * @return 更新后的游戏
     */
    @PostMapping("/{gameId}/end")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> endGame(@PathVariable Long gameId,
                                     @RequestParam String communityCards) {
        try {
            // 验证游戏存在且状态为进行中
            GameHistory gameHistory = gameHistoryService.getById(gameId);
            if (gameHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }
            
            if (!"RUNNING".equals(gameHistory.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏已结束"));
            }
            
            // 更新公共牌
            gameHistoryService.updateCommunityCards(gameId, communityCards);
            
            // 结束游戏
            gameHistoryService.endGame(gameId);
            
            // 获取更新后的游戏信息
            gameHistory = gameHistoryService.getById(gameId);
            
            return ResponseEntity.ok(new ApiResponse(true, "游戏结束成功", gameHistory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
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
    @PostMapping("/{gameId}/winners")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateWinner(@PathVariable Long gameId,
                                          @RequestParam Long userId,
                                          @RequestParam BigDecimal finalChips,
                                          @RequestParam String finalHandType) {
        try {
            // 验证游戏存在且状态为已结束
            GameHistory gameHistory = gameHistoryService.getById(gameId);
            if (gameHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }
            
            if (!"FINISHED".equals(gameHistory.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏尚未结束"));
            }
            
            // 验证用户是否参与该游戏
            PlayerGameHistory playerHistory = playerGameHistoryService.getOne(
                new LambdaQueryWrapper<PlayerGameHistory>()
                    .eq(PlayerGameHistory::getGameId, gameId)
                    .eq(PlayerGameHistory::getUserId, userId)
            );
            
            if (playerHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "该用户不是游戏参与者"));
            }
            
            // 更新玩家最终筹码和牌型
            playerGameHistoryService.updateFinalChips(gameId, userId, finalChips);
            playerGameHistoryService.updateWinner(gameId, userId, true, finalHandType);
            
            // 获取更新后的玩家游戏历史
            playerHistory = playerGameHistoryService.getOne(
                new LambdaQueryWrapper<PlayerGameHistory>()
                    .eq(PlayerGameHistory::getGameId, gameId)
                    .eq(PlayerGameHistory::getUserId, userId)
            );
            
            return ResponseEntity.ok(new ApiResponse(true, "更新获胜者信息成功", playerHistory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
