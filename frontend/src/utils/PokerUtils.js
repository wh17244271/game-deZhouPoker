/**
 * 扑克牌工具类
 */
class PokerUtils {
  // 花色定义
  static SUITS = {
    'S': { name: '黑桃', symbol: '♠', color: 'black' },
    'H': { name: '红桃', symbol: '♥', color: 'red' },
    'D': { name: '方块', symbol: '♦', color: 'red' },
    'C': { name: '梅花', symbol: '♣', color: 'black' }
  };

  // 牌面值定义
  static RANKS = {
    '2': { name: '2', symbol: '2', value: 2 },
    '3': { name: '3', symbol: '3', value: 3 },
    '4': { name: '4', symbol: '4', value: 4 },
    '5': { name: '5', symbol: '5', value: 5 },
    '6': { name: '6', symbol: '6', value: 6 },
    '7': { name: '7', symbol: '7', value: 7 },
    '8': { name: '8', symbol: '8', value: 8 },
    '9': { name: '9', symbol: '9', value: 9 },
    'T': { name: '10', symbol: '10', value: 10 },
    'J': { name: 'J', symbol: 'J', value: 11 },
    'Q': { name: 'Q', symbol: 'Q', value: 12 },
    'K': { name: 'K', symbol: 'K', value: 13 },
    'A': { name: 'A', symbol: 'A', value: 14 }
  };

  // 牌型定义
  static HAND_TYPES = {
    'HIGH_CARD': { name: '高牌', value: 1 },
    'ONE_PAIR': { name: '一对', value: 2 },
    'TWO_PAIR': { name: '两对', value: 3 },
    'THREE_OF_A_KIND': { name: '三条', value: 4 },
    'STRAIGHT': { name: '顺子', value: 5 },
    'FLUSH': { name: '同花', value: 6 },
    'FULL_HOUSE': { name: '葫芦', value: 7 },
    'FOUR_OF_A_KIND': { name: '四条', value: 8 },
    'STRAIGHT_FLUSH': { name: '同花顺', value: 9 },
    'ROYAL_FLUSH': { name: '皇家同花顺', value: 10 }
  };

  /**
   * 解析单张牌
   * @param {string} cardStr - 牌的字符串表示，例如 "AS" 表示黑桃A
   * @returns {Object|null} - 解析后的牌对象，包含花色和点数信息
   */
  static parseCard(cardStr) {
    if (!cardStr || cardStr.length < 2) {
      return null;
    }

    // 解析牌面值和花色
    const rankChar = cardStr.charAt(0).toUpperCase();
    const suitChar = cardStr.charAt(1).toUpperCase();

    // 验证牌面值和花色是否有效
    if (!this.RANKS[rankChar] || !this.SUITS[suitChar]) {
      console.error(`Invalid card: ${cardStr}`);
      return null;
    }

    // 返回牌对象
    return {
      rank: this.RANKS[rankChar],
      suit: this.SUITS[suitChar],
      code: cardStr.toUpperCase(),
      display: `${this.RANKS[rankChar].symbol}${this.SUITS[suitChar].symbol}`
    };
  }

  /**
   * 解析多张牌
   * @param {string} cardsStr - 多张牌的字符串表示，例如 "AS,KH,QD"
   * @returns {Array} - 解析后的牌对象数组
   */
  static parseCards(cardsStr) {
    if (!cardsStr) {
      return [];
    }

    // 如果是数组，直接返回
    if (Array.isArray(cardsStr)) {
      return cardsStr.map(card => typeof card === 'string' ? this.parseCard(card) : card);
    }

    // 分割字符串并解析每张牌
    return cardsStr.split(',').map(card => this.parseCard(card.trim())).filter(card => card !== null);
  }

  /**
   * 获取牌型名称
   * @param {string} handType - 牌型代码
   * @returns {string} - 牌型名称
   */
  static getHandTypeName(handType) {
    if (!handType || !this.HAND_TYPES[handType]) {
      return '未知牌型';
    }
    return this.HAND_TYPES[handType].name;
  }

  /**
   * 生成随机牌（用于测试）
   * @param {number} count - 需要生成的牌数量
   * @returns {Array} - 随机牌数组
   */
  static getRandomCards(count) {
    const suits = Object.keys(this.SUITS);
    const ranks = Object.keys(this.RANKS);
    const cards = [];
    
    // 生成所有可能的牌
    const allCards = [];
    for (const suit of suits) {
      for (const rank of ranks) {
        allCards.push(rank + suit);
      }
    }
    
    // 随机选择指定数量的牌
    for (let i = 0; i < count; i++) {
      if (allCards.length === 0) break;
      
      const randomIndex = Math.floor(Math.random() * allCards.length);
      const randomCard = allCards.splice(randomIndex, 1)[0];
      cards.push(this.parseCard(randomCard));
    }
    
    return cards;
  }
}

export default PokerUtils; 