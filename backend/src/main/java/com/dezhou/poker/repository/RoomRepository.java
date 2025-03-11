package com.dezhou.poker.repository;

import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.Room.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房间仓库接口
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 根据状态查找房间
     *
     * @param status 房间状态
     * @return 房间列表
     */
    List<Room> findByStatus(RoomStatus status);

    /**
     * 查找非结束状态的房间
     *
     * @param status 结束状态
     * @return 房间列表
     */
    List<Room> findByStatusNot(RoomStatus status);

    /**
     * 根据房主ID查找房间
     *
     * @param ownerId 房主ID
     * @return 房间列表
     */
    List<Room> findByOwnerId(Long ownerId);
}
