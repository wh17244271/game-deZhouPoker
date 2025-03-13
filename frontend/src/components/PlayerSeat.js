import React from 'react';
import './PlayerSeat.css';
import PokerCard from './PokerCard';

const PlayerSeat = ({
  player,
  position,
  isActive,
  isCurrent,
  isDealer,
  isSmallBlind,
  isBigBlind,
  lastAction,
  betAmount,
  currentUser,
  isCurrentUserSeat,
  playerCards
}) => {
  // 确定座位的位置样式
  const getSeatPositionStyle = (position) => {
    const positions = {
      1: { bottom: '10%', left: '40%' },
      2: { bottom: '20%', left: '15%' },
      3: { bottom: '50%', left: '5%' },
      4: { top: '20%', left: '15%' },
      5: { top: '10%', left: '40%' },
      6: { top: '10%', right: '40%' },
      7: { top: '20%', right: '15%' },
      8: { bottom: '50%', right: '5%' },
      9: { bottom: '20%', right: '15%' }
    };
    
    return positions[position] || {};
  };
  
  // 确定玩家的动作标签样式
  const getActionBadgeVariant = (action) => {
    if (!action) return 'secondary';
    
    switch (action) {
      case 'FOLD':
        return 'danger';
      case 'CHECK':
        return 'info';
      case 'CALL':
        return 'primary';
      case 'BET':
      case 'RAISE':
        return 'success';
      case 'ALL_IN':
        return 'warning';
      default:
        return 'secondary';
    }
  };
  
  // 确定玩家的动作标签文本
  const getActionBadgeText = (action) => {
    if (!action) return '';
    
    switch (action) {
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
        return '全押';
      default:
        return action;
    }
  };
  
  // 确定座位的状态样式
  const getSeatStatusClass = () => {
    let className = 'player-seat';
    
    if (player) {
      if (isActive) {
        className += ' active-seat';
      }
      
      if (isCurrent) {
        className += ' current-turn';
      }
      
      if (isCurrentUserSeat) {
        className += ' current-user-seat';
      }
    } else {
      className += ' empty-seat';
    }
    
    return className;
  };
  
  return (
    <div 
      className={getSeatStatusClass()}
      style={getSeatPositionStyle(position)}
    >
      {/* 座位位置标记 */}
      <div className="seat-position-label">{position}</div>
      
      {player ? (
        <>
          {/* 玩家信息 */}
          <div className="player-info">
            <div className="player-name">
              {player.user?.username || '玩家 ' + position}
              {isCurrentUserSeat && <span className="text-warning ml-1">(我)</span>}
            </div>
            <div className="player-chips">{player.currentChips || 0}</div>
          </div>
          
          {/* 玩家角色标记 */}
          <div className="player-roles">
            {isDealer && <span className="badge bg-info role-badge">D</span>}
            {isSmallBlind && <span className="badge bg-warning role-badge">SB</span>}
            {isBigBlind && <span className="badge bg-danger role-badge">BB</span>}
          </div>
          
          {/* 玩家动作 */}
          {lastAction && (
            <div className="player-action">
              <span className={`badge bg-${getActionBadgeVariant(lastAction)}`}>
                {getActionBadgeText(lastAction)}
                {betAmount > 0 && ` ${betAmount}`}
              </span>
            </div>
          )}
          
          {/* 玩家手牌 - 只对当前用户显示 */}
          {isCurrentUserSeat && playerCards && playerCards.length > 0 && (
            <div className="player-hand">
              {playerCards.map((card, index) => (
                <div key={index} className="player-card">
                  <PokerCard card={card} small={true} />
                </div>
              ))}
            </div>
          )}
          
          {/* 其他玩家的牌背 */}
          {!isCurrentUserSeat && player.holeCards && (
            <div className="player-hand">
              <div className="player-card card-back" />
              <div className="player-card card-back" />
            </div>
          )}
        </>
      ) : (
        // 空座位
        <div className="empty-seat-label">空座位</div>
      )}
    </div>
  );
};

export default PlayerSeat; 