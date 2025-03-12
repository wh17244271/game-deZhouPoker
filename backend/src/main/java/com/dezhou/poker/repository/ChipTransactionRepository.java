package com.dezhou.poker.repository;

import com.dezhou.poker.entity.ChipTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 筹码变动记录仓库接口
 */
@Repository
public interface ChipTransactionRepository extends JpaRepository<ChipTransaction, Long> {

    /**
     * 根据用户ID查找筹码变动记录
     *
     * @param userId 用户ID
     * @return 筹码变动记录列表
     */
    List<ChipTransaction> findByUserId(Long userId);

    /**
     * 根据游戏ID查找筹码变动记录
     *
     * @param gameId 游戏ID
     * @return 筹码变动记录列表
     */
    List<ChipTransaction> findByGameId(Long gameId);

    /**
     * 根据用户ID和交易类型查找筹码变动记录
     *
     * @param userId 用户ID
     * @param type   交易类型
     * @return 筹码变动记录列表
     */
    List<ChipTransaction> findByUserIdAndType(Long userId, String type);

    /**
     * 根据用户ID和交易时间范围查找筹码变动记录
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 筹码变动记录列表
     */
    List<ChipTransaction> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 计算用户总赢钱金额
     *
     * @param userId 用户ID
     * @return 总赢钱金额
     */
    @Query("SELECT SUM(t.amount) FROM ChipTransaction t WHERE t.userId = ?1 AND t.type = 'WIN'")
    BigDecimal calculateTotalWinnings(Long userId);

    /**
     * 计算用户总输钱金额
     *
     * @param userId 用户ID
     * @return 总输钱金额
     */
    @Query("SELECT SUM(ABS(t.amount)) FROM ChipTransaction t WHERE t.userId = ?1 AND t.type = 'LOSE'")
    BigDecimal calculateTotalLosses(Long userId);
}
