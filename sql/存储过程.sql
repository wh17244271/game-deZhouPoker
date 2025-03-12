-- 用户相关操作

-- 创建新用户
DELIMITER //
CREATE PROCEDURE create_user(
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255),
    IN p_initial_chips DECIMAL(15,2)
)
BEGIN
    INSERT INTO users (username, password, current_chips)
    VALUES (p_username, p_password, p_initial_chips);
END //
DELIMITER ;

-- 用户登录（更新最后登录时间）
DELIMITER //
CREATE PROCEDURE user_login(
    IN p_username VARCHAR(50)
)
BEGIN
    UPDATE users 
    SET last_login = CURRENT_TIMESTAMP
    WHERE username = p_username;
    
    SELECT user_id, username, current_chips, total_games, wins
    FROM users
    WHERE username = p_username;
END //
DELIMITER ;

-- 更新用户筹码
DELIMITER //
CREATE PROCEDURE update_user_chips(
    IN p_user_id BIGINT,
    IN p_amount DECIMAL(15,2),
    IN p_transaction_type VARCHAR(20),
    IN p_game_id BIGINT
)
BEGIN
    -- 更新用户筹码
    UPDATE users 
    SET current_chips = current_chips + p_amount
    WHERE user_id = p_user_id;
    
    -- 记录筹码变动
    INSERT INTO chip_transactions (user_id, amount, transaction_type, game_id)
    VALUES (p_user_id, p_amount, p_transaction_type, p_game_id);
END //
DELIMITER ;

-- 获取用户统计信息
DELIMITER //
CREATE PROCEDURE get_user_stats(
    IN p_user_id BIGINT
)
BEGIN
    SELECT 
        u.username,
        u.current_chips,
        u.total_games,
        u.wins,
        COUNT(DISTINCT gh.game_id) as games_today,
        SUM(CASE WHEN ct.transaction_type = 'WIN' THEN ct.amount ELSE 0 END) as total_winnings,
        SUM(CASE WHEN ct.transaction_type = 'LOSE' THEN ABS(ct.amount) ELSE 0 END) as total_losses
    FROM users u
    LEFT JOIN player_game_history pgh ON u.user_id = pgh.user_id
    LEFT JOIN game_history gh ON pgh.game_id = gh.game_id AND DATE(gh.start_time) = CURDATE()
    LEFT JOIN chip_transactions ct ON u.user_id = ct.user_id
    WHERE u.user_id = p_user_id
    GROUP BY u.user_id;
END //
DELIMITER ;
-- 房间相关操作

-- 创建新房间
DELIMITER //
CREATE PROCEDURE create_room(
    IN p_room_name VARCHAR(100),
    IN p_room_password VARCHAR(50),
    IN p_owner_id BIGINT,
    IN p_min_players INT,
    IN p_max_players INT,
    IN p_small_blind DECIMAL(15,2),
    IN p_big_blind DECIMAL(15,2)
)
BEGIN
    INSERT INTO rooms (
        room_name, room_password, owner_id, 
        min_players, max_players, 
        small_blind, big_blind
    )
    VALUES (
        p_room_name, p_room_password, p_owner_id,
        p_min_players, p_max_players,
        p_small_blind, p_big_blind
    );
    
    SELECT LAST_INSERT_ID() as room_id;
END //
DELIMITER ;

-- 加入房间
DELIMITER //
CREATE PROCEDURE join_room(
    IN p_room_id BIGINT,
    IN p_user_id BIGINT,
    IN p_seat_number INT,
    IN p_buy_in_amount DECIMAL(15,2)
)
BEGIN
    DECLARE v_current_players INT;
    DECLARE v_max_players INT;
    
    -- 获取当前房间信息
    SELECT current_players, max_players 
    INTO v_current_players, v_max_players
    FROM rooms 
    WHERE room_id = p_room_id;
    
    -- 检查是否还有空位
    IF v_current_players < v_max_players THEN
        -- 添加玩家到房间
        INSERT INTO room_players (room_id, user_id, seat_number, current_chips)
        VALUES (p_room_id, p_user_id, p_seat_number, p_buy_in_amount);
        
        -- 更新房间当前玩家数
        UPDATE rooms 
        SET current_players = current_players + 1
        WHERE room_id = p_room_id;
        
        -- 记录筹码变动
        INSERT INTO chip_transactions (user_id, amount, transaction_type)
        VALUES (p_user_id, -p_buy_in_amount, 'BUY_IN');
        
        SELECT TRUE as success;
    ELSE
        SELECT FALSE as success;
    END IF;
END //
DELIMITER ;

-- 获取房间信息
DELIMITER //
CREATE PROCEDURE get_room_info(
    IN p_room_id BIGINT
)
BEGIN
    SELECT 
        r.*,
        u.username as owner_username,
        GROUP_CONCAT(
            CONCAT(rp.seat_number, ':', up.username, ':', rp.current_chips, ':', rp.status)
            ORDER BY rp.seat_number
        ) as players_info
    FROM rooms r
    JOIN users u ON r.owner_id = u.user_id
    LEFT JOIN room_players rp ON r.room_id = rp.room_id
    LEFT JOIN users up ON rp.user_id = up.user_id
    WHERE r.room_id = p_room_id
    GROUP BY r.room_id;
END //
DELIMITER ;

