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