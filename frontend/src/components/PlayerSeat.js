import React from 'react';
import Card from './Card';
import './PlayerSeat.css';

const PlayerSeat = ({ 
  player, 
  position, 
  isActive = false, 
  isCurrent = false, 
  isDealer = false,
  isSmallBlind = false,
  isBigBlind = false,
  lastAction = null,
  betAmount = 0,
  currentUser
}) => {
  // 计算座位位置样式
  const getPositionStyle = () => {
    const positions = {
      1: { top: '75%', left: '50%' },
      2: { top: '85%', left: '30%' },
      3: { top: '75%', left: '10%' },
      4: { top: '50%', left: '5%' },
      5: { top: '25%', left: '10%' },
      6: { top: '15%', left: '30%' },
      7: { top: '15%', left: '70%' },
      8: { top: '25%', left: '90%' },
      9: { top: '50%', left: '95%' },
    };

    return positions[position] || { top: '50%', left: '50%' };
  };

  // 获取动作标签样式
  const getActionBadgeClass = () => {
    if (!lastAction) return '';
    
    switch (lastAction) {
      case 'FOLD':
        return 'bg-secondary';
      case 'CHECK':
        return 'bg-info';
      case 'CALL':
        return 'bg-primary';
      case 'BET':
      case 'RAISE':
        return 'bg-warning text-dark';
      case 'ALL_IN':
        return 'bg-danger';
      default:
        return 'bg-light text-dark';
    }
  };

  // 获取动作显示文本
  const getActionText = () => {
    if (!lastAction) return '';
    
    switch (lastAction) {
      case 'FOLD':
        return '弃牌';
      case 'CHECK':
        return '过牌';
      case 'CALL':
        return '跟注';
      case 'BET':
        return '下注';
      case 'RAISE':
        return '加注';
      case 'ALL_IN':
        return '全下';
      default:
        return lastAction;
    }
  };

  if (!player) {
    return (
      <div 
        className="player-seat empty" 
        style={getPositionStyle()}
      >
        <div className="seat-number">{position}</div>
        <div className="seat-label">空座位</div>
      </div>
    );
  }

  return (
    <div 
      className={`player-seat ${isActive ? 'active' : ''} ${isCurrent ? 'current' : ''}`}
      style={getPositionStyle()}
    >
      <div className="player-info">
        <div className="player-name">{player.user.username}</div>
        <div className="player-chips">{player.currentChips}</div>
        
        {/* 玩家角色标识 */}
        <div className="player-roles">
          {isDealer && <span className="dealer-button">D</span>}
          {isSmallBlind && <span className="small-blind">SB</span>}
          {isBigBlind && <span className="big-blind">BB</span>}
        </div>
        
        {/* 玩家动作 */}
        {lastAction && (
          <div className={`player-action badge ${getActionBadgeClass()}`}>
            {getActionText()}
            {(lastAction === 'BET' || lastAction === 'RAISE' || lastAction === 'CALL' || lastAction === 'ALL_IN') && 
              betAmount > 0 && ` ${betAmount}`}
          </div>
        )}
      </div>
      
      {/* 玩家手牌 */}
      {player.cards && player.cards.length > 0 ? (
        <div className="player-cards">
          {player.cards.map((card, index) => (
            <Card 
              key={index} 
              card={card} 
              hidden={!player.showCards && player.user.id !== currentUser?.userId}
            />
          ))}
        </div>
      ) : (
        <div className="player-cards">
          <div className="card card-placeholder"></div>
          <div className="card card-placeholder"></div>
        </div>
      )}
    </div>
  );
};

export default PlayerSeat; 