-- 获取活跃房间列表
DELIMITER //
CREATE PROCEDURE get_active_rooms()
BEGIN
    SELECT 
        r.room_id,
        r.room_name,
        r.status,
        r.current_players,
        r.max_players,
        r.small_blind,
        r.big_blind,
        u.username as owner_username,
        CASE WHEN r.room_password IS NOT NULL THEN 1 ELSE 0 END as has_password
    FROM rooms r
    JOIN users u ON r.owner_id = u.user_id
    WHERE r.status != 'FINISHED'
    ORDER BY r.created_at DESC;
END //
DELIMITER ;

-- 游戏相关操作

-- 开始新游戏
DELIMITER //
CREATE PROCEDURE start_new_game(
    IN p_room_id BIGINT
)
BEGIN
    DECLARE v_game_id BIGINT;
    
    -- 创建新游戏记录
    INSERT INTO game_history (room_id)
    VALUES (p_room_id);
    
    SET v_game_id = LAST_INSERT_ID();
    
    -- 为房间中的所有玩家创建游戏记录
    INSERT INTO player_game_history (game_id, user_id, initial_chips)
    SELECT v_game_id, rp.user_id, rp.current_chips
    FROM room_players rp
    WHERE rp.room_id = p_room_id AND rp.status = 'ACTIVE';
    
    -- 更新房间状态
    UPDATE rooms 
    SET status = 'PLAYING'
    WHERE room_id = p_room_id;
    
    SELECT v_game_id as game_id;
END //
DELIMITER ;

-- 记录游戏动作
DELIMITER //
CREATE PROCEDURE record_game_action(
    IN p_game_id BIGINT,
    IN p_user_id BIGINT,
    IN p_action_type VARCHAR(20),
    IN p_amount DECIMAL(15,2),
    IN p_round VARCHAR(20)
)
BEGIN
    -- 记录动作
    INSERT INTO game_actions (
        game_id, user_id, action_type, amount, round
    )
    VALUES (
        p_game_id, p_user_id, p_action_type, p_amount, p_round
    );
    
    -- 如果是下注相关的动作，更新玩家筹码
    IF p_action_type IN ('BET', 'CALL', 'RAISE') THEN
        UPDATE room_players rp
        JOIN game_history gh ON rp.room_id = gh.room_id
        SET rp.current_chips = rp.current_chips - p_amount,
            rp.last_action = p_action_type,
            rp.last_bet = p_amount
        WHERE gh.game_id = p_game_id AND rp.user_id = p_user_id;
    END IF;
    
    -- 如果是ALL_IN，更新玩家状态
    IF p_action_type = 'ALL_IN' THEN
        UPDATE room_players rp
        JOIN game_history gh ON rp.room_id = gh.room_id
        SET rp.status = 'ALL_IN',
            rp.last_action = 'ALL_IN',
            rp.last_bet = p_amount
        WHERE gh.game_id = p_game_id AND rp.user_id = p_user_id;
    END IF;
END //
DELIMITER ;

-- 记录All-in投票
DELIMITER //
CREATE PROCEDURE record_allin_vote(
    IN p_game_id BIGINT,
    IN p_user_id BIGINT,
    IN p_vote_option INT
)
BEGIN
    INSERT INTO allin_votes (game_id, user_id, vote_option)
    VALUES (p_game_id, p_user_id, p_vote_option);
    
    -- 返回当前投票统计
    SELECT 
        vote_option,
        COUNT(*) as vote_count
    FROM allin_votes
    WHERE game_id = p_game_id
    GROUP BY vote_option
    ORDER BY vote_count DESC;
END //
DELIMITER ;

-- 结束游戏并结算
DELIMITER //
CREATE PROCEDURE end_game(
    IN p_game_id BIGINT,
    IN p_community_cards VARCHAR(30)
)
BEGIN
    DECLARE v_pot_size DECIMAL(15,2);
    
    -- 计算总奖池
    SELECT SUM(amount)
    INTO v_pot_size
    FROM game_actions
    WHERE game_id = p_game_id 
    AND action_type IN ('BET', 'CALL', 'RAISE', 'ALL_IN');
    
    -- 更新游戏历史记录
    UPDATE game_history
    SET status = 'COMPLETED',
        end_time = CURRENT_TIMESTAMP,
        pot_size = v_pot_size,
        community_cards = p_community_cards
    WHERE game_id = p_game_id;
    
    -- 更新房间状态
    UPDATE rooms r
    JOIN game_history gh ON r.room_id = gh.room_id
    SET r.status = 'WAITING'
    WHERE gh.game_id = p_game_id;
END //
DELIMITER ;

-- 更新获胜者信息
DELIMITER //
CREATE PROCEDURE update_winner(
    IN p_game_id BIGINT,
    IN p_user_id BIGINT,
    IN p_final_chips DECIMAL(15,2),
    IN p_final_hand_type VARCHAR(50)
)
BEGIN
    -- 更新玩家游戏历史
    UPDATE player_game_history
    SET final_chips = p_final_chips,
        final_hand_type = p_final_hand_type,
        is_winner = TRUE
    WHERE game_id = p_game_id AND user_id = p_user_id;
    
    -- 更新玩家统计信息
    UPDATE users
    SET total_games = total_games + 1,
        wins = wins + 1,
        current_chips = current_chips + p_final_chips
    WHERE user_id = p_user_id;
    
    -- 记录筹码变动
    INSERT INTO chip_transactions (user_id, amount, transaction_type, game_id)
    VALUES (p_user_id, p_final_chips, 'WIN', p_game_id);
END //
DELIMITER ;