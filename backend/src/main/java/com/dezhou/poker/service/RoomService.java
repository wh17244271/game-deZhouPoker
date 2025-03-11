package com.dezhou.poker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.model.RoomPlayer;
import com.dezhou.poker.model.RoomPlayerId;
import com.dezhou.poker.model.User;
import com.dezhou.poker.repository.RoomPlayerRepository;
import com.dezhou.poker.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @return 创建的房间
     */
    Room createRoom(String name, String password, Long creatorId, Integer minPlayers, Integer maxPlayers, BigDecimal smallBlind, BigDecimal bigBlind);

    /**
     * 获取活跃房间列表
     *
     * @return 活跃房间列表
     */
    List<Room> getActiveRooms();

    /**
     * 分页获取活跃房间
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
     * 检查房间密码
     *
     * @param roomId 房间ID
     * @param password 密码
     * @return 是否匹配
     */
    boolean checkPassword(Long roomId, String password);
}
