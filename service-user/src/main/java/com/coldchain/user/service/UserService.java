package com.coldchain.user.service;

import com.coldchain.user.dto.*;

/**
 * 用户服务接口
 *
 * @author ColdChain
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应（包含Token）
     */
    LoginResponse register(UserRegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含Token）
     */
    LoginResponse login(UserLoginRequest request);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatar 头像
     */
    void updateUserInfo(Long userId, String nickname, String avatar);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
