package com.dezhou.poker.config;

import com.dezhou.poker.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * 模拟登录拦截器
 * 用于开发环境，自动为请求添加认证信息，绕过JWT验证
 */
@Component
public class MockAuthInterceptor implements HandlerInterceptor {

    @Value("${app.auth.mock.enabled:false}")
    private boolean mockEnabled;

    @Value("${app.auth.mock.username:admin}")
    private String mockUsername;

    @Value("${app.auth.mock.userId:1}")
    private Long mockUserId;

    @Value("${app.auth.mock.role:ADMIN}")
    private String mockRole;

    private final JwtTokenProvider tokenProvider;

    public MockAuthInterceptor(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只有在启用模拟登录且当前没有认证信息时才进行模拟登录
        if (mockEnabled && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    mockUsername, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + mockRole)));

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌并添加到请求头
            String jwt = tokenProvider.generateToken(authentication);
            request.setAttribute("Authorization", "Bearer " + jwt);
        }

        return true;
    }
} 