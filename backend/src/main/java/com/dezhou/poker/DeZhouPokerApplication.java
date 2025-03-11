package com.dezhou.poker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 德州扑克应用程序入口
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableAsync
@MapperScan("com.dezhou.poker.mapper")
@EntityScan("com.dezhou.poker.entity")
public class DeZhouPokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeZhouPokerApplication.class, args);
    }
}