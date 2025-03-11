package com.dezhou.poker.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * MyBatis-Plus代码生成器
 */
public class CodeGenerator {
    public static void main(String[] args) {
        // 数据库连接配置
        String url = "jdbc:mysql://localhost:3306/dezhou?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "root";
        
        // 项目路径配置
        String projectPath = System.getProperty("user.dir");
        
        // 代码生成
        FastAutoGenerator.create(url, username, password)
            // 全局配置
            .globalConfig(builder -> {
                builder.author("CodeGenerator") // 设置作者
                    .outputDir(projectPath + "/src/main/java") // 设置输出目录
                    .commentDate("yyyy-MM-dd") // 注释日期
                    .disableOpenDir(); // 禁止打开输出目录
            })
            // 包配置
            .packageConfig(builder -> {
                builder.parent("com.dezhou.poker") // 设置父包名
                    .entity("entity") // 设置实体包名
                    .service("service") // 设置service包名
                    .serviceImpl("service.impl") // 设置service实现包名
                    .mapper("mapper") // 设置mapper包名
                    .xml("mapper.xml") // 设置xml包名
                    .controller("controller") // 设置controller包名
                    .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper")); // 设置mapperXml生成路径
            })
            // 策略配置
            .strategyConfig(builder -> {
                builder.addInclude( // 设置需要生成的表名
                        "user",
                        "room",
                        "room_player",
                        "game_history",
                        "player_game_history",
                        "game_action",
                        "allin_vote",
                        "chip_transaction"
                    )
                    // 实体策略配置
                    .entityBuilder()
                    .enableLombok() // 开启lombok
                    .enableTableFieldAnnotation() // 开启生成实体时生成字段注解
                    .logicDeleteColumnName("deleted") // 逻辑删除字段名
                    .enableActiveRecord() // 开启ActiveRecord模式
                    
                    // Mapper策略配置
                    .mapperBuilder()
                    .enableMapperAnnotation() // 开启 @Mapper 注解
                    .enableBaseResultMap() // 启用 BaseResultMap 生成
                    .enableBaseColumnList() // 启用 BaseColumnList
                    
                    // Service策略配置
                    .serviceBuilder()
                    .formatServiceFileName("%sService") // 格式化 service 接口文件名称
                    .formatServiceImplFileName("%sServiceImpl") // 格式化 service 实现类文件名称
                    
                    // Controller策略配置
                    .controllerBuilder()
                    .enableRestStyle(); // 开启生成 @RestController 控制器
            })
            // 模板引擎配置
            .templateEngine(new FreemarkerTemplateEngine())
            .execute();
    }
} 