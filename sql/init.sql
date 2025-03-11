-- 使用dezhou数据库
USE dezhou;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    current_chips DECIMAL(15,2) DEFAULT 0.00,
    total_games INT DEFAULT 0,
    wins INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 房间表
CREATE TABLE IF NOT EXISTS rooms (
    room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL,
    room_password VARCHAR(50),
    owner_id BIGINT NOT NULL,
    status ENUM('WAITING', 'PLAYING', 'FINISHED') DEFAULT 'WAITING',
    min_players INT DEFAULT 2,
    max_players INT DEFAULT 9,
    current_players INT DEFAULT 0,
    small_blind DECIMAL(15,2) NOT NULL,
    big_blind DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

-- 房间玩家关系表
CREATE TABLE IF NOT EXISTS room_players (
    room_id BIGINT,
    user_id BIGINT,
    seat_number INT NOT NULL,
    current_chips DECIMAL(15,2) NOT NULL,
    status ENUM('ACTIVE', 'FOLDED', 'ALL_IN', 'WAITING') DEFAULT 'WAITING',
    last_action VARCHAR(50),
    last_bet DECIMAL(15,2) DEFAULT 0.00,
    PRIMARY KEY (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 游戏记录表
CREATE TABLE IF NOT EXISTS game_history (
    game_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    pot_size DECIMAL(15,2) DEFAULT 0.00,
    community_cards VARCHAR(30),
    status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'IN_PROGRESS',
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- 玩家游戏记录表
CREATE TABLE IF NOT EXISTS player_game_history (
    game_id BIGINT,
    user_id BIGINT,
    initial_chips DECIMAL(15,2) NOT NULL,
    final_chips DECIMAL(15,2),
    hole_cards VARCHAR(10),
    final_hand_type VARCHAR(50),
    position INT,
    is_winner BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (game_id, user_id),
    FOREIGN KEY (game_id) REFERENCES game_history(game_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 游戏动作记录表
CREATE TABLE IF NOT EXISTS game_actions (
    action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action_type ENUM('FOLD', 'CHECK', 'CALL', 'BET', 'RAISE', 'ALL_IN') NOT NULL,
    amount DECIMAL(15,2),
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    round ENUM('PRE_FLOP', 'FLOP', 'TURN', 'RIVER') NOT NULL,
    FOREIGN KEY (game_id) REFERENCES game_history(game_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- All-in投票表
CREATE TABLE IF NOT EXISTS allin_votes (
    vote_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_option INT NOT NULL, -- 投票选择发几次牌
    vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES game_history(game_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 筹码变动记录表
CREATE TABLE IF NOT EXISTS chip_transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type ENUM('BUY_IN', 'CASH_OUT', 'WIN', 'LOSE') NOT NULL,
    game_id BIGINT,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (game_id) REFERENCES game_history(game_id)
);