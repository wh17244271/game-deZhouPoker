package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.exception.ResourceNotFoundException;
import com.dezhou.poker.mapper.RoomPlayerMapper;
import com.dezhou.poker.service.RoomPlayerService;
import com.dezhou.poker.service.RoomService;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间玩家服务实现类
 *
 * @author CodeGenerator
 * @since 2023-03-11
 */
@Service
public class RoomPlayerServiceImpl extends ServiceImpl<RoomPlayerMapper, RoomPlayer> implements RoomPlayerService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public RoomPlayer joinRoom(Long roomId, Long userId, Integer seatNumber, BigDecimal buyIn) {
        // 获取房间和用户
        Room room = roomService.getById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room", "id", roomId);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // 检查房间状态
        if (!"WAITING".equals(room.getStatus())) {
            throw new BusinessException("房间不处于等待状态，无法加入");
        }

        // 检查房间是否已满
        long playerCount = count(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId));
        if (playerCount >= room.getMaxPlayers()) {
            throw new BusinessException("房间已满");
        }

        // 检查座位是否已被占用
        RoomPlayer existingSeat = baseMapper.selectBySeat(roomId, seatNumber);
        if (existingSeat != null) {
            throw new BusinessException("座位已被占用");
        }

        // 检查用户筹码是否足够
        if (user.getCurrentChips().compareTo(buyIn) < 0) {
            throw new BusinessException("筹码不足");
        }

        // 更新用户筹码
        if (!userService.updateChips(userId, buyIn.negate())) {
            throw new BusinessException("更新用户筹码失败");
        }

        // 创建房间玩家关系
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setRoomId(roomId);
        roomPlayer.setUserId(userId);
        roomPlayer.setSeatNumber(seatNumber);
        roomPlayer.setCurrentChips(buyIn);
        roomPlayer.setStatus("WAITING");
        roomPlayer.setJoinedAt(LocalDateTime.now());
        roomPlayer.setDeleted(0);

        save(roomPlayer);

        // 更新房间当前玩家数量
        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        roomService.updateById(room);

        return roomPlayer;
    }

    @Override
    @Transactional
    public boolean leaveRoom(Long roomId, Long userId) {
        // 获取房间玩家关系
        RoomPlayer roomPlayer = getOne(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId));

        if (roomPlayer == null) {
            throw new ResourceNotFoundException("RoomPlayer", "roomId and userId", roomId + "," + userId);
        }

        // 更新用户筹码
        if (!userService.updateChips(userId, roomPlayer.getCurrentChips())) {
            throw new BusinessException("更新用户筹码失败");
        }

        // 删除房间玩家关系
        boolean result = remove(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId));

        // 更新房间当前玩家数量
        Room room = roomService.getById(roomId);
        room.setCurrentPlayers(Math.max(0, room.getCurrentPlayers() - 1));
        roomService.updateById(room);

        // 检查房间是否还有玩家
        long playerCount = count(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId));
        if (playerCount == 0) {
            // 如果没有玩家了，更新房间状态为已结束
            roomService.updateStatus(roomId, "FINISHED");
        }

        return result;
    }

    @Override
    public List<RoomPlayer> getRoomPlayers(Long roomId) {
        return baseMapper.selectByRoomId(roomId);
    }

    @Override
    public List<RoomPlayer> getUserRooms(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean updateStatus(Long roomId, Long userId, String status) {
        try {
            // 将字符串转换为枚举值
            RoomPlayer.PlayerStatus playerStatus = RoomPlayer.PlayerStatus.valueOf(status);
            return update(new LambdaUpdateWrapper<RoomPlayer>()
                    .eq(RoomPlayer::getRoomId, roomId)
                    .eq(RoomPlayer::getUserId, userId)
                    .set(RoomPlayer::getStatus, playerStatus.name()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid player status: " + status, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateChips(Long roomId, Long userId, BigDecimal amount) {
        RoomPlayer roomPlayer = getOne(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId));

        if (roomPlayer == null) {
            throw new ResourceNotFoundException("RoomPlayer", "roomId and userId", roomId + "," + userId);
        }

        BigDecimal newChips = roomPlayer.getCurrentChips().add(amount);
        if (newChips.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("筹码不足");
        }

        return update(new LambdaUpdateWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId)
                .set(RoomPlayer::getCurrentChips, newChips));
    }
} 