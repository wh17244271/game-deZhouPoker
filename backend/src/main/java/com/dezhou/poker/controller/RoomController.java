package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.exception.BusinessException;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.GameService;
import com.dezhou.poker.service.RoomPlayerService;
import com.dezhou.poker.service.RoomService;
import com.dezhou.poker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 房间控制器
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private RoomPlayerService roomPlayerService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
            
            // 检查房间是否已经在游戏中
            if ("PLAYING".equals(room.getStatus())) {
                // 如果房间已经在游戏中，尝试获取当前游戏
                GameHistory existingGame = gameService.getCurrentGame(roomId);
                if (existingGame != null) {
                    // 返回已存在的游戏ID
                    Map<String, Object> response = new HashMap<>();
                    response.put("gameId", existingGame.getId());
                    return ResponseEntity.ok(new ApiResponse(true, "游戏已经开始", response));
                }
            }
            
            // 创建游戏实例
            GameHistory gameHistory = gameService.startNewGame(roomId);
            
            // 更新房间状态为游戏中
            roomService.updateStatus(roomId, "PLAYING");
            
            // 更新所有玩家状态为游戏中
            for (RoomPlayer player : players) {
                roomPlayerService.updateStatus(roomId, player.getUserId(), "ACTIVE");
            }
            
            // 返回游戏ID
            Map<String, Object> response = new HashMap<>();
            response.put("gameId", gameHistory.getId());
            
            return ResponseEntity.ok(new ApiResponse(true, "游戏开始成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 删除房间
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @return 删除结果
     */
    @DeleteMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRoom(@AuthenticationPrincipal UserPrincipal currentUser,
                                       @PathVariable Long roomId) {
        try {
            // 获取房间
            Room room = roomService.getById(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }
            
            // 检查是否是房主
            if (!room.getCreatorId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "只有房主才能删除房间"));
            }
            
            // 检查房间状态
            if ("PLAYING".equals(room.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "游戏进行中，无法删除房间"));
            }
            
            // 获取房间内的所有玩家
            List<RoomPlayer> players = roomPlayerService.getRoomPlayers(roomId);
            
            // 将所有玩家的筹码返还
            for (RoomPlayer player : players) {
                // 跳过房主自己
                if (!player.getUserId().equals(currentUser.getId())) {
                    // 返还筹码
                    userService.updateChips(player.getUserId(), player.getCurrentChips());
                    
                    // 删除房间玩家关系
                    roomPlayerService.remove(new LambdaQueryWrapper<RoomPlayer>()
                            .eq(RoomPlayer::getRoomId, roomId)
                            .eq(RoomPlayer::getUserId, player.getUserId()));
                }
            }
            
            // 删除房间
            roomService.removeById(roomId);
            
            return ResponseEntity.ok(new ApiResponse(true, "房间删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 玩家入座
     *
     * @param currentUser 当前用户
     * @param roomId      房间ID
     * @param seatNumber  座位号
     * @return 入座结果
     */
    @PostMapping("/{roomId}/seat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> seatPlayer(@AuthenticationPrincipal UserPrincipal currentUser,
                                    @PathVariable Long roomId,
                                    @RequestBody(required = false) Map<String, Integer> requestBody) {
        try {
            logger.info("收到玩家入座请求, 房间ID: {}, 用户ID: {}", roomId, currentUser.getId());
            
            final Integer requestedSeat = requestBody != null ? requestBody.get("seatNumber") : null;
            Integer seatNumber = requestedSeat;
            logger.info("玩家入座请求详情, 房间ID: {}, 用户ID: {}, 座位号: {}", roomId, currentUser.getId(), seatNumber);

            // 获取房间
            Room room = roomService.getById(roomId);
            if (room == null) {
                logger.error("房间不存在, 房间ID: {}", roomId);
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }

            // 检查用户是否已在该房间
            RoomPlayer existingPlayer = roomService.getRoomPlayer(roomId, currentUser.getId());
            if (existingPlayer == null) {
                logger.error("用户不在房间中, 房间ID: {}, 用户ID: {}", roomId, currentUser.getId());
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您不在该房间中，请先加入房间"));
            }

            // 如果用户已经有座位，直接返回成功
            if (existingPlayer.getSeatNumber() != null) {
                logger.info("用户已经有座位, 房间ID: {}, 用户ID: {}, 座位号: {}", roomId, currentUser.getId(), existingPlayer.getSeatNumber());
                return ResponseEntity.ok(new ApiResponse(true, "您已经入座", existingPlayer.getSeatNumber()));
            }

            // 检查座位是否已被占用（如果指定了座位号）
            if (seatNumber != null) {
                List<RoomPlayer> roomPlayers = roomService.getRoomPlayers(roomId);
                final Integer requestedSeatFinal = requestedSeat; // Create a final copy for the lambda
                RoomPlayer seatOccupier = roomPlayers.stream()
                    .filter(rp -> requestedSeatFinal.equals(rp.getSeatNumber()))
                    .findFirst()
                    .orElse(null);
                    
                if (seatOccupier != null && !seatOccupier.getUserId().equals(currentUser.getId())) {
                    logger.error("座位已被占用, 房间ID: {}, 座位号: {}, 占用者ID: {}", roomId, seatNumber, seatOccupier.getUserId());
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "座位已被占用"));
                }
            }

            // 如果没有指定座位号，自动分配一个座位
            if (seatNumber == null) {
                List<RoomPlayer> seatedPlayers = roomService.getRoomPlayers(roomId).stream()
                    .filter(rp -> rp.getSeatNumber() != null)
                    .collect(Collectors.toList());
                    
                Set<Integer> occupiedSeats = seatedPlayers.stream()
                    .map(RoomPlayer::getSeatNumber)
                    .collect(Collectors.toSet());

                // 默认最大座位数为9
                int maxSeats = room.getMaxPlayers() != null ? room.getMaxPlayers() : 9;
                for (int i = 1; i <= maxSeats; i++) {
                    if (!occupiedSeats.contains(i)) {
                        seatNumber = i;
                        break;
                    }
                }

                if (seatNumber == null) {
                    logger.error("没有可用座位, 房间ID: {}", roomId);
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "没有可用座位"));
                }
            }

            // 更新用户座位
            existingPlayer.setSeatNumber(seatNumber);
            boolean updated = roomService.updateRoomPlayer(existingPlayer);
            
            if (!updated) {
                logger.error("更新用户座位失败, 房间ID: {}, 用户ID: {}, 座位号: {}", roomId, currentUser.getId(), seatNumber);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "入座失败: 更新用户座位失败"));
            }
            
            // 推送房间更新消息
            messagingTemplate.convertAndSend("/topic/room/" + roomId, Collections.singletonMap("type", "ROOM_UPDATE"));

            logger.info("用户入座成功, 房间ID: {}, 用户ID: {}, 座位号: {}", roomId, currentUser.getId(), seatNumber);
            return ResponseEntity.ok(new ApiResponse(true, "入座成功", seatNumber));
        } catch (Exception e) {
            logger.error("玩家入座失败, 房间ID: {}, 用户ID: {}, 错误: {}", roomId, currentUser.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "入座失败: " + e.getMessage()));
        }
    }
}
