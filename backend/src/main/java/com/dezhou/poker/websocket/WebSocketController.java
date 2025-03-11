package com.dezhou.poker.websocket;

import com.dezhou.poker.model.GameAction;
import com.dezhou.poker.model.GameHistory;
import com.dezhou.poker.model.PlayerGameHistory;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * WebSocket控制器
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameService gameService;

    /**
     * 加入房间
     *
     * @param roomId              房间ID
     * @param message             消息
     * @param headerAccessor      消息头访问器
     * @param authentication      认证信息
     * @return 加入消息
     */
    @MessageMapping("/room/{roomId}/join")
    @SendTo("/topic/room.{roomId}")
    public WebSocketMessage joinRoom(@DestinationVariable Long roomId,
                                     @Payload WebSocketMessage message,
                                     SimpMessageHeaderAccessor headerAccessor,
                                     Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 保存用户信息到WebSocket会话
        headerAccessor.getSessionAttributes().put("username", userPrincipal.getUsername());
        headerAccessor.getSessionAttributes().put("userId", userPrincipal.getId());
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        
        // 设置消息信息
        message.setType(WebSocketMessage.MessageType.JOIN);
        message.setSenderId(userPrincipal.getId());
        message.setSenderName(userPrincipal.getUsername());
        message.setContent(userPrincipal.getUsername() + " 加入了房间");
        
        logger.info("用户 {} 加入房间 {}", userPrincipal.getUsername(), roomId);
        
        return message;
    }

    /**
     * 发送聊天消息
     *
     * @param roomId         房间ID
     * @param message        消息
     * @param authentication 认证信息
     * @return 聊天消息
     */
    @MessageMapping("/room/{roomId}/chat")
    @SendTo("/topic/room.{roomId}")
    public WebSocketMessage sendMessage(@DestinationVariable Long roomId,
                                        @Payload WebSocketMessage message,
                                        Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 设置消息信息
        message.setType(WebSocketMessage.MessageType.CHAT);
        message.setSenderId(userPrincipal.getId());
        message.setSenderName(userPrincipal.getUsername());
        
        logger.info("用户 {} 在房间 {} 发送消息: {}", userPrincipal.getUsername(), roomId, message.getContent());
        
        return message;
    }

    /**
     * 游戏动作
     *
     * @param roomId         房间ID
     * @param message        消息
     * @param authentication 认证信息
     * @return 游戏动作消息
     */
    @MessageMapping("/room/{roomId}/action")
    @SendTo("/topic/room.{roomId}")
    public WebSocketMessage gameAction(@DestinationVariable Long roomId,
                                       @Payload WebSocketMessage message,
                                       Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 设置消息信息
        message.setType(WebSocketMessage.MessageType.ACTION);
        message.setSenderId(userPrincipal.getId());
        message.setSenderName(userPrincipal.getUsername());
        
        // 获取动作信息
        Map<String, Object> actionData = (Map<String, Object>) message.getData();
        Long gameId = Long.valueOf(actionData.get("gameId").toString());
        String actionType = actionData.get("actionType").toString();
        String round = actionData.get("round").toString();
        BigDecimal amount = null;
        if (actionData.containsKey("amount")) {
            amount = new BigDecimal(actionData.get("amount").toString());
        }
        
        try {
            // 记录游戏动作
            GameAction.ActionType type = GameAction.ActionType.valueOf(actionType);
            GameAction.GameRound gameRound = GameAction.GameRound.valueOf(round);
            GameAction gameAction = gameService.recordGameAction(gameId, userPrincipal.getId(), type, amount, gameRound);
            
            // 设置消息内容
            message.setContent(userPrincipal.getUsername() + " " + actionType + (amount != null ? " " + amount : ""));
            message.setData(gameAction);
            
            logger.info("用户 {} 在游戏 {} 执行动作: {}", userPrincipal.getUsername(), gameId, actionType);
        } catch (Exception e) {
            message.setType(WebSocketMessage.MessageType.ERROR);
            message.setContent("执行动作失败: " + e.getMessage());
            logger.error("执行动作失败", e);
        }
        
        return message;
    }

    /**
     * All-in投票
     *
     * @param roomId         房间ID
     * @param message        消息
     * @param authentication 认证信息
     * @return All-in投票消息
     */
    @MessageMapping("/room/{roomId}/allin-vote")
    @SendTo("/topic/room.{roomId}")
    public WebSocketMessage allinVote(@DestinationVariable Long roomId,
                                      @Payload WebSocketMessage message,
                                      Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 设置消息信息
        message.setType(WebSocketMessage.MessageType.ALLIN_VOTE);
        message.setSenderId(userPrincipal.getId());
        message.setSenderName(userPrincipal.getUsername());
        
        // 获取投票信息
        Map<String, Object> voteData = (Map<String, Object>) message.getData();
        Long gameId = Long.valueOf(voteData.get("gameId").toString());
        Integer voteOption = Integer.valueOf(voteData.get("voteOption").toString());
        
        try {
            // 记录All-in投票
            gameService.recordAllinVote(gameId, userPrincipal.getId(), voteOption);
            
            // 获取投票结果
            Map<Integer, Long> voteResults = gameService.getAllinVoteResults(gameId);
            Integer mostVotedOption = gameService.getMostVotedAllinOption(gameId);
            
            // 设置消息内容
            message.setContent(userPrincipal.getUsername() + " 投票选择发 " + voteOption + " 次牌");
            message.setData(new Object[]{voteResults, mostVotedOption});
            
            logger.info("用户 {} 在游戏 {} 投票选择发 {} 次牌", userPrincipal.getUsername(), gameId, voteOption);
        } catch (Exception e) {
            message.setType(WebSocketMessage.MessageType.ERROR);
            message.setContent("投票失败: " + e.getMessage());
            logger.error("投票失败", e);
        }
        
        return message;
    }

    /**
     * 游戏状态更新
     *
     * @param roomId         房间ID
     * @param message        消息
     * @param authentication 认证信息
     * @return 游戏状态消息
     */
    @MessageMapping("/room/{roomId}/game-update")
    @SendTo("/topic/room.{roomId}")
    public WebSocketMessage gameUpdate(@DestinationVariable Long roomId,
                                       @Payload WebSocketMessage message,
                                       Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // 设置消息信息
        message.setType(WebSocketMessage.MessageType.GAME);
        message.setSenderId(userPrincipal.getId());
        message.setSenderName(userPrincipal.getUsername());
        
        // 获取游戏信息
        Map<String, Object> gameData = (Map<String, Object>) message.getData();
        String action = gameData.get("action").toString();
        
        try {
            if ("start".equals(action)) {
                // 开始游戏
                GameHistory gameHistory = gameService.startNewGame(roomId);
                List<PlayerGameHistory> players = gameService.getGamePlayers(gameHistory.getId());
                
                message.setContent("游戏开始");
                message.setData(new Object[]{gameHistory, players});
                
                logger.info("用户 {} 在房间 {} 开始游戏", userPrincipal.getUsername(), roomId);
            } else if ("end".equals(action)) {
                // 结束游戏
                Long gameId = Long.valueOf(gameData.get("gameId").toString());
                String communityCards = gameData.get("communityCards").toString();
                
                GameHistory gameHistory = gameService.endGame(gameId, communityCards);
                
                message.setContent("游戏结束");
                message.setData(gameHistory);
                
                logger.info("用户 {} 结束游戏 {}", userPrincipal.getUsername(), gameId);
            } else if ("winner".equals(action)) {
                // 更新获胜者
                Long gameId = Long.valueOf(gameData.get("gameId").toString());
                Long winnerId = Long.valueOf(gameData.get("userId").toString());
                BigDecimal finalChips = new BigDecimal(gameData.get("finalChips").toString());
                String finalHandType = gameData.get("finalHandType").toString();
                
                PlayerGameHistory playerGameHistory = gameService.updateWinner(gameId, winnerId, finalChips, finalHandType);
                
                message.setContent(playerGameHistory.getUser().getUsername() + " 获胜，赢得 " + finalChips + " 筹码");
                message.setData(playerGameHistory);
                
                logger.info("用户 {} 在游戏 {} 获胜", playerGameHistory.getUser().getUsername(), gameId);
            }
        } catch (Exception e) {
            message.setType(WebSocketMessage.MessageType.ERROR);
            message.setContent("游戏操作失败: " + e.getMessage());
            logger.error("游戏操作失败", e);
        }
        
        return message;
    }
}
