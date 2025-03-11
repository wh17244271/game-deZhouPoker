package com.dezhou.poker.websocket;

import lombok.Data;

/**
 * WebSocket消息
 */
@Data
public class WebSocketMessage {

    /**
     * 消息类型
     */
    public enum MessageType {
        JOIN,       // 加入
        LEAVE,      // 离开
        CHAT,       // 聊天
        GAME,       // 游戏
        ACTION,     // 动作
        ALLIN_VOTE, // All-in投票
        ERROR       // 错误
    }

    private MessageType type;
    private Long senderId;
    private String senderName;
    private String content;
    private Object data;
}
