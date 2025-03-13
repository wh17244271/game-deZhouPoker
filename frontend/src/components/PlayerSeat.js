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
    let className = 'player-seat player-seat-position-' + position;
    
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
    >
      {/* 座位位置标记 */}
      <div className="seat-position-label">{position}</div>
      
      {/* 庄家按钮 */}
      {isDealer && <div className="dealer-button">D</div>}
      
      {/* 小盲注按钮 */}
      {isSmallBlind && <div className="small-blind-button">SB</div>}
      
      {/* 大盲注按钮 */}
      {isBigBlind && <div className="big-blind-button">BB</div>}
      
      {player ? (
        <>
          {/* 玩家信息 */}
          <div className="player-info">
            <div className="player-name">
              {player.user?.username || player.username || '玩家 ' + position}
              {isCurrentUserSeat && <span className="text-warning ml-1">(我)</span>}
            </div>
            <div className="player-chips">{player.currentChips || 0}</div>
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