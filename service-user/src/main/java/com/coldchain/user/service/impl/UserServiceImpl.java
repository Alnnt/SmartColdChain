package com.coldchain.user.service.impl;

import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.ResultCode;
import com.coldchain.user.dto.*;
import com.coldchain.user.entity.User;
import com.coldchain.user.mapper.UserMapper;
import com.coldchain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author ColdChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(UserRegisterRequest request) {
        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        // 校验用户名是否已存在
        User existUser = userMapper.selectByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 校验手机号是否已存在
        User phoneUser = userMapper.selectByPhone(request.getPhone());
        if (phoneUser != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: username={}", user.getUsername());

        // 生成Token并返回
        String token = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(token)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        // 查询用户（支持用户名或手机号登录）
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.selectByPhone(request.getUsername());
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: username={}", user.getUsername());

        // 生成Token并返回
        String token = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(token)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户密码修改成功: userId={}", userId);
    }
}
