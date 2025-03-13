package com.dezhou.poker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dezhou.poker.entity.PlayerStatus;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        roomPlayer.setStatus(PlayerStatus.SEATED);
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
            PlayerStatus playerStatus = PlayerStatus.valueOf(status);
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

    /**
     * 为用户分配座位
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param seatNumber 座位号，如果为null则自动分配
     * @return 分配的座位号
     */
    @Override
    public RoomPlayer assignSeat(Long roomId, Long userId, Integer seatNumber) {
        // 获取用户在房间中的信息
        RoomPlayer roomPlayer = this.getBaseMapper().selectOne(
            new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId)
        );
        
        if (roomPlayer == null) {
            throw new BusinessException("用户不在该房间中");
        }
        
        // 如果指定了座位号，检查是否已被占用
        if (seatNumber != null) {
            RoomPlayer existingSeat = this.getBaseMapper().selectOne(
                new LambdaQueryWrapper<RoomPlayer>()
                    .eq(RoomPlayer::getRoomId, roomId)
                    .eq(RoomPlayer::getSeatNumber, seatNumber)
                    .ne(RoomPlayer::getUserId, userId)
            );
            
            if (existingSeat != null) {
                throw new BusinessException("座位已被占用");
            }
        } else {
            // 如果未指定座位号，自动分配一个座位
            List<RoomPlayer> seatedPlayers = this.getBaseMapper().selectList(
                new LambdaQueryWrapper<RoomPlayer>()
                    .eq(RoomPlayer::getRoomId, roomId)
                    .isNotNull(RoomPlayer::getSeatNumber)
            );
            
            Set<Integer> occupiedSeats = seatedPlayers.stream()
                .map(RoomPlayer::getSeatNumber)
                .collect(Collectors.toSet());
            
            // 获取房间信息，以确定最大座位数
            Room room = roomService.getById(roomId);
            int maxSeats = room != null && room.getMaxPlayers() != null ? room.getMaxPlayers() : 9;
            
            // 寻找空闲的座位
            for (int i = 1; i <= maxSeats; i++) {
                if (!occupiedSeats.contains(i)) {
                    seatNumber = i;
                    break;
                }
            }
            
            if (seatNumber == null) {
                throw new BusinessException("没有可用座位");
            }
        }
        
        // 更新用户座位
        roomPlayer.setSeatNumber(seatNumber);
        
        // 使用条件更新而不是updateById
        boolean updated = update(new LambdaUpdateWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId)
                .set(RoomPlayer::getSeatNumber, seatNumber)
                .set(RoomPlayer::getStatus, "SEATED"));
                
        if (!updated) {
            throw new BusinessException("更新座位信息失败");
        }
        
        return roomPlayer;
    }

    @Override
    public RoomPlayer getRoomPlayer(Long roomId, Long userId) {
        return getOne(new LambdaQueryWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomId)
                .eq(RoomPlayer::getUserId, userId));
    }
    
    @Override
    public boolean updateRoomPlayer(RoomPlayer roomPlayer) {
        if (roomPlayer == null) {
            return false;
        }
        
        // 使用条件更新而不是updateById
        return update(new LambdaUpdateWrapper<RoomPlayer>()
                .eq(RoomPlayer::getRoomId, roomPlayer.getRoomId())
                .eq(RoomPlayer::getUserId, roomPlayer.getUserId())
                .set(roomPlayer.getSeatNumber() != null, RoomPlayer::getSeatNumber, roomPlayer.getSeatNumber())
                .set(roomPlayer.getStatus() != null, RoomPlayer::getStatus, roomPlayer.getStatus())
                .set(roomPlayer.getCurrentChips() != null, RoomPlayer::getCurrentChips, roomPlayer.getCurrentChips())
                .set(roomPlayer.getLastAction() != null, RoomPlayer::getLastAction, roomPlayer.getLastAction())
                .set(roomPlayer.getLastBet() != null, RoomPlayer::getLastBet, roomPlayer.getLastBet()));
    }
} 