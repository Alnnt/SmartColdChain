package com.coldchain.user.service;

import com.coldchain.user.dto.UserVO;

/**
 * 用户服务接口
 *
 * @author Alnnt
 */
public interface UserService {

    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛淇℃伅
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛淇℃伅
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
