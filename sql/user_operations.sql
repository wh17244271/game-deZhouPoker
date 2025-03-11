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