package com.dezhou.poker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.entity.ChipTransaction;
import com.dezhou.poker.entity.GameHistory;
import com.dezhou.poker.entity.PlayerGameHistory;
import com.dezhou.poker.entity.User;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.ChipTransactionService;
import com.dezhou.poker.service.GameHistoryService;
import com.dezhou.poker.service.PlayerGameHistoryService;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private GameHistoryService gameHistoryService;

    @Autowired
    private PlayerGameHistoryService playerGameHistoryService;

    @Autowired
    private ChipTransactionService chipTransactionService;

    /**
     * 获取当前用户信息
     *
     * @param currentUser 当前用户
     * @return 用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.getById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 获取用户游戏历史
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 游戏历史列表
     */
    @GetMapping("/{userId}/game-history")
    public ResponseEntity<?> getUserGameHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PlayerGameHistory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PlayerGameHistory> queryWrapper = new LambdaQueryWrapper<PlayerGameHistory>()
                .eq(PlayerGameHistory::getUserId, userId)
                .orderByDesc(PlayerGameHistory::getCreatedAt);
        
        IPage<PlayerGameHistory> historyPage = playerGameHistoryService.page(pageParam, queryWrapper);
        return ResponseEntity.ok(historyPage);
    }

    /**
     * 获取用户筹码交易记录
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 筹码交易记录列表
     */
    @GetMapping("/{userId}/chip-transactions")
    public ResponseEntity<?> getUserChipTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ChipTransaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChipTransaction> queryWrapper = new LambdaQueryWrapper<ChipTransaction>()
                .eq(ChipTransaction::getUserId, userId)
                .orderByDesc(ChipTransaction::getCreatedAt);
        
        IPage<ChipTransaction> transactionPage = chipTransactionService.page(pageParam, queryWrapper);
        return ResponseEntity.ok(transactionPage);
    }

    /**
     * 更新用户筹码
     *
     * @param currentUser 当前用户
     * @param userId      用户ID
     * @param amount      筹码变动量
     * @param reason      变动原因
     * @return 更新结果
     */
    @PostMapping("/{userId}/chips")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserChips(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        
        boolean success = userService.updateChips(userId, amount);
        if (!success) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "更新筹码失败"));
        }
        
        // 记录筹码交易
        ChipTransaction transaction = new ChipTransaction()
                .setUserId(userId)
                .setAmount(amount)
                .setTransactionType(amount.compareTo(BigDecimal.ZERO) > 0 ? "DEPOSIT" : "WITHDRAW")
                .setReason(reason)
                .setOperatorId(currentUser.getId())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setDeleted(0);
        
        chipTransactionService.save(transaction);
        
        return ResponseEntity.ok(new ApiResponse(true, "更新筹码成功", transaction));
    }

    /**
     * 获取排行榜
     *
     * @param type 排行榜类型 (chips, wins, winRate)
     * @param limit 限制数量
     * @return 排行榜数据
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "chips") String type,
            @RequestParam(defaultValue = "10") int limit) {
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        switch (type) {
            case "chips":
                queryWrapper.orderByDesc(User::getCurrentChips);
                break;
            case "wins":
                queryWrapper.orderByDesc(User::getWins);
                break;
            case "winRate":
                queryWrapper.gt(User::getTotalGames, 0)
                        .orderByDesc(u -> u.getWins() * 100 / u.getTotalGames());
                break;
            default:
                return ResponseEntity.badRequest().body(new ApiResponse(false, "无效的排行榜类型"));
        }
        
        // 限制返回数量
        Page<User> page = new Page<>(1, limit);
        IPage<User> userPage = userService.page(page, queryWrapper);
        
        return ResponseEntity.ok(userPage.getRecords());
    }
} 