package com.coldchain.user.controller;

import com.coldchain.auth.common.util.RequestUtil;
import com.coldchain.common.result.Result;
import com.coldchain.user.dto.UserVO;
import com.coldchain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Alnnt
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController {

    private final UserService userService;

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
            @Parameter(description = "昵称") @RequestParam(value = "nickname", required = false) String nickname,
            @Parameter(description = "头像URL") @RequestParam(value = "avatar", required = false) String avatar) {
        Long userId = RequestUtil.getUserId();
        userService.updateUserInfo(userId, nickname, avatar);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @Parameter(description = "旧密码") @RequestParam("oldPassword") String oldPassword,
            @Parameter(description = "新密码") @RequestParam("newPassword") String newPassword) {
        Long userId = RequestUtil.getUserId();
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
