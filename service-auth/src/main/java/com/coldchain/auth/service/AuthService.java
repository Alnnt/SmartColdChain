package com.coldchain.auth.service;

import com.coldchain.auth.dto.*;

/**
 * 认证服务接口
 *
 * @author Alnnt
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 鏅€氱敤鎴风櫥褰?
     */
    UserLoginResponse userLogin(LoginRequest request);

    /**
     * 鏅€氱敤鎴锋敞鍐?
     */
    UserLoginResponse register(RegisterRequest request);

    /**
     * 鍒锋柊Token
     */
    TokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 验证Token
     *
     */
    boolean validateToken(String token);
}
