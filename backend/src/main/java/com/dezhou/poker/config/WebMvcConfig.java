package com.dezhou.poker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MockAuthInterceptor mockAuthInterceptor;

    @Autowired
    public WebMvcConfig(MockAuthInterceptor mockAuthInterceptor) {
        this.mockAuthInterceptor = mockAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mockAuthInterceptor).addPathPatterns("/**");
    }
} 