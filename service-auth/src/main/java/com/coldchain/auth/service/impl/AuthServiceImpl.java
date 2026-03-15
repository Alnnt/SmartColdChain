package com.coldchain.auth.service.impl;

import com.coldchain.auth.common.AuthConstants;
import com.coldchain.auth.common.JwtTokenUtil;
import com.coldchain.auth.dto.*;
import com.coldchain.auth.entity.SysPermission;
import com.coldchain.auth.entity.SysRole;
import com.coldchain.auth.entity.SysUser;
import com.coldchain.auth.mapper.SysPermissionMapper;
import com.coldchain.auth.mapper.SysRoleMapper;
import com.coldchain.auth.mapper.SysUserMapper;
import com.coldchain.auth.service.AuthService;
import com.coldchain.common.exception.BusinessException;
import com.coldchain.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 *
 * @author Alnnt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = findUserByUsernameOrPhone(request.getUsername());

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        List<String> roleCodes = loadRoleCodes(user.getId());
        List<String> permissionCodes = loadPermissionCodes(user.getId());

        Long warehouseId = user.getWarehouseId();
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername(), roleCodes, permissionCodes, warehouseId);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: username={}", user.getUsername());

        return LoginResponse.builder()
                .userId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .warehouseId(warehouseId != null ? String.valueOf(warehouseId) : null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public UserLoginResponse userLogin(LoginRequest request) {
        SysUser user = findUserByUsernameOrPhone(request.getUsername());

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED, "账号已被禁用");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());

        log.info("普通用户登录成功 username={}", user.getUsername());

        return UserLoginResponse.builder()
                .userId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(accessToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        if (userMapper.selectByPhone(request.getPhone()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号已注册");
        }

        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .phone(request.getPhone())
                .userType(0)
                .status(1)
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: username={}", user.getUsername());

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());

        return UserLoginResponse.builder()
                .userId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .accessToken(accessToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED, "刷新令牌已过期");
        }

        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "无效的刷新令牌");
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        SysUser user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() != 1)) {
            throw new BusinessException(ResultCode.USER_DISABLED, "用户不存在或已被禁用");
        }

        String newAccessToken;
        if (user.getUserType() != null && user.getUserType() == 1) {
            List<String> roleCodes = loadRoleCodes(userId);
            List<String> permissionCodes = loadPermissionCodes(userId);
            newAccessToken = jwtTokenUtil.generateAccessToken(userId, username, roleCodes, permissionCodes);
        } else {
            newAccessToken = jwtTokenUtil.generateAccessToken(userId, username);
        }
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

        log.info("Token刷新成功: username={}", username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        List<String> roleCodes = loadRoleCodes(userId);
        List<String> permissionCodes = loadPermissionCodes(userId);

        return UserInfoVO.builder()
                .userId(user.getId() != null ? String.valueOf(user.getId()) : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .warehouseId(user.getWarehouseId() != null ? String.valueOf(user.getWarehouseId()) : null)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenUtil.validateToken(token)) {
            String key = AuthConstants.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", jwtTokenUtil.getAccessTokenExpiration(), TimeUnit.SECONDS);
            log.info("用户登出，Token已加入黑名单");
        }
    }

    @Override
    public boolean validateToken(String token) {
        String key = AuthConstants.TOKEN_BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists) {
            return false;
        }
        return jwtTokenUtil.validateToken(token);
    }

    private SysUser findUserByUsernameOrPhone(String usernameOrPhone) {
        SysUser user = userMapper.selectByUsername(usernameOrPhone);
        if (user == null) {
            user = userMapper.selectByPhone(usernameOrPhone);
        }
        return user;
    }

    private List<String> loadRoleCodes(Long userId) {
        List<SysRole> roles = roleMapper.selectByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());
    }

    private List<String> loadPermissionCodes(Long userId) {
        List<SysPermission> permissions = permissionMapper.selectByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(SysPermission::getCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());
    }
}
