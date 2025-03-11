package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dezhou.poker.entity.ChipTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 筹码交易Mapper接口
 */
@Mapper
public interface ChipTransactionMapper extends BaseMapper<ChipTransaction> {

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @return 交易记录列表
     */
    @Select("SELECT ct.*, u.* FROM chip_transaction ct " +
            "LEFT JOIN user u ON ct.user_id = u.id " +
            "WHERE ct.user_id = #{userId} AND ct.deleted = 0 " +
            "ORDER BY ct.transaction_time DESC")
    List<ChipTransaction> selectByUserId(@Param("userId") Long userId);
} 