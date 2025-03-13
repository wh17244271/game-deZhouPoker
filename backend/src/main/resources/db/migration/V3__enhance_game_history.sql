-- 增强游戏历史记录表
ALTER TABLE game_history 
ADD COLUMN winner_ids VARCHAR(255) COMMENT '赢家ID列表，JSON格式',
ADD COLUMN total_pot DECIMAL(15,2) COMMENT '总奖池金额',
ADD COLUMN side_pots TEXT COMMENT '边池信息，JSON格式';

-- 增强用户统计数据
ALTER TABLE users 
ADD COLUMN best_hand VARCHAR(50) COMMENT '最佳牌型',
ADD COLUMN biggest_pot DECIMAL(15,2) DEFAULT 0 COMMENT '最大奖池',
ADD COLUMN hands_played INT DEFAULT 0 COMMENT '总手牌数',
ADD COLUMN hands_won INT DEFAULT 0 COMMENT '赢得的手牌数',
ADD COLUMN fold_percentage DECIMAL(5,2) DEFAULT 0 COMMENT '弃牌百分比';

-- 修改chip_transactions的transaction_type枚举类型
ALTER TABLE chip_transactions MODIFY COLUMN transaction_type ENUM(
    'BUY_IN', 
    'CASH_OUT', 
    'WIN_MAIN_POT', 
    'WIN_SIDE_POT', 
    'BLIND_SMALL', 
    'BLIND_BIG', 
    'BET', 
    'CALL', 
    'RAISE', 
    'ALL_IN',
    'WIN',
    'LOSE'
) NOT NULL; 