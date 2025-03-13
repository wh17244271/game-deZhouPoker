# 德州扑克游戏数据库设计建议

## 现有数据库结构

当前系统已经有以下表结构：

### 用户相关
- `users`: 用户基本信息
- `room_players`: 用户在房间中的状态

### 游戏相关
- `rooms`: 游戏房间信息
- `game_history`: 游戏历史记录
- `player_game_history`: 玩家在游戏中的历史记录
- `game_actions`: 游戏中的动作记录
- `allin_votes`: All-in投票记录
- `chip_transactions`: 筹码交易记录

## 优化建议

根据德州扑克游戏的规则和应用代码，以下是一些优化建议：

### 1. 添加玩家状态

我们已经通过迁移脚本修改了 `room_players` 表的 `status` 字段，使其支持以下状态：
- `WAITING`: 等待中
- `SEATED`: 已入座
- `IN_GAME`: 游戏中
- `ACTIVE`: 活跃
- `FOLDED`: 已弃牌
- `LEFT`: 已离开

这些状态足以满足德州扑克游戏的需求。

### 2. 添加更多游戏轮次状态

目前 `game_actions` 表的 `round` 字段支持以下轮次：
- `PRE_FLOP`: 翻牌前
- `FLOP`: 翻牌
- `TURN`: 转牌
- `RIVER`: 河牌

这些轮次状态是标准的德州扑克游戏轮次。

### 3. 增强游戏历史记录

在 `game_history` 表中，建议添加以下字段：
- `winner_ids`: 赢家ID列表，用于记录多个赢家（可用JSON格式存储）
- `total_pot`: 总奖池金额
- `side_pots`: 边池信息（可用JSON格式存储）

### 4. 添加玩家统计数据

建议在 `users` 表中添加更多的统计字段：
- `best_hand`: 最佳牌型
- `biggest_pot`: 最大奖池
- `hands_played`: 总手牌数
- `hands_won`: 赢得的手牌数
- `fold_percentage`: 弃牌百分比

### 5. 优化筹码管理

为了更好地管理筹码，建议在 `chip_transactions` 表中添加更细致的交易类型：
- `BUY_IN`: 买入
- `CASH_OUT`: 兑出
- `WIN_MAIN_POT`: 赢得主池
- `WIN_SIDE_POT`: 赢得边池
- `BLIND_SMALL`: 小盲注
- `BLIND_BIG`: 大盲注
- `BET`: 下注
- `CALL`: 跟注
- `RAISE`: 加注
- `ALL_IN`: 全下

## 完整表结构建议

以下是建议的完整表结构设计，包括现有表和新增表：

### users（用户）
- `user_id`: BIGINT, PRIMARY KEY
- `username`: VARCHAR(50), NOT NULL
- `password`: VARCHAR(255), NOT NULL
- `current_chips`: DECIMAL(15,2)
- `total_games`: INT
- `wins`: INT
- `best_hand`: VARCHAR(50)
- `biggest_pot`: DECIMAL(15,2)
- `hands_played`: INT
- `hands_won`: INT
- `fold_percentage`: DECIMAL(5,2)
- `created_at`: TIMESTAMP
- `last_login`: TIMESTAMP
- `deleted`: TINYINT(1)

### rooms（房间）
- `room_id`: BIGINT, PRIMARY KEY
- `room_name`: VARCHAR(100), NOT NULL
- `room_password`: VARCHAR(50)
- `owner_id`: BIGINT, FOREIGN KEY -> users.user_id
- `status`: ENUM('WAITING', 'PLAYING', 'FINISHED')
- `min_players`: INT
- `max_players`: INT
- `current_players`: INT
- `small_blind`: DECIMAL(15,2), NOT NULL
- `big_blind`: DECIMAL(15,2), NOT NULL
- `deleted`: TINYINT(1)
- `created_at`: TIMESTAMP

