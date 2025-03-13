package com.dezhou.poker.controller.endpoint;

import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.PlayerStatus;
import com.dezhou.poker.entity.Room;
import com.dezhou.poker.entity.RoomPlayer;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.RoomPlayerService;
import com.dezhou.poker.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 座位控制器
 * 处理玩家入座、离开座位等操作
 * 
 * 注：此控制器与 RoomController 中的 seatPlayer 方法存在冲突
 * 暂时禁用此控制器，使用 RoomController 中的相应功能
 */
// @RestController  // 注释掉以避免冲突
// @RequestMapping("/rooms")
public class SeatController {

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomPlayerService roomPlayerService;

    /**
     * 玩家入座
     * 
     * @param currentUser 当前用户
     * @param roomId 房间ID
     * @param requestBody 请求体，可包含座位号
     * @return 入座结果
     */
    // @PostMapping("/{roomId}/seat")  // 注释掉以避免冲突
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> seatPlayer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long roomId,
            @RequestBody(required = false) Map<String, Integer> requestBody) {
        
        try {
            Integer seatNumber = requestBody != null ? requestBody.get("seatNumber") : null;
            logger.info("玩家入座请求: 房间ID={}, 用户ID={}, 座位号={}", roomId, currentUser.getId(), seatNumber);
            
            // 获取房间
            Room room = roomService.getById(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "房间不存在"));
            }
            
            // 检查用户是否已在房间中
            RoomPlayer roomPlayer = roomPlayerService.getRoomPlayer(roomId, currentUser.getId());
            if (roomPlayer == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "您尚未加入该房间"));
            }
            
            // 如果用户已有座位，则返回当前记录
            if (roomPlayer.getSeatNumber() != null) {
                return ResponseEntity.ok(new ApiResponse(true, "您已入座", roomPlayer));
            }
            
            // 如果请求了特定座位
            if (seatNumber != null) {
                // 检查座位是否已被占用
                List<RoomPlayer> allPlayers = roomPlayerService.getRoomPlayers(roomId);
                boolean isSeatOccupied = allPlayers.stream()
                        .anyMatch(p -> seatNumber.equals(p.getSeatNumber()));
                
                if (isSeatOccupied) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "座位已被占用"));
                }
                
                // 分配请求的座位
                roomPlayer.setSeatNumber(seatNumber);
            } else {
                // 自动分配座位
                // 获取房间最大座位数
                int maxSeats = room.getMaxPlayers();
                
                // 获取已占用的座位
                List<RoomPlayer> allPlayers = roomPlayerService.getRoomPlayers(roomId);
                Set<Integer> occupiedSeats = allPlayers.stream()
                        .filter(p -> p.getSeatNumber() != null)
                        .map(RoomPlayer::getSeatNumber)
                        .collect(Collectors.toSet());
                
                // 查找第一个未被占用的座位
                Integer assignedSeat = null;
                for (int i = 1; i <= maxSeats; i++) {
                    if (!occupiedSeats.contains(i)) {
                        assignedSeat = i;
                        break;
                    }
                }
                
                // 如果所有座位都被占用
                if (assignedSeat == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "没有可用座位"));
                }
                
                roomPlayer.setSeatNumber(assignedSeat);
            }
            
            // 更新玩家状态为已入座
            roomPlayer.setStatus(PlayerStatus.SEATED);
            
            // 保存更新
            roomPlayerService.updateById(roomPlayer);
            
            return ResponseEntity.ok(new ApiResponse(true, "入座成功", roomPlayer));
        } catch (Exception e) {
            logger.error("玩家入座失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "入座失败: " + e.getMessage()));
        }
    }
} 