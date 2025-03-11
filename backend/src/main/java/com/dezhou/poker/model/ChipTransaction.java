package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 筹码变动记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chip_transactions")
public class ChipTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameHistory game;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime = LocalDateTime.now();

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        BUY_IN,     // 买入
        CASH_OUT,   // 取出
        WIN,        // 赢
        LOSE        // 输
    }
}