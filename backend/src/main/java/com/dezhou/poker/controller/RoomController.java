package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.RoomPlayerService;
import com.dezhou.poker.service.RoomService;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房间控制器
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private RoomPlayerService roomPlayerService;

    @Autowired
    private UserService userService;

    /**
     * 创建房间
     *
     * @param currentUser 当前用户
     * @param name        房间名称
     * @param password    房间密码
     * @param minPlayers  最小玩家数
     * @param maxPlayers  最大玩家数
     * @param smallBlind  小盲注
     * @param bigBlind    大盲注
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @RequestParam String name,
                                        @RequestParam(required = false) String password,
                                        @RequestParam(defaultValue = "2") Integer minPlayers,
                                        @RequestParam(defaultValue = "9") Integer maxPlayers,
                                        @RequestParam BigDecimal smallBlind,
                                        @RequestParam BigDecimal bigBlind) {
        Room room = roomService.createRoom(name, password, currentUser.getId(), minPlayers, maxPlayers, smallBlind, bigBlind);
        return ResponseEntity.ok(new ApiResponse(true, "房间创建成功", room));
    }

    /**
     * 获取活跃房间列表
     *
     * @return 活跃房间列表
     */
    @GetMapping
    public ResponseEntity<?> getActiveRooms() {
        List<Room> rooms = roomService.getActiveRooms();
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * 分页获取活跃房间列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResponseEntity<?> getActiveRoomsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Room> roomPage = roomService.getActiveRoomsPage(page, size);
        return ResponseEntity.ok(roomPage);
    }

    /**
     * 获取房间详情
     *
     * @param roomId 房间ID
     * @return 房间详情
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomDetails(@PathVariable Long roomId) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
        }
        
        // 获取房间玩家列表
        List<RoomPlayer> players = roomPlayerService.getRoomPlayers(roomId);
        
        // 更新当前玩家数量
        int playerCount = players.size();
        room.setCurrentPlayers(playerCount);
        
        // 获取房主信息并设置到房间对象中
        if (room.getCreatorId() != null) {
            User owner = userService.getById(room.getCreatorId());
            room.setOwner(owner);
            System.out.println("设置房主信息: " + owner);
        }
        
        // 确保玩家对象中包含用户信息
        for (RoomPlayer player : players) {
            if (player.getUser() == null && player.getUserId() != null) {
                User user = userService.getById(player.getUserId());
                player.setUser(user);
                System.out.println("设置玩家 " + player.getUserId() + " 的用户信息: " + user);
            }
        }
        
        // 更新房间信息
        roomService.updateById(room);
        
        return ResponseEntity.ok(new ApiResponse(true, "获取房间详情成功", new Object[]{room, players}));
    }

    /**
     * 加入房间
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @param seatNumber  座位号
     * @param buyIn       买入金额
     * @return 加入结果
     */
    @PostMapping("/{roomId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> joinRoom(@AuthenticationPrincipal UserPrincipal currentUser,
                                      @PathVariable Long roomId,
                                      @RequestParam Integer seatNumber,
                                      @RequestParam BigDecimal buyIn) {
        try {
            RoomPlayer roomPlayer = roomPlayerService.joinRoom(roomId, currentUser.getId(), seatNumber, buyIn);
            return ResponseEntity.ok(new ApiResponse(true, "加入房间成功", roomPlayer));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 离开房间
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @return 离开结果
     */
    @PostMapping("/{roomId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> leaveRoom(@AuthenticationPrincipal UserPrincipal currentUser,
                                       @PathVariable Long roomId) {
        roomPlayerService.leaveRoom(roomId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "离开房间成功"));
    }
    
    /**
     * 检查房间密码
     *
     * @param roomId   房间ID
     * @param password 密码
     * @return 检查结果
     */
    @PostMapping("/{roomId}/check-password")
    public ResponseEntity<?> checkPassword(@PathVariable Long roomId,
                                          @RequestParam String password) {
        boolean isMatch = roomService.checkPassword(roomId, password);
        return ResponseEntity.ok(new ApiResponse(true, "密码检查完成", isMatch));
    }

    /**
     * 准备游戏
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @return 准备结果
     */
    @PostMapping("/{roomId}/ready")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> readyGame(@AuthenticationPrincipal UserPrincipal currentUser,
                                      @PathVariable Long roomId) {
        try {
            roomPlayerService.updateStatus(roomId, currentUser.getId(), "READY");
            return ResponseEntity.ok(new ApiResponse(true, "准备成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 取消准备
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @return 取消准备结果
     */
    @PostMapping("/{roomId}/cancel-ready")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelReady(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @PathVariable Long roomId) {
        try {
            roomPlayerService.updateStatus(roomId, currentUser.getId(), "WAITING");
            return ResponseEntity.ok(new ApiResponse(true, "取消准备成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 开始游戏
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @return 开始游戏结果
     */
    @PostMapping("/{roomId}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startGame(@AuthenticationPrincipal UserPrincipal currentUser,
                                      @PathVariable Long roomId) {
        try {
            // 检查是否是房主
            Room room = roomService.getById(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }
            
            if (!room.getCreatorId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "只有房主才能开始游戏"));
            }
            
            // 检查玩家数量是否满足最小要求
            List<RoomPlayer> players = roomPlayerService.getRoomPlayers(roomId);
            if (players.size() < room.getMinPlayers()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "玩家数量不足，无法开始游戏"));
            }
            
            // 检查所有玩家是否都已准备
            boolean allReady = true;
            for (RoomPlayer player : players) {
                if (!"READY".equals(player.getStatus()) && !player.getUserId().equals(room.getCreatorId())) {
                    allReady = false;
                    break;
                }
            }
            
            // if (!allReady) {
            //     return ResponseEntity.badRequest().body(new ApiResponse(false, "有玩家尚未准备，无法开始游戏"));
            // }
            
            // 更新房间状态为游戏中
            roomService.updateStatus(roomId, "PLAYING");
            
            // 更新所有玩家状态为游戏中
            for (RoomPlayer player : players) {
                roomPlayerService.updateStatus(roomId, player.getUserId(), "PLAYING");
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "游戏开始成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