### room_players（房间玩家）
- `room_id`: BIGINT, FOREIGN KEY -> rooms.room_id, PRIMARY KEY (composite)
- `user_id`: BIGINT, FOREIGN KEY -> users.user_id, PRIMARY KEY (composite)
- `seat_number`: INT
- `current_chips`: DECIMAL(15,2)
- `status`: VARCHAR(20) ('WAITING', 'SEATED', 'IN_GAME', 'ACTIVE', 'FOLDED', 'LEFT')
- `last_action`: VARCHAR(50)
- `last_bet`: DECIMAL(15,2)
- `deleted`: TINYINT(1)

### game_history（游戏历史）
- `game_id`: BIGINT, PRIMARY KEY
- `room_id`: BIGINT, FOREIGN KEY -> rooms.room_id
- `start_time`: TIMESTAMP
- `end_time`: TIMESTAMP
- `pot_size`: DECIMAL(15,2)
- `community_cards`: VARCHAR(30)
- `status`: ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED')
- `big_blind`: DECIMAL(19,2)
- `big_blind_position`: INT
- `small_blind`: DECIMAL(19,2)
- `small_blind_position`: INT
- `dealer_position`: INT
- `current_player_id`: BIGINT
- `current_round`: INT
- `current_bet`: DECIMAL(19,2)
- `winner_ids`: VARCHAR(255)  # JSON格式存储多个赢家ID
- `total_pot`: DECIMAL(15,2)
- `side_pots`: TEXT  # JSON格式存储边池信息
- `created_at`: DATETIME
- `updated_at`: DATETIME
- `deleted`: TINYINT(1)

### player_game_history（玩家游戏历史）
- `game_id`: BIGINT, FOREIGN KEY -> game_history.game_id, PRIMARY KEY (composite)
- `user_id`: BIGINT, FOREIGN KEY -> users.user_id, PRIMARY KEY (composite)
- `initial_chips`: DECIMAL(15,2)
- `final_chips`: DECIMAL(15,2)
- `hole_cards`: VARCHAR(10)
- `final_hand_type`: VARCHAR(50)
- `position`: INT
- `is_winner`: TINYINT(1)
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP
- `deleted`: TINYINT(1)

### game_actions（游戏动作）
- `action_id`: BIGINT, PRIMARY KEY
- `game_id`: BIGINT, FOREIGN KEY -> game_history.game_id
- `user_id`: BIGINT, FOREIGN KEY -> users.user_id
- `action_type`: ENUM('FOLD', 'CHECK', 'CALL', 'BET', 'RAISE', 'ALL_IN')
- `amount`: DECIMAL(15,2)
- `action_time`: TIMESTAMP
- `round`: ENUM('PRE_FLOP', 'FLOP', 'TURN', 'RIVER')
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP
- `deleted`: TINYINT(1)

### allin_votes（全下投票）
- `vote_id`: BIGINT, PRIMARY KEY
- `game_id`: BIGINT, FOREIGN KEY -> game_history.game_id
- `user_id`: BIGINT, FOREIGN KEY -> users.user_id
- `vote_option`: INT
- `vote_time`: TIMESTAMP
- `created_at`: DATETIME
- `updated_at`: DATETIME
- `deleted`: TINYINT(1)

### chip_transactions（筹码交易）
- `transaction_id`: BIGINT, PRIMARY KEY
- `user_id`: BIGINT, FOREIGN KEY -> users.user_id
- `amount`: DECIMAL(15,2)
- `transaction_type`: ENUM('BUY_IN', 'CASH_OUT', 'WIN_MAIN_POT', 'WIN_SIDE_POT', 'BLIND_SMALL', 'BLIND_BIG', 'BET', 'CALL', 'RAISE', 'ALL_IN')
- `game_id`: BIGINT, FOREIGN KEY -> game_history.game_id
- `transaction_time`: TIMESTAMP
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP
- `deleted`: TINYINT(1)

## 结论

以上建议的数据库结构基于现有的应用程序代码和数据库设计，并进行了一些扩展和优化。这些修改可以更好地支持德州扑克游戏的功能，提供更丰富的游戏数据记录和玩家统计信息。

由于已经有一个正在运行的应用程序，建议通过增量方式实施这些更改，而不是一次性重新设计整个数据库。这样可以最大限度地减少对现有功能的影响。 