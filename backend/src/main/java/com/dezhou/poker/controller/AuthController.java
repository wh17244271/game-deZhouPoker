package com.dezhou.poker.controller;

import com.dezhou.poker.dto.request.LoginRequest;
import com.dezhou.poker.dto.request.SignUpRequest;
import com.dezhou.poker.dto.response.ApiResponse;
import com.dezhou.poker.dto.response.JwtAuthenticationResponse;
import com.dezhou.poker.model.User;
import com.dezhou.poker.security.JwtTokenProvider;
import com.dezhou.poker.security.UserPrincipal;
import com.dezhou.poker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return JWT认证响应
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userPrincipal.getId(), userPrincipal.getUsername()));
    }

    /**
     * 用户注册
     *
     * @param signUpRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "用户名已被使用!"),
                    HttpStatus.BAD_REQUEST);
        }

        // 创建用户
        User user = userService.createUser(
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getInitialChips()
        );

        return ResponseEntity.ok(new ApiResponse(true, "用户注册成功"));
    }
}
