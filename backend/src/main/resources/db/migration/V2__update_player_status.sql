-- 修改room_players表的status字段以支持新的状态
ALTER TABLE room_players MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'WAITING';

-- 将status字段的枚举值更新为匹配PlayerStatus枚举
-- 更新已有记录为有效状态
UPDATE room_players SET status = 'WAITING' WHERE status IS NULL OR status = '';
UPDATE room_players SET status = 'SEATED' WHERE status = 'ACTIVE';
UPDATE room_players SET status = 'FOLDED' WHERE status = 'FOLDED';
UPDATE room_players SET status = 'IN_GAME' WHERE status = 'ALL_IN';

-- 创建注释
ALTER TABLE room_players MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '玩家状态：WAITING(等待中), SEATED(已入座), IN_GAME(游戏中), ACTIVE(活跃), FOLDED(已弃牌), LEFT(已离开)'; 