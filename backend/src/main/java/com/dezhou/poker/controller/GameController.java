package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.AllinVote;
import com.dezhou.poker.entity.GameAction;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.AllinVoteService;
import com.dezhou.poker.service.GameActionService;
import com.dezhou.poker.service.GameService;
import com.dezhou.poker.service.PlayerGameHistoryService;
import com.dezhou.poker.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/games")
public class GameController {

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

            // 获取游戏玩家
            List<PlayerGameHistory> players = gameService.getGamePlayers(gameHistory.getId());

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("game", gameHistory);
            response.put("players", players);

            return ResponseEntity.ok(new ApiResponse(true, "获取当前游戏成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 开始发牌
     *
     * @param currentUser 当前用户
     * @param gameId      游戏ID
     * @return 发牌结果
     */
    @PostMapping("/{gameId}/deal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> dealCards(@AuthenticationPrincipal UserPrincipal currentUser,
                                      @PathVariable Long gameId) {
        try {
            // 获取游戏
            GameHistory gameHistory = gameService.getById(gameId);
            if (gameHistory == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在"));
            }

            // 检查游戏状态
            if (!"IN_PROGRESS".equals(gameHistory.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏状态不正确"));
            }

            // 获取房间
            Room room = roomService.getById(gameHistory.getRoomId());
            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }

            // 检查是否是房主
            if (!room.getCreatorId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "只有房主才能开始发牌"));
            }

            // 开始发牌
            Map<String, Object> dealResult = gameService.dealCards(gameId);

            return ResponseEntity.ok(new ApiResponse(true, "发牌成功", dealResult));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
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
            // 获取公共牌
            String communityCards = gameService.getCommunityCards(gameId);
            if (communityCards == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏不存在或尚未发公共牌"));
            }

            return ResponseEntity.ok(new ApiResponse(true, "获取公共牌成功", communityCards));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
