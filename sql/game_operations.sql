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