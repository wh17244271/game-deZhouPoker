import React from 'react';
import PokerUtils from '../utils/PokerUtils';
import '../styles/Card.css';

/**
 * 扑克牌组件
 * @param {Object} props - 组件属性
 * @param {Object|string} props.card - 牌对象或牌字符串
 * @param {boolean} props.hidden - 是否隐藏牌面
 * @param {string} props.size - 牌的尺寸 (small, normal, large)
 * @returns {JSX.Element} 扑克牌组件
 */
const Card = ({ card, hidden = false, size = 'normal' }) => {
  // 如果需要隐藏牌面，显示牌背
  if (hidden) {
    return (
      <div className={`poker-card card-back ${size !== 'normal' ? size : ''}`}>
      </div>
    );
  }

  // 如果没有牌，显示空牌
  if (!card) {
    return (
      <div className={`poker-card card-empty ${size !== 'normal' ? size : ''}`}>
      </div>
    );
  }

  // 解析牌
  const parsedCard = typeof card === 'string' ? PokerUtils.parseCard(card) : card;

  // 如果牌无效，显示无效牌
  if (!parsedCard || !parsedCard.rank || !parsedCard.suit) {
    return (
      <div className={`poker-card card-invalid ${size !== 'normal' ? size : ''}`}>
        无效
      </div>
    );
  }

  // 确定花色颜色
  const colorClass = ['H', 'D'].includes(parsedCard.suit.code) ? 'card-red' : 'card-black';

  return (
    <div className={`poker-card ${size !== 'normal' ? size : ''}`}>
      {/* 左上角 */}
      <div className={`card-corner card-top-left ${colorClass}`}>
        <span className="card-rank">{parsedCard.rank.symbol}</span>
        <span className="card-suit">{parsedCard.suit.symbol}</span>
      </div>

      {/* 中心 */}
      <div className={`card-center ${colorClass}`}>
        {parsedCard.suit.symbol}
      </div>

      {/* 右下角 */}
      <div className={`card-corner card-bottom-right ${colorClass}`}>
        <span className="card-rank">{parsedCard.rank.symbol}</span>
        <span className="card-suit">{parsedCard.suit.symbol}</span>
      </div>
    </div>
  );
};

export default Card; 