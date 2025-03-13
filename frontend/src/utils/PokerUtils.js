/**
 * 扑克牌工具类
 * 提供德州扑克游戏中常用的牌型和功能
 */
export default class PokerUtils {
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
   * 解析扑克牌字符串为数组
   * @param {string} cardsStr - 扑克牌字符串，如 "AH,KD,QC"
   * @returns {Array} - 扑克牌数组
   */
  static parseCards(cardsStr) {
    if (!cardsStr) return [];
    
    // 移除所有空格并按逗号分割
    return cardsStr.replace(/\s/g, '').split(',').filter(card => card.trim());
  }

  /**
   * 获取牌型文本描述
   * @param {string} handType - 牌型代码
   * @returns {string} - 牌型文本描述
   */
  static getHandTypeText(handType) {
    if (!handType) return '未知牌型';
    
    const handTypes = {
      'HIGH_CARD': '高牌',
      'ONE_PAIR': '一对',
      'TWO_PAIR': '两对',
      'THREE_OF_A_KIND': '三条',
      'STRAIGHT': '顺子',
      'FLUSH': '同花',
      'FULL_HOUSE': '葫芦',
      'FOUR_OF_A_KIND': '四条',
      'STRAIGHT_FLUSH': '同花顺',
      'ROYAL_FLUSH': '皇家同花顺'
    };
    
    return handTypes[handType] || handType;
  }

  /**
   * 比较两张牌的大小
   * @param {string} card1 - 第一张牌
   * @param {string} card2 - 第二张牌
   * @returns {number} - 比较结果：1表示card1大，-1表示card2大，0表示相等
   */
  static compareCards(card1, card2) {
    if (!card1 || !card2) return 0;
    
    const ranks = '23456789TJQKA';
    const rank1 = card1.charAt(0);
    const rank2 = card2.charAt(0);
    
    const rankIndex1 = ranks.indexOf(rank1);
    const rankIndex2 = ranks.indexOf(rank2);
    
    if (rankIndex1 > rankIndex2) return 1;
    if (rankIndex1 < rankIndex2) return -1;
    return 0;
  }

  /**
   * 按照牌面大小排序
   * @param {Array} cards - 扑克牌数组
   * @returns {Array} - 排序后的扑克牌数组
   */
  static sortCardsByRank(cards) {
    if (!cards || !Array.isArray(cards)) return [];
    
    const sortedCards = [...cards];
    sortedCards.sort((a, b) => this.compareCards(b, a)); // 降序排列
    
    return sortedCards;
  }

  /**
   * 获取牌的点数
   * @param {string} card - 扑克牌字符串
   * @returns {string} - 扑克牌点数
   */
  static getCardRank(card) {
    if (!card || card.length < 2) return '';
    return card.charAt(0);
  }

  /**
   * 获取牌的花色
   * @param {string} card - 扑克牌字符串
   * @returns {string} - 扑克牌花色
   */
  static getCardSuit(card) {
    if (!card || card.length < 2) return '';
    return card.charAt(1);
  }

  /**
   * 获取花色的Unicode符号
   * @param {string} suit - 花色字符
   * @returns {string} - 花色Unicode符号
   */
  static getSuitSymbol(suit) {
    const symbols = {
      'H': '♥',
      'D': '♦',
      'C': '♣',
      'S': '♠'
    };
    
    return symbols[suit] || suit;
  }

  /**
   * 获取花色的颜色
   * @param {string} suit - 花色字符
   * @returns {string} - 颜色名称
   */
  static getSuitColor(suit) {
    if (suit === 'H' || suit === 'D') {
      return 'red';
    }
    return 'black';
  }

  /**
   * 格式化牌面展示
   * @param {string} card - 扑克牌字符串
   * @returns {string} - 格式化后的牌面文本
   */
  static formatCard(card) {
    if (!card || card.length < 2) return '';
    
    const rank = this.getCardRank(card);
    const suit = this.getCardSuit(card);
    const symbol = this.getSuitSymbol(suit);
    
    let displayRank = rank;
    if (rank === 'T') displayRank = '10';
    
    return `${displayRank}${symbol}`;
  }

  /**
   * 检查是否是同花
   * @param {Array} cards - 扑克牌数组
   * @returns {boolean} - 是否是同花
   */
  static isFlush(cards) {
    if (!cards || cards.length < 5) return false;
    
    const suits = new Set();
    cards.forEach(card => {
      suits.add(this.getCardSuit(card));
    });
    
    return suits.size === 1;
  }

  /**
   * 检查是否是顺子
   * @param {Array} cards - 扑克牌数组
   * @returns {boolean} - 是否是顺子
   */
  static isStraight(cards) {
    if (!cards || cards.length < 5) return false;
    
    const sortedCards = this.sortCardsByRank(cards);
    const ranks = sortedCards.map(card => this.getCardRank(card));
    
    // 将扑克牌点数转换为数值
    const valueMap = {
      '2': 2, '3': 3, '4': 4, '5': 5, '6': 6,
      '7': 7, '8': 8, '9': 9, 'T': 10, 'J': 11,
      'Q': 12, 'K': 13, 'A': 14
    };
    
    // 处理A可以作为1的特殊情况
    if (ranks.includes('A') && ranks.includes('2') && ranks.includes('3') && 
        ranks.includes('4') && ranks.includes('5')) {
      return true;
    }
    
    // 检查是否连续
    const values = ranks.map(rank => valueMap[rank]);
    values.sort((a, b) => b - a); // 降序排列
    
    for (let i = 1; i < values.length; i++) {
      if (values[i - 1] - values[i] !== 1) {
        return false;
      }
    }
    
    return true;
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