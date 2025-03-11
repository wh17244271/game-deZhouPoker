package com.dezhou.poker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dezhou.poker.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 房间Mapper接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 查询活跃房间列表
     *
     * @return 活跃房间列表
     */
    @Select("SELECT * FROM room WHERE status = 'ACTIVE' AND deleted = 0")
    List<Room> selectActiveRooms();

    /**
     * 分页查询活跃房间
     *
     * @param page 分页参数
     * @return 分页结果
     */
    @Select("SELECT * FROM room WHERE status = 'ACTIVE' AND deleted = 0")
    IPage<Room> selectActiveRoomsPage(Page<Room> page);

    /**
     * 更新房间状态
     *
     * @param roomId 房间ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE room SET status = #{status} WHERE id = #{roomId}")
    int updateStatus(@Param("roomId") Long roomId, @Param("status") String status);
} 