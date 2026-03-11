package com.coldchain.user.controller;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.auth.common.util.RequestUtil;
import com.coldchain.common.result.Result;
import com.coldchain.user.dto.*;
import com.coldchain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author ColdChain
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<LoginResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());
        LoginResponse response = userService.register(request);
        return Result.success(response);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = RequestUtil.getUserId();
        UserVO user = userService.getCurrentUser(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUserInfo(
            HttpServletRequest request,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "头像URL") @RequestParam(required = false) String avatar) {
        Long userId = RequestUtil.getUserId();
        userService.updateUserInfo(userId, nickname, avatar);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @Parameter(description = "旧密码") @RequestParam String oldPassword,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        Long userId = RequestUtil.getUserId();
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

//    /**
//     * 从请求中获取用户ID
//     */
//    private Long getUserIdFromRequest(HttpServletRequest request) {
//        // 优先从网关传递的 Header 获取
//        String userId = request.getHeader(AuthConstants.USER_ID_HEADER);
//        if (StringUtils.hasText(userId)) {
//            return Long.valueOf(userId);
//        }
//        // 否则从 Token 中解析
//        String token = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
//        if (token != null && token.startsWith(AuthConstants.TOKEN_PREFIX)) {
//            token = token.substring(AuthConstants.TOKEN_PREFIX.length());
//            return jwtTokenUtil.getUserIdFromToken(token);
//        }
//        return null;
//    }
}
