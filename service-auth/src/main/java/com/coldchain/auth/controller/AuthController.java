package com.coldchain.auth.controller;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.auth.dto.*;
import com.coldchain.auth.service.AuthService;
import com.coldchain.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录、登出、Token刷新")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("刷新Token请求");
        TokenResponse response = authService.refreshToken(request);
        return Result.success(response);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserInfoVO userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        authService.logout(token);
        return Result.success();
    }

    @GetMapping("/validate")
    @Operation(summary = "验证Token有效性")
    public Result<Boolean> validateToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        boolean valid = authService.validateToken(token);
        return Result.success(valid);
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AuthConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            return jwtTokenUtil.getUserIdFromToken(token);
        }
        // 也支持从网关传递的Header获取
        String userId = request.getHeader(AuthConstants.USER_ID_HEADER);
        if (StringUtils.hasText(userId)) {
            return Long.valueOf(userId);
        }
        return null;
    }
}
