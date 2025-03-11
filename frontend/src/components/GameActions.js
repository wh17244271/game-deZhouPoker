import React, { useState } from 'react';
import { Button, ButtonGroup, Form, InputGroup } from 'react-bootstrap';
import WebSocketService from '../services/WebSocketService';

const GameActions = ({ 
  gameId, 
  currentRound, 
  isPlayerTurn, 
  currentBet = 0, 
  playerChips = 0,
  minRaise = 0
}) => {
  const [betAmount, setBetAmount] = useState(minRaise || currentBet * 2 || 10);
  
  // 检查是否可以执行特定动作
  const canCheck = currentBet === 0;
  const canCall = currentBet > 0 && playerChips >= currentBet;
  const canRaise = playerChips > currentBet + minRaise;
  const canBet = currentBet === 0 && playerChips > 0;
  
  // 执行游戏动作
  const performAction = (actionType, amount = null) => {
    if (!isPlayerTurn || !gameId) return;
    
    WebSocketService.sendGameAction(
      gameId,
      actionType,
      amount,
      currentRound
    );
  };
  
  // 处理下注金额变化
  const handleBetAmountChange = (e) => {
    const value = parseInt(e.target.value, 10);
    if (isNaN(value) || value < 0) {
      setBetAmount(0);
    } else if (value > playerChips) {
      setBetAmount(playerChips);
    } else {
      setBetAmount(value);
    }
  };
  
  // 处理加注
  const handleRaise = () => {
    // 确保加注金额至少是当前下注的两倍或最小加注值
    const minRaiseAmount = Math.max(minRaise, currentBet * 2);
    if (betAmount < minRaiseAmount) {
      setBetAmount(minRaiseAmount);
      return;
    }
    
    performAction('RAISE', betAmount);
  };
  
  // 处理下注
  const handleBet = () => {
    if (betAmount <= 0) {
      setBetAmount(minRaise || 10);
      return;
    }
    
    performAction('BET', betAmount);
  };
  
  // 如果不是玩家回合，禁用所有按钮
  if (!isPlayerTurn) {
    return (
      <div className="game-actions">
        <p className="text-center">等待其他玩家操作...</p>
      </div>
    );
  }
  
  return (
    <div className="game-actions">
      <ButtonGroup className="me-2">
        <Button 
          variant="secondary" 
          onClick={() => performAction('FOLD')}
        >
          弃牌
        </Button>
        
        {canCheck ? (
          <Button 
            variant="info" 
            onClick={() => performAction('CHECK')}
          >
            过牌
          </Button>
        ) : canCall ? (
          <Button 
            variant="primary" 
            onClick={() => performAction('CALL', currentBet)}
          >
            跟注 ({currentBet})
          </Button>
        ) : (
          <Button variant="primary" disabled>
            跟注
          </Button>
        )}
      </ButtonGroup>
      
      {canBet ? (
        <InputGroup className="me-2 d-inline-flex" style={{ width: 'auto' }}>
          <Form.Control
            type="number"
            value={betAmount}
            onChange={handleBetAmountChange}
            min={minRaise || 1}
            max={playerChips}
            style={{ width: '100px' }}
          />
          <Button 
            variant="warning" 
            onClick={handleBet}
          >
            下注
          </Button>
        </InputGroup>
      ) : canRaise ? (
        <InputGroup className="me-2 d-inline-flex" style={{ width: 'auto' }}>
          <Form.Control
            type="number"
            value={betAmount}
            onChange={handleBetAmountChange}
            min={Math.max(minRaise, currentBet * 2)}
            max={playerChips}
            style={{ width: '100px' }}
          />
          <Button 
            variant="warning" 
            onClick={handleRaise}
          >
            加注
          </Button>
        </InputGroup>
      ) : null}
      
      <Button 
        variant="danger" 
        onClick={() => performAction('ALL_IN', playerChips)}
        disabled={playerChips <= 0}
      >
        全下 ({playerChips})
      </Button>
    </div>
  );
};

export default GameActions; 