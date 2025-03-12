package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 用户Mapper接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 更新用户筹码
     *
     * @param userId 用户ID
     * @param amount 筹码变动量
     * @return 影响行数
     */
    @Update("UPDATE users SET current_chips = current_chips + #{amount} WHERE user_id = #{userId}")
    int updateChips(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 更新用户游戏统计信息
     *
     * @param userId 用户ID
     * @param isWin 是否获胜
     * @return 影响行数
     */
    @Update("UPDATE users SET total_games = total_games + 1, wins = wins + #{isWin} WHERE user_id = #{userId}")
    int updateGameStats(@Param("userId") Long userId, @Param("isWin") int isWin);
} 