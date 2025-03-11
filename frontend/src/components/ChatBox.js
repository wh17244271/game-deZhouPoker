import React, { useState, useEffect, useRef } from 'react';
import { Form, Button, InputGroup } from 'react-bootstrap';
import WebSocketService from '../services/WebSocketService';

const ChatBox = ({ messages, currentUser }) => {
  const [message, setMessage] = useState('');
  const chatBoxRef = useRef(null);

  // 自动滚动到底部
  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  // 发送消息
  const handleSendMessage = (e) => {
    e.preventDefault();
    
    if (!message.trim()) return;
    
    WebSocketService.sendChatMessage(message);
    setMessage('');
  };

  // 获取消息类型样式
  const getMessageClass = (msg) => {
    if (msg.type === 'SYSTEM') {
      return 'system';
    } else if (msg.senderId === currentUser.userId) {
      return 'user current-user';
    } else {
      return 'user';
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-box" ref={chatBoxRef}>
        {messages.length === 0 ? (
          <div className="text-center text-muted my-3">
            暂无消息，开始聊天吧！
          </div>
        ) : (
          messages.map((msg, index) => (
            <div key={index} className={`chat-message ${getMessageClass(msg)}`}>
              {msg.type !== 'SYSTEM' && (
                <strong>{msg.senderName}: </strong>
              )}
              {msg.content}
            </div>
          ))
        )}
      </div>
      
      <Form onSubmit={handleSendMessage} className="chat-input">
        <InputGroup>
          <Form.Control
            type="text"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="输入消息..."
            maxLength={100}
          />
          <Button variant="primary" type="submit">
            发送
          </Button>
        </InputGroup>
      </Form>
    </div>
  );
};

export default ChatBox; 