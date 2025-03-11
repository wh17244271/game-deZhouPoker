package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.mapper.RoomMapper;
import com.dezhou.poker.mapper.RoomPlayerMapper;
import com.dezhou.poker.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间服务实现类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Service
@Transactional
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private RoomPlayerMapper roomPlayerMapper;

    @Override
    public Room createRoom(String name, String password, Long creatorId, Integer minPlayers, Integer maxPlayers, BigDecimal smallBlind, BigDecimal bigBlind) {
        Room room = new Room();
        room.setName(name);
        room.setPassword(password);
        room.setCreatorId(creatorId);
        room.setMinPlayers(minPlayers);
        room.setMaxPlayers(maxPlayers);
        room.setSmallBlind(smallBlind);
        room.setBigBlind(bigBlind);
        room.setCurrentPlayers(0);
        room.setStatus("WAITING");
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        room.setDeleted(0);
        
        save(room);
        return room;
    }

    @Override
    public List<Room> getActiveRooms() {
        return list(new QueryWrapper<Room>()
            .eq("status", "WAITING")
            .or()
            .eq("status", "PLAYING"));
    }

    @Override
    public IPage<Room> getActiveRoomsPage(int page, int size) {
        return page(new Page<>(page, size),
            new QueryWrapper<Room>()
                .eq("status", "WAITING")
                .or()
                .eq("status", "PLAYING"));
    }

    @Override
    public boolean updateStatus(Long roomId, String status) {
        Room room = getById(roomId);
        if (room != null) {
            room.setStatus(status);
            return updateById(room);
        }
        return false;
    }

    @Override
    public boolean checkPassword(Long roomId, String password) {
        Room room = getById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }
        
        // 如果房间没有密码，直接返回true
        if (!StringUtils.hasText(room.getPassword())) {
            return true;
        }
        
        // 如果提供的密码为空，返回false
        if (!StringUtils.hasText(password)) {
            return false;
        }
        
        // 比较密码
        return room.getPassword().equals(password);
    }

    @Override
    public List<RoomPlayer> getRoomPlayers(Long roomId) {
        return roomPlayerMapper.selectList(
            new QueryWrapper<RoomPlayer>()
                .eq("room_id", roomId)
                .orderBy(true, true, "position")
        );
    }
} 