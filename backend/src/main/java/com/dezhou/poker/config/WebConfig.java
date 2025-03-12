package com.dezhou.poker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于配置CORS等Web相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置CORS过滤器
     * 允许前端应用与后端进行跨域通信
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许接受cookies
        config.setAllowCredentials(true);
        
        // 允许所有来源
        config.addAllowedOrigin("http://localhost:3000"); // 前端应用地址
        
        // 允许所有头信息
        config.addAllowedHeader("*");
        
        // 允许所有方法（GET, POST, PUT, DELETE等）
        config.addAllowedMethod("*");
        
        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 