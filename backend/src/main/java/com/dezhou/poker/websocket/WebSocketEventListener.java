package com.dezhou.poker.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * 处理WebSocket连接事件
     *
     * @param event 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("收到新的WebSocket连接");
    }

    /**
     * 处理WebSocket断开连接事件
     *
     * @param event 断开连接事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (username != null) {
            logger.info("用户断开连接: {}", username);

            // 发送用户离开消息
            if (roomId != null) {
                WebSocketMessage message = new WebSocketMessage();
                message.setType(WebSocketMessage.MessageType.LEAVE);
                message.setSenderId(userId);
                message.setSenderName(username);
                message.setContent(username + " 离开了房间");

                messagingTemplate.convertAndSend("/topic/room." + roomId, message);
            }
        }
    }
}
