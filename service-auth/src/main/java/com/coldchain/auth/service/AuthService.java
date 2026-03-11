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
     * 刷新Token
     *
     * @param request 刷新请求
     * @return Token响应
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
     *
     * @param token 访问Token
     */
    void logout(String token);

    /**
     * 验证Token
     *
     * @param token 访问Token
     * @return 是否有效
     */
    boolean validateToken(String token);
}
