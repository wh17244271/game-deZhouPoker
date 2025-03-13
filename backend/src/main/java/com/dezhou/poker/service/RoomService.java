package com.dezhou.poker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.model.RoomPlayerId;
import com.dezhou.poker.model.User;
import com.dezhou.poker.repository.RoomPlayerRepository;
import com.dezhou.poker.repository.RoomRepository;
import com.dezhou.poker.mapper.RoomMapper;
import com.dezhou.poker.mapper.RoomPlayerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间服务接口
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
public interface RoomService extends IService<Room> {
    
    /**
     * 创建房间
     *
     * @param name 房间名称
     * @param password 房间密码
     * @param creatorId 创建者ID
     * @param minPlayers 最小玩家数
     * @param maxPlayers 最大玩家数
     * @param smallBlind 小盲注
     * @param bigBlind 大盲注
     * @return 房间信息
     */
    Room createRoom(String name, String password, Long creatorId, Integer minPlayers, Integer maxPlayers, BigDecimal smallBlind, BigDecimal bigBlind);

    /**
     * 获取活跃房间列表
     *
     * @return 活跃房间列表
     */
    List<Room> getActiveRooms();

    /**
     * 分页获取活跃房间列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<Room> getActiveRoomsPage(int page, int size);

    /**
     * 更新房间状态
     *
     * @param roomId 房间ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long roomId, String status);

    /**
     * 获取房间玩家列表
     *
     * @param roomId 房间ID
     * @return 房间玩家列表
     */
    List<RoomPlayer> getRoomPlayers(Long roomId);

    /**
     * 检查房间密码
     *
     * @param roomId   房间ID
     * @param password 密码
     * @return 密码是否正确
     */
    boolean checkPassword(Long roomId, String password);

    /**
     * 获取房间中特定用户的信息
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 房间玩家信息
     */
    RoomPlayer getRoomPlayer(Long roomId, Long userId);

    /**
     * 更新房间玩家信息
     * @param roomPlayer 房间玩家信息
     * @return 是否更新成功
     */
    boolean updateRoomPlayer(RoomPlayer roomPlayer);
}
