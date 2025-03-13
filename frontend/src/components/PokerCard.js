import React from 'react';
import './PokerCard.css';

const PokerCard = ({ card, hidden = false, small = false }) => {
  if (!card || hidden) {
    return (
      <div className={`poker-card card-back ${small ? 'small' : ''}`}>
        <div className="card-inner"></div>
      </div>
    );
  }

  // 解析扑克牌
  const parseCard = (cardStr) => {
    // 如果card已经是对象形式（可能从PokerUtils.parseCard获取）
    if (typeof cardStr === 'object' && cardStr.suit && cardStr.rank) {
      const suitColor = (cardStr.suit.code === 'H' || cardStr.suit.code === 'D') ? 'red' : 'black';
      return {
        rank: cardStr.rank.symbol,
        suit: cardStr.suit.symbol,
        suitColor: suitColor
      };
    }
    
    if (!cardStr || typeof cardStr !== 'string' || cardStr.length < 2) {
      return { rank: '?', suit: '?' };
    }
    
    const rank = cardStr.charAt(0).toUpperCase();
    const suitChar = cardStr.charAt(1).toUpperCase();
    
    let suit = '';
    let suitColor = '';
    
    // 设置花色
    switch (suitChar) {
      case 'H': // 红桃
        suit = '♥';
        suitColor = 'red';
        break;
      case 'D': // 方块
        suit = '♦';
        suitColor = 'red';
        break;
      case 'C': // 梅花
        suit = '♣';
        suitColor = 'black';
        break;
      case 'S': // 黑桃
        suit = '♠';
        suitColor = 'black';
        break;
      default:
        suit = suitChar;
        suitColor = 'black';
    }
    
    // 转换扑克牌的等级
    let displayRank = rank;
    switch (rank) {
      case 'T':
        displayRank = '10';
        break;
      case 'J':
        displayRank = 'J';
        break;
      case 'Q':
        displayRank = 'Q';
        break;
      case 'K':
        displayRank = 'K';
        break;
      case 'A':
        displayRank = 'A';
        break;
      default:
        // 保留原值
    }
    
    return { rank: displayRank, suit, suitColor };
  };
  
  const { rank, suit, suitColor } = parseCard(card);
  
  return (
    <div className={`poker-card ${small ? 'small' : ''}`}>
      <div className={`card-inner ${suitColor}`}>
        <div className="card-corner top-left">
          <div className="card-rank">{rank}</div>
          <div className="card-suit">{suit}</div>
        </div>
        <div className="card-center">{suit}</div>
        <div className="card-corner bottom-right">
          <div className="card-rank">{rank}</div>
          <div className="card-suit">{suit}</div>
        </div>
      </div>
    </div>
  );
};

export default PokerCard; 