import React, { useState } from 'react';
import { Button, ButtonGroup, Form, InputGroup } from 'react-bootstrap';
import './GameActions.css';

/**
 * 游戏动作组件
 * 提供玩家可执行的游戏动作：过牌、跟注、下注、加注、弃牌、全押
 */
const GameActions = ({
  gameId,
  currentRound,
  isPlayerTurn,
  currentBet,
  playerChips,
  minRaise,
  timeLeft,
  onAction
}) => {
  const [raiseAmount, setRaiseAmount] = useState(currentBet + minRaise);
  const [betAmount, setBetAmount] = useState(minRaise);
  
  // 根据当前轮次和下注状态，确定玩家可以执行的动作
  const canCheck = currentBet === 0;
  const canCall = currentBet > 0 && playerChips >= currentBet;
  const canBet = currentBet === 0 && playerChips > 0;
  const canRaise = currentBet > 0 && playerChips > currentBet + minRaise;
  const canAllIn = playerChips > 0;
  
  // 处理过牌按钮点击
  const handleCheck = () => {
    if (onAction) {
      onAction('CHECK', 0);
    }
  };
  
  // 处理跟注按钮点击
  const handleCall = () => {
    if (onAction) {
      onAction('CALL', currentBet);
    }
  };
  
  // 处理下注按钮点击
  const handleBet = () => {
    if (onAction) {
      onAction('BET', betAmount);
    }
  };
  
  // 处理加注按钮点击
  const handleRaise = () => {
    if (onAction) {
      onAction('RAISE', raiseAmount);
    }
  };
  
  // 处理弃牌按钮点击
  const handleFold = () => {
    if (onAction) {
      onAction('FOLD', 0);
    }
  };
  
  // 处理全押按钮点击
  const handleAllIn = () => {
    if (onAction) {
      onAction('ALL_IN', playerChips);
    }
  };
  
  // 处理加注金额变化
  const handleRaiseChange = (e) => {
    const value = parseInt(e.target.value, 10);
    if (!isNaN(value) && value >= currentBet + minRaise && value <= playerChips) {
      setRaiseAmount(value);
    }
  };
  
  // 处理下注金额变化
  const handleBetChange = (e) => {
    const value = parseInt(e.target.value, 10);
    if (!isNaN(value) && value >= minRaise && value <= playerChips) {
      setBetAmount(value);
    }
  };
  
  if (!isPlayerTurn || !gameId) {
    return null;
  }
  
  return (
    <div className="game-actions">
      <div className="action-info">
        {timeLeft > 0 && (
          <div className={`time-left ${timeLeft <= 10 ? 'time-critical' : ''}`}>
            剩余时间: {timeLeft}秒
          </div>
        )}
        <div className="bet-info">
          当前下注: {currentBet || 0}
          {playerChips > 0 && `, 您的筹码: ${playerChips}`}
        </div>
      </div>
      
      <div className="action-buttons">
        <ButtonGroup className="action-group">
          {canCheck && (
            <Button variant="info" onClick={handleCheck}>
              过牌
            </Button>
          )}
          
          {canCall && (
            <Button variant="primary" onClick={handleCall}>
              跟注 ({currentBet})
            </Button>
          )}
          
          <Button variant="danger" onClick={handleFold}>
            弃牌
          </Button>
        </ButtonGroup>
        
        <ButtonGroup className="action-group">
          {canBet && (
            <div className="bet-controls">
              <InputGroup>
                <Form.Control
                  type="number"
                  value={betAmount}
                  onChange={handleBetChange}
                  min={minRaise}
                  max={playerChips}
                />
                <Button variant="success" onClick={handleBet}>
                  下注
                </Button>
              </InputGroup>
            </div>
          )}
          
          {canRaise && (
            <div className="raise-controls">
              <InputGroup>
                <Form.Control
                  type="number"
                  value={raiseAmount}
                  onChange={handleRaiseChange}
                  min={currentBet + minRaise}
                  max={playerChips}
                />
                <Button variant="warning" onClick={handleRaise}>
                  加注
                </Button>
              </InputGroup>
            </div>
          )}
          
          {canAllIn && (
            <Button variant="outline-danger" onClick={handleAllIn}>
              全押 ({playerChips})
            </Button>
          )}
        </ButtonGroup>
      </div>
    </div>
  );
};

export default GameActions; 